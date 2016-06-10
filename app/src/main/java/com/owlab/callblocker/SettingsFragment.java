package com.owlab.callblocker;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.view.View;

/**
 * Created by ernest on 6/5/16.
 */
public class SettingsFragment extends PreferenceFragment {
    public static final String TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set contents
        addPreferencesFromResource(R.xml.settings);



        CheckBoxPreference showNotificationIcon = (CheckBoxPreference) getPreferenceManager().findPreference(getString(R.string.settings_key_show_notification_icon));
        showNotificationIcon.setOnPreferenceChangeListener(new FUNS.ShowBlockingNotificationIconPrefChangeListener());

        CheckBoxPreference suppressRingingPref = (CheckBoxPreference) getPreferenceManager().findPreference(getString(R.string.settings_key_suppress_ringing));
        suppressRingingPref.setOnPreferenceChangeListener(new FUNS.SuppressRingingPrefChangeListener());

        CheckBoxPreference suppressCallNotificationPref = (CheckBoxPreference) getPreferenceManager().findPreference(getString(R.string.settings_key_suppress_call_notification));
        suppressCallNotificationPref.setOnPreferenceChangeListener(new FUNS.SuppressCallNotificationPrefChangeListener());

        CheckBoxPreference dismissCallPref = (CheckBoxPreference) getPreferenceManager().findPreference(getString(R.string.settings_key_dismiss_call));
        dismissCallPref.setOnPreferenceChangeListener(new FUNS.DismissCallPrefChangeListener());
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
