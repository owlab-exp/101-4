package com.owlab.callblocker.listener;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

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
public class CallQuieterPhoneStateListener extends PhoneStateListener {
    public static final String TAG = CallQuieterPhoneStateListener.class.getSimpleName();

    private int PREVIOUS_CALL_STATE = -101;

    Context ctx;
    //AudioManager audioManager;

    int ringerMode = -1;

    public CallQuieterPhoneStateListener(Context ctx) {
        Log.d(TAG, "instance created");
        this.ctx = ctx;
        //audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void onCallStateChanged(int state, String phoneNumber) {
        //super.onCallStateChanged(state, incomingNumber);
        Log.d(TAG, ">>>>> incomingNumber: " + phoneNumber);
        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                Log.d(TAG, "onCallStateChanged: CALL_STATE_IDLE");
                switch(PREVIOUS_CALL_STATE) {
                    case TelephonyManager.CALL_STATE_IDLE:
                        break;
                    case TelephonyManager.CALL_STATE_RINGING:
                        //Incoming call missed or dismissed
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        //Outgoing call ended
                        break;
                    default:

                }
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                Log.d(TAG, "onCallStateChanged: CALL_STATE_RINGING");
                switch(PREVIOUS_CALL_STATE) {
                    case TelephonyManager.CALL_STATE_IDLE:
                        //Incoming call arrived
                        break;
                    default:

                }
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                Log.d(TAG, "onCallStateChanged: CALL_STATE_OFFHOOK");
                switch(PREVIOUS_CALL_STATE) {
                    case TelephonyManager.CALL_STATE_IDLE:
                        //Outgoing call
                        break;
                    case TelephonyManager.CALL_STATE_RINGING:
                        //Answering call
                        break;
                    default:

                }
                break;
            default:
                Log.d(TAG, "onCallStateChanged: UNKNOWN_STATE: " + state);
                break;
        }

        PREVIOUS_CALL_STATE = state;
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
