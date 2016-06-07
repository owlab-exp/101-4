package com.owlab.callblocker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by ernest on 6/7/16.
 */
public class BootCompletedIntentReceiver extends BroadcastReceiver {
    private static final String TAG = BootCompletedIntentReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        //if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
        //    //TODO start the call blocker service, if it is ON
        //}
        Log.d(TAG, ">>>>> boot completed intent received");
    }
}
