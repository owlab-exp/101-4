package com.owlab.callblocker;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.CompoundButton;

import com.owlab.callblocker.service.CallBlockerIntentService;

/**
 * Created by ernest on 6/10/16.
 */
public class FUNS {

    public static void initializeApp(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        //initialize blocking on, if not
        //if(sharedPreferences.contains(context.getString(R.string.pref_key_blocking_on))) {
        //    //do nothing because it is already initialized
        //} else {
        //    sharedPreferences.edit().putBoolean(context.getString(R.string.pref_key_blocking_on), true).commit();
        //}

        //initialize settings to default values, if not before
        PreferenceManager.setDefaultValues(context, R.xml.settings, false);
    }

    /**
     *
     */
    public static class BlockingSwitchChangeListener implements CompoundButton.OnCheckedChangeListener {
        private Context context;

        public BlockingSwitchChangeListener(Context context) {
            this.context = context;
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
            if(checked) {
                CallBlockerIntentService.startActionBlockingOn(context);
            } else {
                CallBlockerIntentService.startActionBlockingOff(context);
            }
        }
    }

    /**
     *
     */
    public static class ShowBlockingNotificationIconPrefChangeListener implements Preference.OnPreferenceChangeListener {
        private static final String TAG = ShowBlockingNotificationIconPrefChangeListener.class.getSimpleName();

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Log.d(TAG, ">>> changed to " + newValue.toString());

            //returning true means changing the checkbox finally
            return true;
        }
    }

    /**
     *
     */
    public static class SuppressRingingPrefChangeListener implements Preference.OnPreferenceChangeListener {
        private static final String TAG = SuppressRingingPrefChangeListener.class.getSimpleName();

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Log.d(TAG, ">>> changed to " + newValue.toString());

            //returning true means changing the checkbox finally
            return true;
        }
    }

    /**
     *
     */
    public static class SuppressCallNotificationPrefChangeListener implements Preference.OnPreferenceChangeListener {
        private static final String TAG = SuppressCallNotificationPrefChangeListener.class.getSimpleName();

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Log.d(TAG, ">>> changed to " + newValue.toString());

            //returning true means changing the checkbox finally
            return true;
        }
    }

    /**
     *
     */
    public static class DismissCallPrefChangeListener implements Preference.OnPreferenceChangeListener {
        private static final String TAG = DismissCallPrefChangeListener.class.getSimpleName();

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Log.d(TAG, ">>> changed to " + newValue.toString());

            //returning true means changing the checkbox finally
            return true;
        }
    }
}
