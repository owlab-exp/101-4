package com.owlab.callquieter.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CallLog;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.owlab.callquieter.contentobserver.CallLogObserver;

/**
 * Created by ernest on 6/13/16.
 */
public class CallLogObserverStartService extends Service {
    private static final String TAG = CallLogObserverStartService.class.getSimpleName();

    private CallLogObserver callLogObserver;

    @Override
    public void onCreate() {
        Log.d(TAG, ">>>>> creating...");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, ">>>>> starting...");

        //callLogObserver = new CallLogObserver(new Handler(), getBaseContext());
        //String phoneNumber = intent.getExtras().getString(CONS.INTENT_KEY_PHONE_NUMBER);
        //long timeFrom = intent.getExtras().getLong(CONS.INTENT_KEY_TIME_FROM);
        //boolean delete = intent.getExtras().getBoolean(CONS.INTENT_KEY_SHOULD_DELETE);
        //callLogObserver = new CallLogObserver(new Handler(), getBaseContext(), this, phoneNumber, timeFrom, delete);
        callLogObserver = new CallLogObserver(new Handler(), this, intent.getExtras());
        Log.d(TAG, ">>>>> Uri to be registered: " + CallLog.Calls.CONTENT_URI);
        getBaseContext().getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true, callLogObserver);
        Toast.makeText(getBaseContext(), "Call log cleansing service started", Toast.LENGTH_SHORT).show();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, ">>>>> destroying...");

        getBaseContext().getContentResolver().unregisterContentObserver(callLogObserver);
        callLogObserver = null;
        Toast.makeText(getBaseContext(), "Call log cleansing service finished", Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
