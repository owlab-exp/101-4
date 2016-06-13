package com.owlab.callblocker.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.owlab.callblocker.FUNS;
import com.owlab.callblocker.MainActivity;
import com.owlab.callblocker.R;

/**
 * Created by ernest on 6/5/16.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set contents
        addPreferencesFromResource(R.xml.settings);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        CheckBoxPreference showNotificationIcon = (CheckBoxPreference) getPreferenceManager().findPreference(getString(R.string.settings_key_show_app_notification_icon));
        showNotificationIcon.setOnPreferenceChangeListener(new FUNS.ShowBlockingNotificationIconPrefChangeListener(getActivity()));

        CheckBoxPreference suppressRingingPref = (CheckBoxPreference) getPreferenceManager().findPreference(getString(R.string.settings_key_suppress_ringing));
        suppressRingingPref.setOnPreferenceChangeListener(new FUNS.SuppressRingingPrefChangeListener(getActivity()));

        //CheckBoxPreference suppressCallNotificationPref = (CheckBoxPreference) getPreferenceManager().findPreference(getString(R.string.settings_key_suppress_call_notification));
        //suppressCallNotificationPref.setOnPreferenceChangeListener(new FUNS.SuppressCallNotificationPrefChangeListener());

        CheckBoxPreference dismissCallPref = (CheckBoxPreference) getPreferenceManager().findPreference(getString(R.string.settings_key_dismiss_call));
        dismissCallPref.setOnPreferenceChangeListener(new FUNS.DismissCallPrefChangeListener(getActivity()));

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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, ">>>>> preference changed, key: " + key);
        Preference preference = findPreference(key);
        if(preference instanceof CheckBoxPreference) {
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference)preference;
            checkBoxPreference.setChecked(sharedPreferences.getBoolean(key, false));
        }
    }
}
