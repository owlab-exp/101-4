package com.owlab.callquieter.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.owlab.callquieter.CONS;
import com.owlab.callquieter.MainActivity;
import com.owlab.callquieter.R;

import java.text.SimpleDateFormat;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - onContentChanged intent actions, extra parameters and static
 * helper methods.
 */
public class CallQuieterIntentService extends IntentService {
    public static final String TAG = CallQuieterIntentService.class.getSimpleName();

    private static final String ACTION_WHEN_BOOT_COMPLETED = "com.owlab.callquieter.service.action.WHEN_BOOT_COMPLETED";

    private static final String ACTION_QUIETER_ON = "com.owlab.callquieter.service.action.QUIETER_ON";
    private static final String ACTION_QUIETER_OFF = "com.owlab.callquieter.service.action.QUIETER_OFF";

    private static final String ACTION_STATUSBAR_NOTIFICATION_ON  = "com.owlab.callquieter.service.action.STATUSBAR_NOTIFICATION_ON";
    private static final String ACTION_STATUSBAR_NOTIFICATION_OFF = "com.owlab.callquieter.service.action.STATUSBAR_NOTIFICATION_OFF";

    private static final String ACTION_STATUSBAR_NOTIFICATION_COUNTER_UPDATE  = "com.owlab.callquieter.service.action.STATUSBAR_NOTIFICATION_COUNTER_UPDATE";
    private static final String ACTION_STATUSBAR_NOTIFICATION_COUNTER_CLEAR = "com.owlab.callquieter.service.action.STATUSBAR_NOTIFICATION_COUNTER_CLEAR";

    public CallQuieterIntentService() {
        super(TAG);
    }

    //Static methods for others
    public static void startActionWhenBootBootCompleted(Context context) {
        Intent intent = new Intent(context, CallQuieterIntentService.class);
        intent.setAction(ACTION_WHEN_BOOT_COMPLETED);
        context.startService(intent);
    }

    public static void startActionQuieterOn(Context context, ResultReceiver resultReceiver) {
        ////Log.d(TAG, "startActionQuieterOn called");
        Intent intent = new Intent(context, CallQuieterIntentService.class);
        intent.setAction(ACTION_QUIETER_ON);
        intent.putExtra("receiver", resultReceiver);
        context.startService(intent);
    }

    public static void startActionQuieterOff(Context context, ResultReceiver resultReceiver) {
        ////Log.d(TAG, "startActionQuieterOff called");
        Intent intent = new Intent(context, CallQuieterIntentService.class);
        intent.setAction(ACTION_QUIETER_OFF);
        intent.putExtra("receiver", resultReceiver);
        context.startService(intent);
    }

    public static void startActionStatusbarNotificationOn(Context context) {
        Intent intent = new Intent(context, CallQuieterIntentService.class);
        intent.setAction(ACTION_STATUSBAR_NOTIFICATION_ON);
        context.startService(intent);
    }

    public static void startActionStatusbarNotificationOff(Context context) {
        Intent intent = new Intent(context, CallQuieterIntentService.class);
        intent.setAction(ACTION_STATUSBAR_NOTIFICATION_OFF);
        context.startService(intent);
    }

    public static void startActionStatusbarNotificationCounterUpdate(Context context) {
        Intent intent = new Intent(context, CallQuieterIntentService.class);
        intent.setAction(ACTION_STATUSBAR_NOTIFICATION_COUNTER_UPDATE);
        context.startService(intent);
    }

    public static void startActionStatusbarNotificationCounterClear(Context context) {
        Intent intent = new Intent(context, CallQuieterIntentService.class);
        intent.setAction(ACTION_STATUSBAR_NOTIFICATION_COUNTER_CLEAR);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ////Log.d(TAG, ">>>>> handling intent action: " + intent.getAction().toString());

        if (intent == null) return;

        if(intent.getAction().equals(ACTION_WHEN_BOOT_COMPLETED)) {
            handleActionWhenBootCompleted();
        } else if (intent.getAction().equals(ACTION_QUIETER_ON)) {
            handleActionQuieterOn((ResultReceiver) intent.getParcelableExtra("receiver"));
        } else if (intent.getAction().equals(ACTION_STATUSBAR_NOTIFICATION_ON)) {
            //handleActionStatusbarNotificationOn(true);
            handleActionStatusbarNotificationOn();
        } else if (intent.getAction().equals(ACTION_STATUSBAR_NOTIFICATION_COUNTER_UPDATE)) {
            //handleActionStatusbarNotificationOn(true);
            handleActionStatusbarNotificationCounterUpdate();
        } else if (intent.getAction().equals(ACTION_STATUSBAR_NOTIFICATION_COUNTER_CLEAR)) {
            //handleActionStatusbarNotificationOn(true);
            handleActionStatusbarNotificationCounterClear();
        } else if (intent.getAction().equals(ACTION_STATUSBAR_NOTIFICATION_OFF)) {
            handleActionStatusbarNotificationOff();
        //} else if (intent.getAction().equals(ACTION_SUPPRESS_RINGING_ON)) {
        //    handleActionQuietRingerOn();
        //} else if (intent.getAction().equals(ACTION_SUPPRESS_RINGING_OFF)) {
        //    handleActionQuietRingerOff();
        //} else if (intent.getAction().equals(ACTION_SUPPRESS_CALL_NOTIFICATION_ON)) {
        //    handleActionSuppressCallNotificationOn();
        //} else if (intent.getAction().equals(ACTION_SUPPRESS_CALL_NOTIFICATION_OFF)) {
        //    handleActionSuppressCallNotificationOff();
        //} else if (intent.getAction().equals(ACTION_DISMISS_CALL_ON)) {
        //    handleActionDismissCallOn();
        //} else if (intent.getAction().equals(ACTION_DISMISS_CALL_OFF)) {
        //    handleActionDismissCallOff();
        //} else if (intent.getAction().equals(ACTION_SUPPRESS_HEADS_UP_NOTIFICATION_ON)) {
        //    handleActionSuppressHeadsUpNotificationOn();
        //} else if (intent.getAction().equals(ACTION_SUPPRESS_HEADS_UP_NOTIFICATION_OFF)) {
        //    handleActionSuppressHeadsUpNotificationOff();
        } else if (intent.getAction().equals(ACTION_QUIETER_OFF)) {
            handleActionQuieterOff((ResultReceiver)intent.getParcelableExtra("receiver"));
        }
    }

    //private void handleActionWhenBootCompleted(ResultReceiver resultReceiver) {
    private void handleActionWhenBootCompleted() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        // If the blocking is already on
        if(sharedPreferences.getBoolean(CONS.PREF_KEY_BLOCKING_ON, false)) {
            //handleActionStatusbarNotificationOn(false);
            //handleActionStatusbarNotificationOn();
            //resultReceiver.send(CONS.RESULT_SUCCESS, null);
            startActionQuieterOn(getBaseContext(), null);
            //startActionQuieterOn(getBaseContext(), new ResultReceiver(new Handler()) {

            //    @Override
            //    protected void onReceiveResult(int resultCode, Bundle resultData) {
            //        if(resultCode == CONS.RESULT_SUCCESS) {
            //            Toast.makeText(getBaseContext(), "CallQuieter started successfully", Toast.LENGTH_SHORT).show();
            //        } else {
            //            Toast.makeText(getBaseContext(), "CallQuieter could not start", Toast.LENGTH_SHORT).show();
            //        }
            //    }
            //});
        }
    }

    private void handleActionQuieterOn(ResultReceiver resultReceiver) {
        ////Log.d(TAG, "handleActionQuieterOn called");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        //// If the blocking is already on
        //if(sharedPreferences.getBoolean(CONS.PREF_KEY_BLOCKING_ON, false)) {
        //    resultReceiver.send(CONS.RESULT_FAIL, null);
        //    return;
        //}

        ////Log.d(TAG, "Start Call Quieter Service...");
        getBaseContext().startService(new Intent(getBaseContext(), CallQuieterService.class));

        sharedPreferences.edit().putBoolean(CONS.PREF_KEY_BLOCKING_ON, true).commit();
        //handleActionStatusbarNotificationOn(true);
        handleActionStatusbarNotificationOn();
        if(resultReceiver != null) {
            resultReceiver.send(CONS.RESULT_SUCCESS, null);
        }
    }

    private void handleActionQuieterOff(ResultReceiver resultReceiver) {
        ////Log.d(TAG, "handleActionQuieterOff called");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        //// If the blocking is already off
        //if(!sharedPreferences.getBoolean(CONS.PREF_KEY_BLOCKING_ON, false)) {
        //    resultReceiver.send(CONS.RESULT_FAIL, null);
        //    return;
        //}

        ////Log.d(TAG, "Stop Call Quieter Service...");

        getBaseContext().stopService(new Intent(getBaseContext(), CallQuieterService.class));

        sharedPreferences.edit().putBoolean(CONS.PREF_KEY_BLOCKING_ON, false).commit();
        // at last off the blocking notification icon
        handleActionStatusbarNotificationOff();
        resultReceiver.send(CONS.RESULT_SUCCESS, null);

    }

    //private void handleActionStatusbarNotificationOn(boolean checkStatus) {
    private void handleActionStatusbarNotificationOn() {
        //////Log.d(TAG, ">>>>> handleActionStatusbarNotificationOn called");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        //If the blocking is not on state, then do not show
        if(!sharedPreferences.getBoolean(CONS.PREF_KEY_BLOCKING_ON, false)) {
            return;
        }

        //If the notification is disabled then return
        if (!sharedPreferences.getBoolean(getString(R.string.settings_key_show_app_notification_icon), false)) {
            ////Log.d(TAG, ">>>>> show notification icon disabled");
            return;
        }

        //Sometimes the preference is not updated...when app is destroyed without calling notification off
        //But nonetheless the notification will be create or updated by explicit "notify"!
        //Thus here this routine can be omitted.
        //If the notification is already on then return
        //if(checkStatus && sharedPreferences.getBoolean(getString(R.string.status_key_notification_icon_shown), false)) {
        //      return;
        //}

        //Otherwise show notification icon
        Intent startServiceIntent = new Intent(getBaseContext(), CallQuieterIntentService.class);
        startServiceIntent.setAction(ACTION_STATUSBAR_NOTIFICATION_COUNTER_CLEAR);
        PendingIntent clearCountPendingIntent = PendingIntent.getService(getBaseContext(), 0, startServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent openMainActivityIntent = new Intent(getApplication(), MainActivity.class);
        openMainActivityIntent.setAction("OPEN_QUIETED_CALL_LOG");
        openMainActivityIntent.putExtra("pageNo", 1);

        int count = sharedPreferences.getInt(getString(R.string.status_key_notification_count), 0);
        long since = sharedPreferences.getLong(getString(R.string.status_key_notification_count_since), 0l);
        if(since == 0l) {
            since = System.currentTimeMillis();
            sharedPreferences.edit().putLong(getString(R.string.status_key_notification_count_since), since).commit();
        }
        SimpleDateFormat sdf = new SimpleDateFormat();
        String sinceDate = sdf.format(since);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                //.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_statusbar))
                .setSmallIcon(R.drawable.ic_statusbar)
                .setContentTitle(getString(R.string.app_name) + " running")
                .setContentText(String.valueOf(count) + (count == 0 || count == 1 ? " call" : " calls")  + " quieted since " + sinceDate)
                //.addAction(R.drawable.ic_clear_24, "Clear count", clearCountPendingIntent)
                .addAction(R.drawable.ic_refresh_white_24dp, "RESET COUNT", clearCountPendingIntent)
                .setOngoing(true)
                .setContentIntent(PendingIntent.getActivity(getApplication(), 0, openMainActivityIntent, 0));

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

    private void handleActionStatusbarNotificationCounterUpdate() {
        handleActionStatusbarNotificationOn();
    }

    private void handleActionStatusbarNotificationCounterClear() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        sharedPreferences.edit().putInt(getString(R.string.status_key_notification_count), 0).commit();

        //Also initialize since date
        sharedPreferences.edit().putLong(getString(R.string.status_key_notification_count_since), System.currentTimeMillis()).commit();

        handleActionStatusbarNotificationOn();
    }

    //private void handleActionQuietRingerOn() {}
    //private void handleActionQuietRingerOff() {}
    //private void handleActionSuppressCallNotificationOn() {}
    //private void handleActionSuppressCallNotificationOff() {}
    //private void handleActionDismissCallOn() {}
    //private void handleActionDismissCallOff() {}
    //private void handleActionSuppressHeadsUpNotificationOn() {}
    //private void handleActionSuppressHeadsUpNotificationOff() {}

}
