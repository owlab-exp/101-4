package com.owlab.callblocker.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.owlab.callblocker.listener.CallQuieterPhoneStateListener;

/**
 * Created by ernest on 7/2/16.
 */
public class CallQuieterService extends Service {
    private static final String TAG = CallQuieterService.class.getSimpleName();

    TelephonyManager telManager;
    CallQuieterPhoneStateListener phoneStateListener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, ">>>>> onStartCommand called");


        Log.d(TAG, ">>>>> Registering PHONE STATE LISTENERd...");
        telManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        phoneStateListener = new CallQuieterPhoneStateListener(getBaseContext());

        telManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, ">>>>> onDestroy called");

        Log.d(TAG, ">>>>> Unregistering PHONE STATE LISTENERd...");
        telManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);

    }
}
