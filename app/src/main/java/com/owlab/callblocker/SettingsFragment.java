package com.owlab.callblocker;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.View;

/**
 * Created by ernest on 6/5/16.
 */
public class SettingsFragment extends PreferenceFragment {
    public static final String TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        CheckBoxPreference blockingNotificationOn = (CheckBoxPreference) getPreferenceManager().findPreference("pref_key_blocking_notification_on");
        blockingNotificationOn.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.d(TAG, ">>>>> " + preference.getKey() + " changed to " + newValue.toString());
                return true;
            }
        });

        //CheckBoxPreference suppressRinger = (CheckBoxPreference) getPreferenceManager().findPreference("pref_key_suppress_ringer");
        //suppressRinger.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
        //    @Override
        //    public boolean onPreferenceChange(Preference preference, Object newValue) {
        //        Log.d(TAG, ">>>>> " + preference.getKey() + " changed to " + newValue.toString());
        //        return true;
        //    }
        //});
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ((MainActivity)getActivity()).changeActionBar("Settings");
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity)getActivity()).restoreActionBar();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).changeActionBar("Settings");
    }
}
