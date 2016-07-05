package com.owlab.callquieter.listener;

import android.Manifest;
import android.content.ContentResolver;
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
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.owlab.callquieter.CONS;
import com.owlab.callquieter.R;
import com.owlab.callquieter.contentobserver.CallQuieterContentChangeListener;
import com.owlab.callquieter.contentprovider.CallQuieterDbHelper;
import com.owlab.callquieter.service.CallLogObserverStartService;
import com.owlab.callquieter.service.CallQuieterIntentService;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * Created by ernest on 6/29/16.
 * TelephonyManager telManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
 * Usuage:
 * PhoneStateListener phoneStateListener = new CallQuieterPhoneStateListener(context);
 *         telManager.listen(customPhoneStateListener,
 *         PhoneStateListener.LISTEN_CALL_STATE
 *         | PhoneStateListener.LISTEN_CELL_INFO
 *         | PhoneStateListener.LISTEN_CELL_LOCATION
 *         | PhoneStateListener.LISTEN_DATA_ACTIVITY
 *         | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
 *         | PhoneStateListener.LISTEN_SERVICE_STATE
 *         | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
 *         | PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR
 *         | PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR
 *         );
 *
 * //After some time
 * telManager.listen(customPhoneStateListener, PhoneStateListener.LISTEN_NONE);
 */
public class CallQuieterPhoneStateListener extends PhoneStateListener implements CallQuieterContentChangeListener {
    public static final String TAG = CallQuieterPhoneStateListener.class.getSimpleName();

    Context ctx;

    //To determine call state precisely
    private int LAST_CALL_STATE = -101;
    private boolean isAnswered = false;

    //Used for main functionalities
    private AudioManager audioManager;
    private SharedPreferences sharedPreferences;
    private ContentResolver contentResolver;
    private Object iTelephonyObject;
    private Method endCallMethod;
    private CallQuieterDbHelper callQuieterDbHelper;
    //This should be updated whenever the phone number list changes
    private Pattern matchPattern;

    private static final String[] contactsProjection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

    private int mLastRinggerMode;
    private boolean mIsRingerChanged;

    public CallQuieterPhoneStateListener(Context ctx) {
        Log.d(TAG, "instance created");
        this.ctx = ctx;
        //
        try {
            init();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void init() throws Exception {
        audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        contentResolver = ctx.getContentResolver();
        TelephonyManager telephonyManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        Method getITelephonyMethod = telephonyManager.getClass().getDeclaredMethod("getITelephony");
        getITelephonyMethod.setAccessible(true);
        iTelephonyObject = getITelephonyMethod.invoke(telephonyManager);
        endCallMethod = iTelephonyObject.getClass().getDeclaredMethod("endCall");
        callQuieterDbHelper = new CallQuieterDbHelper(ctx);
        matchPattern = callQuieterDbHelper.getMatchPattern();
    }

    @Override
    public void onCallStateChanged(int state, String phoneNumber) {
        //super.onCallStateChanged(state, incomingNumber);
        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                Log.d(TAG, "onCallStateChanged: CALL_STATE_IDLE");
                switch(LAST_CALL_STATE) {
                    case TelephonyManager.CALL_STATE_RINGING:
                        //Incoming call missed or dismissed
                        onIncomingCallMissed(phoneNumber, System.currentTimeMillis());
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        if(isAnswered) {
                            onIncomingCallEnded(phoneNumber, System.currentTimeMillis());
                            isAnswered = false;
                        } else {
                            onOutgoingCallEnded(phoneNumber, System.currentTimeMillis());
                        }
                        //Outgoing call ended
                        break;
                    default:
                }
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                Log.d(TAG, "onCallStateChanged: CALL_STATE_RINGING");
                onIncomingCallArrived(phoneNumber, System.currentTimeMillis());
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                Log.d(TAG, "onCallStateChanged: CALL_STATE_OFFHOOK");
                switch(LAST_CALL_STATE) {
                    case TelephonyManager.CALL_STATE_IDLE:
                        //Outgoing call
                        onOutgoingCallStarted(phoneNumber, System.currentTimeMillis());
                        break;
                    case TelephonyManager.CALL_STATE_RINGING:
                        //Answering call
                        onIncomingCallAnswered(phoneNumber, System.currentTimeMillis());
                        isAnswered = true;
                        break;
                    default:

                }
                break;
            default:
                Log.d(TAG, "onCallStateChanged: UNKNOWN_STATE: " + state);
                break;
        }

        LAST_CALL_STATE = state;
    }

    private void onIncomingCallArrived(String phoneNumber, long timeArrived) {
        Log.d(TAG, ">>>>> onIncomingCallArrived: (" + phoneNumber + ")");
        if(!sharedPreferences.getBoolean(CONS.PREF_KEY_BLOCKING_ON, false)) {
            //If call quieter not on
            return;
        }

        if(phoneNumber == null || phoneNumber.isEmpty()) {
            if(sharedPreferences.getBoolean(ctx.getString(R.string.settings_key_block_hidden_number), false)) {
                //If make hidden number quiet
                quietCall("", timeArrived);
            }
            //No need to proceed with empty number
            return;
        }

        if(sharedPreferences.getBoolean(ctx.getString(R.string.settings_key_block_unknown_number), false)
                && ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            if(!isInContacts(phoneNumber)) {
                quietCall(phoneNumber, timeArrived);
                return;
            }
        }

        if(matchPattern.matcher(phoneNumber).matches()) {
            quietCall(phoneNumber, timeArrived);
            return;
        }
    }

    private void onIncomingCallMissed(String phoneNumber, long timeMissed) {
        Log.d(TAG, ">>>>> onIncomingCallMissed");

        recoverRinggerMode();
    }

    private void onIncomingCallAnswered(String phoneNumber, long timeAnswered) {
        Log.d(TAG, ">>>>> onIncomingCallAnswered");

    }

    private void onIncomingCallEnded(String phoneNumber, long timeEnded) {
        Log.d(TAG, ">>>>> onIncomingCallEnded");

        recoverRinggerMode();
    }

    private void onOutgoingCallStarted(String phoneNumber, long timeOutgoingStarted) {
        Log.d(TAG, ">>>>> onOutgoingCallStarted");

    }

    private void onOutgoingCallEnded(String phoneNumber, long timeOutgoingEnded) {
        Log.d(TAG, ">>>>> onOutgoingCallEnded");

    }

    @Override
    public void onContentChanged() {
        Log.d(TAG, ">>>>> updating match pattern");
        matchPattern = callQuieterDbHelper.getMatchPattern();
    }

    private boolean isInContacts(String phoneNumber) {
        boolean result = false;
        Uri contactsFilterUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor contactsCursor = contentResolver.query(contactsFilterUri, contactsProjection, null, null, null);

        if(contactsCursor != null) {
            result = contactsCursor.getCount() > 0;
            contactsCursor.close();
        }
        return result;
    }

    private void quietCall(String phoneNumber, long time) {
        if(sharedPreferences.getBoolean(ctx.getString(R.string.settings_key_suppress_ringing), false)) {
            suppressRinging();
        }

        if(sharedPreferences.getBoolean(ctx.getString(R.string.settings_key_dismiss_call), false)) {
            dismisCall();
        }

        handleCallLog(phoneNumber, time);
    }

    private void suppressRinging() {
        mLastRinggerMode = audioManager.getRingerMode();
        Log.d(TAG, ">>>>> last ringger: " + mLastRinggerMode);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        mIsRingerChanged = true;

        Toast.makeText(ctx, "Ringger mode replaced to silence", Toast.LENGTH_SHORT).show();
    }

    private void recoverRinggerMode() {
        if(mIsRingerChanged) {
            Log.d(TAG, ">>>>> last ringger: " + mLastRinggerMode);
            audioManager.setRingerMode(mLastRinggerMode);
            mIsRingerChanged = false;
            Toast.makeText(ctx, "Ringger restored", Toast.LENGTH_SHORT).show();
        }
    }

    private void dismisCall() {
        try {
            endCallMethod.invoke(iTelephonyObject);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void handleCallLog(String phoneNumber, long fromTime) {
        boolean deleteCallLogOn = sharedPreferences.getBoolean(ctx.getString(R.string.settings_key_delete_call_log), false)
                && ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED;


        Log.d(TAG, ">>>>> starting content observer start service");
        Intent intent = new Intent(ctx, CallLogObserverStartService.class);
        intent.putExtra(CONS.INTENT_KEY_PHONE_NUMBER, phoneNumber);
        intent.putExtra(CONS.INTENT_KEY_TIME_FROM, fromTime - (3 * 1000));
        intent.putExtra(CONS.INTENT_KEY_SHOULD_DELETE, deleteCallLogOn);
        ctx.startService(intent);


        //Count blockings
        int blockedCount = sharedPreferences.getInt(ctx.getString(R.string.status_key_notification_count), 0);
        sharedPreferences.edit().putInt(ctx.getString(R.string.status_key_notification_count), ++blockedCount).commit();
        //Update block counter in notification
        CallQuieterIntentService.startActionStatusbarNotificationCounterUpdate(ctx);
    }
    /*
    @Override
    public void onCellInfoChanged(List<CellInfo> cellInfoList) {
        super.onCellInfoChanged(cellInfoList);
        Log.d(TAG, "onCellInfoChanged: " + cellInfoList);
    }

    @Override
    public void onDataActivity(int direction) {
        super.onDataActivity(direction);

        switch(direction) {
            case TelephonyManager.DATA_ACTIVITY_NONE:
                Log.d(TAG, "onDataActivity: DATA_ACTIVITY_NONE");
                break;
            case TelephonyManager.DATA_ACTIVITY_IN:
                Log.d(TAG, "onDataActivity: DATA_ACTIVITY_IN");
                break;
            case TelephonyManager.DATA_ACTIVITY_OUT:
                Log.d(TAG, "onDataActivity: DATA_ACTIVITY_OUT");
                break;
            case TelephonyManager.DATA_ACTIVITY_INOUT:
                Log.d(TAG, "onDataActivity: DATA_ACTIVITY_INOUT");
                break;
            case TelephonyManager.DATA_ACTIVITY_DORMANT:
                Log.d(TAG, "onDataActivity: DATA_ACTIVITY_DORMANT");
                break;
            default:
                Log.w(TAG, "onDataActivity: UNKNOWN " + direction);
                break;
        }
    }

    @Override
    public void onServiceStateChanged(ServiceState serviceState) {
        super.onServiceStateChanged(serviceState);
        Log.d(TAG, "onServiceStateChanged: " + serviceState.toString());

        switch (serviceState.getState()) {
            case ServiceState.STATE_IN_SERVICE:
                Log.d(TAG, "onServiceStateChanged: STATE_IN_SERVICE");
                break;
            case ServiceState.STATE_OUT_OF_SERVICE:
                Log.d(TAG, "onServiceStateChanged: STATE_OUT_OF_SERVICE");
                break;
            case ServiceState.STATE_EMERGENCY_ONLY:
                Log.d(TAG, "onServiceStateChanged: STATE_EMERGENCY_ONLY");
                break;
            case ServiceState.STATE_POWER_OFF:
                Log.d(TAG, "onServiceStateChanged: STATE_POWER_OFF");
                break;
        }
    }
    */

    /*
    @Override
    public void onCellLocationChanged(CellLocation location) {
        super.onCellLocationChanged(location);
        if (location instanceof GsmCellLocation) {
            GsmCellLocation gcLoc = (GsmCellLocation) location;
            Log.d(TAG,
                    "onCellLocationChanged: GsmCellLocation "
                            + gcLoc.toString());
            Log.d(TAG, "onCellLocationChanged: GsmCellLocation getCid "
                    + gcLoc.getCid());
            Log.d(TAG, "onCellLocationChanged: GsmCellLocation getLac "
                    + gcLoc.getLac());
            Log.d(TAG, "onCellLocationChanged: GsmCellLocation getPsc"
                    + gcLoc.getPsc()); // Requires min API 9
        } else if (location instanceof CdmaCellLocation) {
            CdmaCellLocation ccLoc = (CdmaCellLocation) location;
            Log.d(TAG,
                    "onCellLocationChanged: CdmaCellLocation "
                            + ccLoc.toString());
            Log.d(TAG,
                    "onCellLocationChanged: CdmaCellLocation getBaseStationId "
                            + ccLoc.getBaseStationId());
            Log.d(TAG,
                    "onCellLocationChanged: CdmaCellLocation getBaseStationLatitude "
                            + ccLoc.getBaseStationLatitude());
            Log.d(TAG,
                    "onCellLocationChanged: CdmaCellLocation getBaseStationLongitude"
                            + ccLoc.getBaseStationLongitude());
            Log.d(TAG,
                    "onCellLocationChanged: CdmaCellLocation getNetworkId "
                            + ccLoc.getNetworkId());
            Log.d(TAG,
                    "onCellLocationChanged: CdmaCellLocation getSystemId "
                            + ccLoc.getSystemId());
        } else {
            Log.d(TAG, "onCellLocationChanged: " + location.toString());
        }
    }

    @Override
    public void onCallForwardingIndicatorChanged(boolean cfi) {
        super.onCallForwardingIndicatorChanged(cfi);
        Log.d(TAG, "onCallForwardingIndicatorChanged: " + cfi);
    }

    @Override
    public void onMessageWaitingIndicatorChanged(boolean mwi) {
        super.onMessageWaitingIndicatorChanged(mwi);
        Log.d(TAG, "onMessageWaitingIndicatorChanged: " + mwi);
    }
    */
}
