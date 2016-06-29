package com.owlab.callblocker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.owlab.callblocker.CONS;

import java.util.Date;

/**
 * Created by ernest on 5/29/16.
 */
public abstract class AbstractPhoneStateChangeReceiver extends BroadcastReceiver {
    private static final String TAG = AbstractPhoneStateChangeReceiver.class.getSimpleName();

    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date timeFrom;
    private static boolean isIncoming;
    private static String savedPhoneNumber;

    protected boolean initialized;

    private static long counter = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        //Log.d(TAG, ">>>>> onReceive counter: " + ++counter);
        String intentAction = intent.getAction();
        //Log.d(TAG, ">>>>> intent action: " + intentAction);

        if(intentAction.equals("android.intent.action.PHONE_STATE")) {
            //long startTime = System.currentTimeMillis();

            //Is this needed?
            if(!initialized) {
                initialize(context);
                initialized = true;
            }

            //If android.intent.action.PHONE_STATE
            //if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
                String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
                String phoneNumber = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                int state = 0;
                if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    state = TelephonyManager.CALL_STATE_IDLE;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    state = TelephonyManager.CALL_STATE_RINGING;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    state = TelephonyManager.CALL_STATE_OFFHOOK;
                } else {
                    Log.d(TAG, ">>>>> other call state: " + stateStr);
                    Log.d(TAG, ">>>>> phoneNumber: " + phoneNumber);
                    return;
                }

                onCallStateChanged(context, state, phoneNumber);
            //} else {
            //    Log.d(TAG, ">>>>> other intent action: " + intent.getAction().toString());
            //}
            //Log.d(TAG, ">>>>> processing time: " + (System.currentTimeMillis() - startTime));
        } else if(intentAction.equals("com.owlab.callblocker.WARM_UP")) {
            initialize(context);
            initialized = true;
        } else if(intentAction.equals(CONS.ACTION_UPDATE_MATCH_PATTERN)) {
            updateMatchPattern(context);
        }
    }

    // Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
    // Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
    // http://stackoverflow.com/questions/15563921/how-to-detect-incoming-calls-in-an-android-device
    private void onCallStateChanged(Context context, int state, String phoneNumber) {
        if(state == lastState) {
            return;
        }

        switch(state) {
            //TODO when "isIncoming" should be false?
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                timeFrom = new Date();
                savedPhoneNumber = phoneNumber;
                onIncomingCallArrived(context, phoneNumber, timeFrom);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //Ringing -> Offhook are picking up of incoming calls (Or that will be outgoing call)
                if(lastState == TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = true;
                    timeFrom = new Date();
                    onIncomingCallAnswered(context, savedPhoneNumber, timeFrom);
                }
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                if(lastState == TelephonyManager.CALL_STATE_RINGING) {
                    onIncomingCallMissed(context, savedPhoneNumber, timeFrom);
                } else if(isIncoming) {
                    onIncomingCallEnded(context, savedPhoneNumber, timeFrom, new Date());
                }
                break;
            default:
        }
        lastState = state;
    }

    protected abstract void initialize(Context context);
    protected abstract void updateMatchPattern(Context context);
    protected abstract void onIncomingCallArrived(Context context, String phoneNumber, Date start);
    protected abstract void onIncomingCallAnswered(Context context, String phoneNumber, Date start);
    protected abstract void onIncomingCallEnded(Context context, String phoneNumber, Date start, Date end);
    protected abstract void onIncomingCallMissed(Context context, String phoneNumber, Date start);
}
