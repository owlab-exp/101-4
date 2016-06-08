package com.owlab.callblocker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.owlab.callblocker.Constant;
import com.owlab.callblocker.service.CallBlockerIntentService;

/**
 * Created by ernest on 6/7/16.
 */
public class BootCompletedIntentReceiver extends BroadcastReceiver {
    private static final String TAG = BootCompletedIntentReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        //if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
        //}
        Log.d(TAG, ">>>>> boot completed intent received");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if(sharedPreferences.getBoolean(Constant.PREF_KEY_SERVICE_ON, false)) {
            //TODO start the call blocker service, if it is ON
            CallBlockerIntentService.startService(context);
        }
    }
}
