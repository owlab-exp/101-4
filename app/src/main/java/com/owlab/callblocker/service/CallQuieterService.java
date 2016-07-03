package com.owlab.callblocker.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.owlab.callblocker.contentobserver.CallQuieterContentObserver;
import com.owlab.callblocker.contentprovider.CallBlockerProvider;
import com.owlab.callblocker.listener.CallQuieterPhoneStateListener;

/**
 * Created by ernest on 7/2/16.
 */
public class CallQuieterService extends Service {
    private static final String TAG = CallQuieterService.class.getSimpleName();

    private TelephonyManager telManager;
    private CallQuieterPhoneStateListener phoneStateListener;
    private CallQuieterContentObserver callQuieterContentObserver;

    private boolean started = false;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, ">>>>>> intent action: " + (intent != null ? intent.getAction() : "intent is null"));

        if(!started) {
            Log.d(TAG, ">>>>> Registering PHONE STATE LISTENER ...");
            telManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            phoneStateListener = new CallQuieterPhoneStateListener(getBaseContext());
            callQuieterContentObserver = new CallQuieterContentObserver(new Handler(), phoneStateListener);

            getContentResolver().registerContentObserver(CallBlockerProvider.BLOCKED_NUMBER_URI, true, callQuieterContentObserver);
            telManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

            started = true;
        } else {
            Log.d(TAG, ">>>>> Call quieter service already started");
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, ">>>>> onDestroy called");

        Log.d(TAG, ">>>>> Unregistering PHONE STATE LISTENER ...");
        getContentResolver().unregisterContentObserver(callQuieterContentObserver);
        telManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
    }
}
