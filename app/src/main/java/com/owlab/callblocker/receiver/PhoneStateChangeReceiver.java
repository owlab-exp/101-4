package com.owlab.callblocker.receiver;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.owlab.callblocker.R;
import com.owlab.callblocker.content.CallBlockerContentProvider;
import com.owlab.callblocker.content.CallBlockerTbl;
import com.owlab.callblocker.service.CallLogDeleteService;

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

    public PhoneStateChangeReceiver() {
        Log.d(TAG, ">>>>> instantiated");
    }

    //private static final String[] mProjection = {CallBlockerTbl.Schema._ID, CallBlockerTbl.Schema.COLUMN_NAME_PHONE_NUMBER, CallBlockerTbl.Schema.COLUMN_NAME_IS_ACTIVE};
    private static final String[] mProjection = {CallBlockerTbl.Schema._ID, CallBlockerTbl.Schema.COLUMN_NAME_PHONE_NUMBER};
    private static final String mSelectionClause = CallBlockerTbl.Schema.COLUMN_NAME_PHONE_NUMBER + " = ?" + " AND " + CallBlockerTbl.Schema.COLUMN_NAME_IS_ACTIVE + " = " + 1;
    //TODO select only active ones
    private static final String[] mSelectionArgs = {""};

    @Override
    protected void onIncomingCallArrived(Context context, String phoneNumber, Date start) {
        Log.d(TAG, ">>>>> Call arrived: " + phoneNumber + " at " + start.toString());

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isBlockingOn = sharedPreferences.getBoolean(context.getString(R.string.pref_key_blocking_on), false);
        if (!isBlockingOn) {
            Log.d(TAG, ">>>>> preference - blocking - off, do nothing");
            return;
        }

        boolean suppressRingingOn = sharedPreferences.getBoolean(context.getString(R.string.settings_key_suppress_ringing), false);
        //boolean suppressCallNotificationOn = sharedPreferences.getBoolean(context.getString(R.string.settings_key_suppress_call_notification), false);
        boolean dismissCallOn = sharedPreferences.getBoolean(context.getString(R.string.settings_key_dismiss_call), false);

        boolean deleteCallLogOn = sharedPreferences.getBoolean(context.getString(R.string.settings_key_delete_call_log), false)
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED;

        // Determine if the incoming number is registered and active in the blocking db
        // TODO: is this needed? if the passed phone number is a pure form already, then this will not be needed
        String purePhoneNumber = phoneNumber.replaceAll("[^\\d]", "");
        mSelectionArgs[0] = purePhoneNumber;
        Cursor cursor = context.getContentResolver().query(CallBlockerContentProvider.CONTENT_URI, mProjection, mSelectionClause, mSelectionArgs, null);
        //Log.d(TAG, ">>>>> cursor: " + (cursor != null ? cursor.getCount() : 0));
        //while(cursor.moveToNext()) {
        //    Log.d(TAG, ">>>> found row: (" +
        //            cursor.getString(cursor.getColumnIndexOrThrow(CallBlockerTbl.Schema.COLUMN_NAME_PHONE_NUMBER)) + ", " +
        //            cursor.getInt(cursor.getColumnIndexOrThrow(CallBlockerTbl.Schema.COLUMN_NAME_IS_ACTIVE)) +
        //            ")"
        //    );
        //}

        if (cursor != null && cursor.getCount() > 0) {
            //cursor.moveToFirst();
            //boolean isActive = cursor.getInt(cursor.getColumnIndexOrThrow(CallBlockerTbl.Schema.COLUMN_NAME_IS_ACTIVE)) > 0;
            //cursor.close();

            //if (isActive) {
            if(deleteCallLogOn) {
                Log.d(TAG, ">>>>> starting delete service");
                Intent intent = new Intent(context, CallLogDeleteService.class);
                intent.putExtra("phoneNumber", phoneNumber);
                intent.putExtra("startTime", start.getTime() - (2*1000));
                context.startService(intent);
            }
            if (suppressRingingOn) {
                suppressRinging(context);
            }
            if (dismissCallOn) {
                dismissCall(context);
            }

            //} else {
            //    Toast.makeText(context, "the incoming number (" + purePhoneNumber + ") is inactive filtering subject", Toast.LENGTH_SHORT).show();
            //}
        } else {
            Toast.makeText(context, "the incoming number (" + purePhoneNumber + ") is not subject to be filtered", Toast.LENGTH_SHORT).show();
        }
    }

    private void suppressRinging(Context context) {
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

        if (mIsRingerChanged) {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            am.setRingerMode(mLastRingerMode);
            mIsRingerChanged = false;
            Toast.makeText(context, "ringer mode restored", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onIncomingCallMissed(Context context, String phoneNumber, Date start) {
        Log.d(TAG, ">>>>> Call missed: " + phoneNumber + " at " + start.toString());

        if (mIsRingerChanged) {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            am.setRingerMode(mLastRingerMode);
            mIsRingerChanged = false;
            Toast.makeText(context, "ringer mode restored", Toast.LENGTH_SHORT).show();
        }
    }
}
