package com.owlab.callblocker.receiver;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.owlab.callblocker.CONS;
import com.owlab.callblocker.R;
import com.owlab.callblocker.contentprovider.CallBlockerDbHelper;
import com.owlab.callblocker.service.CallLogObserverStartService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

/**
 * Created by ernest on 5/27/16.
 */
public class PhoneStateChangeReceiver extends AbstractPhoneStateChangeReceiver {
    private static final String TAG = PhoneStateChangeReceiver.class.getSimpleName();

    private static boolean mIsRingerChanged = false;
    private static int mLastRingerMode = 0;
    SharedPreferences sharedPreferences;

    public PhoneStateChangeReceiver() {
        Log.d(TAG, ">>>>> instantiated");
    }

    @Override
    protected void onIncomingCallArrived(Context context, String phoneNumber, Date fromTime) {
        Log.d(TAG, ">>>>> Call arrived: " + phoneNumber + " at " + fromTime.toString());
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        boolean isBlockingOn = sharedPreferences.getBoolean(context.getString(R.string.pref_key_blocking_on), false);
        if (!isBlockingOn) {
            Log.d(TAG, ">>>>> preference - blocking - off, do nothing");
            return;
        }

        boolean blockHiddenNumberOn = sharedPreferences.getBoolean(context.getString(R.string.settings_key_block_hidden_number), false);
        boolean blockUnknownNumberOn = sharedPreferences.getBoolean(context.getString(R.string.settings_key_block_unknown_number), false)
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;

        if(blockHiddenNumberOn) {
            if(phoneNumber == null || phoneNumber.isEmpty()) {
                blockCall(context, "", fromTime);
                return;
            }
        }

        if(phoneNumber != null && !phoneNumber.isEmpty()) {
            if(blockUnknownNumberOn && !isInContacts(context, phoneNumber)) {
                blockCall(context, phoneNumber, fromTime);
                return;
            }
        }

        CallBlockerDbHelper dbHelper = new CallBlockerDbHelper(context);
        if(dbHelper.isActiveBlockedNumberStartsWith(phoneNumber) || dbHelper.isActiveBlockedNumberExact(phoneNumber)) {
            blockCall(context, phoneNumber, fromTime);
            return;
        } else {
            Toast.makeText(context, "the incoming number (" + phoneNumber + ") is not subject to be filtered", Toast.LENGTH_SHORT).show();
        }
    }

    private static final String[] contactsProjection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};
    private boolean isInContacts(Context context, String phoneNumber) {
        boolean result = false;

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        //Log.d(TAG, ">>>>> uri: " + uri.toString());
        Cursor contactsCursor = context.getContentResolver().query(uri, contactsProjection, null, null, null);
        if (contactsCursor != null) {
            if (contactsCursor.getCount() > 0) {
                result = true;
            }
            contactsCursor.close();
        }

        return result;
    }

    private void blockCall(Context context, String phoneNumber, Date fromTime) {
        int blockedCount = sharedPreferences.getInt(context.getString(R.string.status_key_notification_count), 0);

        boolean suppressRingingOn = sharedPreferences.getBoolean(context.getString(R.string.settings_key_suppress_ringing), false);
        //boolean suppressCallNotificationOn = sharedPreferences.getBoolean(context.getString(R.string.settings_key_suppress_call_notification), false);
        boolean dismissCallOn = sharedPreferences.getBoolean(context.getString(R.string.settings_key_dismiss_call), false);

        boolean deleteCallLogOn = sharedPreferences.getBoolean(context.getString(R.string.settings_key_delete_call_log), false)
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED;


        Log.d(TAG, ">>>>> starting content observer start service");
        Intent intent = new Intent(context, CallLogObserverStartService.class);
        intent.putExtra(CONS.INTENT_KEY_PHONE_NUMBER, phoneNumber);
        intent.putExtra(CONS.INTENT_KEY_TIME_FROM, fromTime.getTime() - (3 * 1000));
        intent.putExtra(CONS.INTENT_KEY_SHOULD_DELETE, deleteCallLogOn);
        context.startService(intent);

        if (suppressRingingOn) {
            suppressRinging(context);
        }

        if (dismissCallOn) {
            //This should be dismissed
            dismissCall(context);
        }

        sharedPreferences.edit().putInt(context.getString(R.string.status_key_notification_count), ++blockedCount);
    }

    private void suppressRinging(Context context) {
        mIsRingerChanged = false;
        //Subject to getting be quiet
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        //int amMode = am.getMode();
        if (am.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
            mLastRingerMode = am.getRingerMode();
            am.setRingerMode(AudioManager.RINGER_MODE_SILENT); //Silent and not vibrate
            mIsRingerChanged = true;
            Toast.makeText(context, "change ringer mode to silent", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "current ringer mode is silent", Toast.LENGTH_SHORT).show();
        }
    }

    private void releaseRinging(Context context) {
        if (mIsRingerChanged) {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            am.setRingerMode(mLastRingerMode);
            mIsRingerChanged = false;
            Toast.makeText(context, "ringer mode restored", Toast.LENGTH_SHORT).show();
        }
    }

    private void dismissCall(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Method getTelephonyMethod = telephonyManager.getClass().getDeclaredMethod("getITelephony");
            getTelephonyMethod.setAccessible(true);
            Object iTelephony = getTelephonyMethod.invoke(telephonyManager);
            //Method silenceRingerMethod = iTelephony.getClass().getDeclaredMethod("silenceRinger");
            Method endCallMethod = iTelephony.getClass().getDeclaredMethod("endCall");
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

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
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
