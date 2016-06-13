package com.owlab.callblocker.service;

import android.Manifest;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.owlab.callblocker.CONS;
import com.owlab.callblocker.MainActivity;
import com.owlab.callblocker.R;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class CallBlockerIntentService extends IntentService {
    public static final String TAG = CallBlockerIntentService.class.getSimpleName();

    private static final String ACTION_BLOCKING_ON = "com.owlab.callblocker.service.action.BLOCKING_ON";
    private static final String ACTION_BLOCKING_OFF = "com.owlab.callblocker.service.action.BLOCKING_OFF";

    private static final String ACTION_STATUSBAR_NOTIFICATION_ON  = "com.owlab.callblocker.service.action.STATUSBAR_NOTIFICATION_ON";
    private static final String ACTION_STATUSBAR_NOTIFICATION_OFF = "com.owlab.callblocker.service.action.STATUSBAR_NOTIFICATION_OFF";

    private static final String ACTION_SUPPRESS_RINGING_ON  = "com.owlab.callblocker.service.action.SUPPRESS_RINGING_ON";
    private static final String ACTION_SUPPRESS_RINGING_OFF = "com.owlab.callblocker.service.action.SUPPRESS_RINGING_OFF";

    private static final String ACTION_SUPPRESS_CALL_NOTIFICATION_ON  = "com.owlab.callblocker.service.action.SUPPRESS_CALL_NOTIFICATION_ON";
    private static final String ACTION_SUPPRESS_CALL_NOTIFICATION_OFF = "com.owlab.callblocker.service.action.SUPPRESS_CALL_NOTIFICATION_OFF";

    private static final String ACTION_DISMISS_CALL_ON  = "com.owlab.callblocker.service.action.DISMISS_CALL_ON";
    private static final String ACTION_DISMISS_CALL_OFF = "com.owlab.callblocker.service.action.DISMISS_CALL_OFF";

    private static final String ACTION_SUPPRESS_HEADS_UP_NOTIFICATION_ON  = "com.owlab.callblocker.service.action.SUPPRESS_HEADS_UP_NOTIFICATION_ON";
    private static final String ACTION_SUPPRESS_HEADS_UP_NOTIFICATION_OFF = "com.owlab.callblocker.service.action.SUPPRESS_HEADS_UP_NOTIFICATION_OFF";

    //private static boolean isStarted = false;
    //private static boolean statusbarNotificationOn = false;

    public CallBlockerIntentService() {
        super(TAG);
    }

    public static void startActionBlockingOn(Context context, ResultReceiver resultReceiver) {
        Intent intent = new Intent(context, CallBlockerIntentService.class);
        intent.setAction(ACTION_BLOCKING_ON);
        intent.putExtra("receiver", resultReceiver);
        context.startService(intent);
    }

    public static void startActionBlockingOff(Context context, ResultReceiver resultReceiver) {
        Intent intent = new Intent(context, CallBlockerIntentService.class);
        intent.setAction(ACTION_BLOCKING_OFF);
        intent.putExtra("receiver", resultReceiver);
        context.startService(intent);
    }

    public static void startActionStatusbarNotificationOn(Context context) {
        Intent intent = new Intent(context, CallBlockerIntentService.class);
        intent.setAction(ACTION_STATUSBAR_NOTIFICATION_ON);
        context.startService(intent);
    }

    public static void startActionStatusbarNotificationOff(Context context) {
        Intent intent = new Intent(context, CallBlockerIntentService.class);
        intent.setAction(ACTION_STATUSBAR_NOTIFICATION_OFF);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, ">>>>> handling intent action: " + intent.getAction().toString());

        if (intent == null) return;

        if (intent.getAction().equals(ACTION_BLOCKING_ON)) {
            handleActionBlockingOn((ResultReceiver) intent.getParcelableExtra("receiver"));
        } else if (intent.getAction().equals(ACTION_STATUSBAR_NOTIFICATION_ON)) {
            handleActionStatusbarNotificationOn();
        } else if (intent.getAction().equals(ACTION_STATUSBAR_NOTIFICATION_OFF)) {
            handleActionStatusbarNotificationOff();
        } else if (intent.getAction().equals(ACTION_SUPPRESS_RINGING_ON)) {
            handleActionQuietRingerOn();
        } else if (intent.getAction().equals(ACTION_SUPPRESS_RINGING_OFF)) {
            handleActionQuietRingerOff();
        } else if (intent.getAction().equals(ACTION_SUPPRESS_CALL_NOTIFICATION_ON)) {
            handleActionSuppressCallNotificationOn();
        } else if (intent.getAction().equals(ACTION_SUPPRESS_CALL_NOTIFICATION_OFF)) {
            handleActionSuppressCallNotificationOff();
        } else if (intent.getAction().equals(ACTION_DISMISS_CALL_ON)) {
            handleActionDismissCallOn();
        } else if (intent.getAction().equals(ACTION_DISMISS_CALL_OFF)) {
            handleActionDismissCallOff();
        } else if (intent.getAction().equals(ACTION_SUPPRESS_HEADS_UP_NOTIFICATION_ON)) {
            handleActionSuppressHeadsUpNotificationOn();
        } else if (intent.getAction().equals(ACTION_SUPPRESS_HEADS_UP_NOTIFICATION_OFF)) {
            handleActionSuppressHeadsUpNotificationOff();
        } else if (intent.getAction().equals(ACTION_BLOCKING_OFF)) {
            handleActionBlockingOff((ResultReceiver)intent.getParcelableExtra("receiver"));
        }
    }

    private void handleActionBlockingOn(ResultReceiver resultReceiver) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        // If the blocking is already on
        if(sharedPreferences.getBoolean(getString(R.string.pref_key_blocking_on), false)) {
            resultReceiver.send(CONS.RESULT_FAIL, null);
            return;
        }

        sharedPreferences.edit().putBoolean(getString(R.string.pref_key_blocking_on), true).commit();
        //if(sharedPreferences.getBoolean(getString(R.string.status_key_phone_state_receiver_registered), false)) {
        //    //already registered!
        //    return;
        //}
        //// register broadcast receiver,
        //IntentFilter intentFilter = new IntentFilter();
        //intentFilter.addAction("android.intent.action.PHONE_STATE");
        ////PHONE_STATE is not ordered broadcast, therefore has no priority
        ////intentFilter.setPriority(....);
        //PhoneCallFilter phoneStateReceiver = new PhoneCallFilter();
        //registerReceiver(phoneStateReceiver, intentFilter);

        // Save state
        //if(!sharedPreferences.getBoolean(getString(R.string.status_key_phone_state_receiver_registered), false)) {
        //sharedPreferences.edit().putBoolean(getString(R.string.status_key_phone_state_receiver_registered), true).commit();
        //}

        if(sharedPreferences.getBoolean(getString(R.string.settings_key_delete_call_log), false) &&
                ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED
                ) {
            //Register call log deleter
            startService(new Intent(this, CallLogCleansingService.class));
        }

        // at last show blocking notification icon
        handleActionStatusbarNotificationOn();
        resultReceiver.send(CONS.RESULT_SUCCESS, null);
    }

    private void handleActionBlockingOff(ResultReceiver resultReceiver) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        // If the blocking is already off
        if(!sharedPreferences.getBoolean(getString(R.string.pref_key_blocking_on), false)) {
            resultReceiver.send(CONS.RESULT_FAIL, null);
            return;
        }

        sharedPreferences.edit().putBoolean(getString(R.string.pref_key_blocking_on), false).commit();
        //if(!sharedPreferences.getBoolean(getString(R.string.status_key_phone_state_receiver_registered), false)) {
        //    //not registered!
        //    return;
        //}

        //// unregister broadcast receiver, but incorrect code!!! -> declare in the manifest file
        //PhoneCallFilter phoneStateReceiver = new PhoneCallFilter();
        //unregisterReceiver(phoneStateReceiver);

        // Save state
        //if(!sharedPreferences.getBoolean(getString(R.string.status_key_phone_state_receiver_registered), false)) {
        //sharedPreferences.edit().putBoolean(getString(R.string.status_key_phone_state_receiver_registered), false).commit();
        //}

        //Unregister call log deleter
        //getBaseContext().getContentResolver().unregisterContentObserver(new CallLogObserver(new Handler(), getBaseContext()));
        if(sharedPreferences.getBoolean(getString(R.string.settings_key_delete_call_log), false)) {
            stopService(new Intent(this, CallLogCleansingService.class));
        }

        // at last off the blocking notification icon
        handleActionStatusbarNotificationOff();
        resultReceiver.send(CONS.RESULT_SUCCESS, null);
    }

    private void handleActionStatusbarNotificationOn() {
        //Log.d(TAG, ">>>>> handleActionStatusbarNotificationOn called");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        //If the notification is disabled then return
        if (!sharedPreferences.getBoolean(getString(R.string.settings_key_show_app_notification_icon), false)) {
            Log.d(TAG, ">>>>> show notification icon disabled");
            return;
        }

        //If the notification is already on then return
        if(sharedPreferences.getBoolean(getString(R.string.status_key_notification_icon_shown), false)) {
            return;
        }

        //If the blocking is not on state, then do not show
        if(!sharedPreferences.getBoolean(getString(R.string.pref_key_blocking_on), false)) {
            return;
        }

        //Otherwise show notification icon
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_call_blocker_48)
                .setContentTitle("CallBlocker")
                .setOngoing(true)
                .setContentIntent(PendingIntent.getActivity(getApplication(), 0, new Intent(getApplication(), MainActivity.class), 0));
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(CONS.STATUSBAR_NOTIFICATION_ID, notificationBuilder.build());

        //Write status
        sharedPreferences.edit().putBoolean(getString(R.string.status_key_notification_icon_shown), true).commit();
    }

    private void handleActionStatusbarNotificationOff() {
        //If the notification is not turned on, return
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if(!sharedPreferences.getBoolean(getString(R.string.status_key_notification_icon_shown), false)) {
            return;
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(CONS.STATUSBAR_NOTIFICATION_ID);

        //write status
        sharedPreferences.edit().putBoolean(getString(R.string.status_key_notification_icon_shown), false).commit();
    }

    private void handleActionQuietRingerOn() {}
    private void handleActionQuietRingerOff() {}
    private void handleActionSuppressCallNotificationOn() {}
    private void handleActionSuppressCallNotificationOff() {}
    private void handleActionDismissCallOn() {}
    private void handleActionDismissCallOff() {}
    private void handleActionSuppressHeadsUpNotificationOn() {}
    private void handleActionSuppressHeadsUpNotificationOff() {}

}
