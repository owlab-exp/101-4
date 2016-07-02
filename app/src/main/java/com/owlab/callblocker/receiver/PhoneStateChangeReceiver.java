package com.owlab.callblocker.receiver;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.owlab.callblocker.CONS;
import com.owlab.callblocker.R;
import com.owlab.callblocker.contentprovider.CallBlockerDbHelper;
import com.owlab.callblocker.service.CallBlockerIntentService;
import com.owlab.callblocker.service.CallLogObserverStartService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Created by ernest on 5/27/16.
 */
public class PhoneStateChangeReceiver extends AbstractPhoneStateChangeReceiver {
    private static final String TAG = PhoneStateChangeReceiver.class.getSimpleName();

    private static boolean mIsRingerChanged = false;
    private static int mLastRingerMode = 0;

    private static SharedPreferences sharedPreferences;
    //private static CallBlockerDbHelper dbHelper;
    private static Pattern matchPattern;
    private static ContentResolver contentResolver;
    private static AudioManager audioManager;

    private static TelephonyManager telephonyManager;
    private static Method getTelephonyMethod;
    private static Object iTelephony;
    private static Method endCallMethod;

    //public PhoneStateChangeReceiver() {
    //    Log.d(TAG, ">>>>> instantiated");
    //}

    @Override
    protected void initialize(Context context) {
        Log.d(TAG, ">>>>> initializing started");

        if(sharedPreferences == null)
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        //if(dbHelper == null)
        //    dbHelper = new CallBlockerDbHelper(context);
        if(matchPattern == null) {
            CallBlockerDbHelper dbHelper = new CallBlockerDbHelper(context);
            matchPattern = dbHelper.getMatchPattern();
        }
        if(contentResolver == null)
            contentResolver = context.getContentResolver();
        if(audioManager == null)
            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        try {
            if (telephonyManager == null
                    || getTelephonyMethod == null
                    || iTelephony == null
                    || endCallMethod == null
                    ) {
                telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                getTelephonyMethod = telephonyManager.getClass().getDeclaredMethod("getITelephony");
                getTelephonyMethod.setAccessible(true);
                iTelephony = getTelephonyMethod.invoke(telephonyManager);
                endCallMethod = iTelephony.getClass().getDeclaredMethod("endCall");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, ">>>>> initializing finished");

    }

    @Override
    protected void updateMatchPattern(Context context) {
        Log.d(TAG, ">>>>> onContentChanged match pattern started");
        CallBlockerDbHelper dbHelper = new CallBlockerDbHelper(context);
        matchPattern = dbHelper.getMatchPattern();
        Log.d(TAG, ">>>>> onContentChanged match pattern finished");
    }

    @Override
    protected void onIncomingCallArrived(Context context, String phoneNumber, Date fromTime) {
        //Log.d(TAG, ">>>>> Call arrived: " + phoneNumber + " at " + fromTime.toString());

        boolean isBlockingOn = sharedPreferences.getBoolean(CONS.PREF_KEY_BLOCKING_ON, false);
        if (!isBlockingOn) {
            //Log.d(TAG, ">>>>> preference - blocking - off, do nothing");
            return;
        }

        boolean hiddenNumber = phoneNumber == null || phoneNumber.isEmpty();

        if(hiddenNumber) {
            //Check if hidden numbers should be blocked
            if(sharedPreferences.getBoolean(context.getString(R.string.settings_key_block_hidden_number), false)) {
                quietCall(context, "", fromTime);
            }
            //Since hidden number, no more matching needed
            return;
        }

        //Now no hidden number

        //Check if unknown number should be blocked
        if(sharedPreferences.getBoolean(context.getString(R.string.settings_key_block_unknown_number), false)
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            if(!isInContacts(context, phoneNumber)) {
                quietCall(context, phoneNumber, fromTime);
                return;
            }
        }

        //Check if numbers are in blocked list
        //if (dbHelper.isActiveBlockedNumberStartsWith(phoneNumber) || dbHelper.isActiveBlockedNumberExact(phoneNumber)) {
        if(matchPattern.matcher(phoneNumber).matches()) {
            quietCall(context, phoneNumber, fromTime);
            return;
        }
    }

    private static final String[] contactsProjection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

    private boolean isInContacts(Context context, String phoneNumber) {
        boolean result = false;

        Uri contactsFilterUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        //Log.d(TAG, ">>>>> uri: " + uri.toString());
        Cursor contactsCursor = contentResolver.query(contactsFilterUri, contactsProjection, null, null, null);
        if (contactsCursor != null) {
            if (contactsCursor.getCount() > 0) {
                result = true;
            }
            contactsCursor.close();
        }

        return result;
    }

    private void quietCall(Context context, String phoneNumber, Date fromTime) {

        boolean suppressRingingOn = sharedPreferences.getBoolean(context.getString(R.string.settings_key_suppress_ringing), false);
        //boolean suppressCallNotificationOn = sharedPreferences.getBoolean(context.getString(R.string.settings_key_suppress_call_notification), false);

        if (suppressRingingOn) {
            Log.d(TAG, ">>>>> suppress ring on");
            //Already suppressed
            suppressRinging(context);
        }

        boolean dismissCallOn = sharedPreferences.getBoolean(context.getString(R.string.settings_key_dismiss_call), false);

        if (dismissCallOn) {
            Log.d(TAG, ">>>>> dismiss call on");
            //This should be dismissed
            dismissCall(context);
        }

        boolean deleteCallLogOn = sharedPreferences.getBoolean(context.getString(R.string.settings_key_delete_call_log), false)
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED;


        Log.d(TAG, ">>>>> starting content observer start service");
        Intent intent = new Intent(context, CallLogObserverStartService.class);
        intent.putExtra(CONS.INTENT_KEY_PHONE_NUMBER, phoneNumber);
        intent.putExtra(CONS.INTENT_KEY_TIME_FROM, fromTime.getTime() - (3 * 1000));
        intent.putExtra(CONS.INTENT_KEY_SHOULD_DELETE, deleteCallLogOn);
        context.startService(intent);


        //Count blockings
        int blockedCount = sharedPreferences.getInt(context.getString(R.string.status_key_notification_count), 0);
        sharedPreferences.edit().putInt(context.getString(R.string.status_key_notification_count), ++blockedCount).commit();
        //Update block counter in notification
        CallBlockerIntentService.startActionStatusbarNotificationCounterUpdate(context);

        //blockedCount = sharedPreferences.getInt(context.getString(R.string.status_key_notification_count), 0);
        //Log.d(TAG, ">>>>> count: " + blockedCount);
    }


    private void suppressRinging(Context context) {
        Log.d(TAG, ">>>>> suppress ringing");
        mIsRingerChanged = false;
        ////Subject to getting be quiet
        //if(audioManager == null) {
        //    audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        //}
        //Swap ringer mode if not silent
        if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
            mLastRingerMode = audioManager.getRingerMode();
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT); //Silent and not vibrate
            mIsRingerChanged = true;
            Toast.makeText(context, "change ringer mode to silent", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "current ringer mode is silent", Toast.LENGTH_SHORT).show();
        }
    }

    private void releaseRinging(Context context) {
        if (mIsRingerChanged) {
            //Log.d(TAG, ">>>>> release ringing");
            audioManager.setRingerMode(mLastRingerMode);
            mIsRingerChanged = false;
            Toast.makeText(context, "ringer mode restored", Toast.LENGTH_SHORT).show();
        }
    }

    private void dismissCall(Context context) {
        try {
            //Method silenceRingerMethod = iTelephony.getClass().getDeclaredMethod("silenceRinger");
            //if(suppressRingingOn) {
            //    Log.d(TAG, ">>>>> suppress ringing...");
            //    //Not work
            //    silenceRingerMethod.invoke(iTelephony);
            //}
            //if(dismissCallOn) {
            Log.d(TAG, ">>>>> dismiss call...");
            //Need CALL_PHONE permission
            endCallMethod.invoke(iTelephony);
            //}

        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onIncomingCallAnswered(Context context, String phoneNumber, Date start) {
        Log.d(TAG, ">>>>> Call answered: " + phoneNumber + " at " + start.toString());
    }

    @Override
    protected void onIncomingCallEnded(Context context, String phoneNumber, Date start, Date end) {
        Log.d(TAG, ">>>>> Call ended: " + phoneNumber + ", from " + start.toString() + " to " + end.toString());
        releaseRinging(context);

    }

    @Override
    protected void onIncomingCallMissed(Context context, String phoneNumber, Date start) {
        Log.d(TAG, ">>>>> Call missed: " + phoneNumber + " at " + start.toString());
        releaseRinging(context);

    }
}
