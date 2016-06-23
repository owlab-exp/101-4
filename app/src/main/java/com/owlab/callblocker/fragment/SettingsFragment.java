package com.owlab.callblocker.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.owlab.callblocker.FUNS;
import com.owlab.callblocker.MainActivity;
import com.owlab.callblocker.R;

import java.util.Objects;

/**
 * Created by ernest on 6/5/16.
 */
//public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, ">>>>> onCreate called with savedInstanceState: " + Objects.toString(savedInstanceState));
        // set contents
        addPreferencesFromResource(R.xml.settings);

        CheckBoxPreference showNotificationIcon = (CheckBoxPreference) getPreferenceManager().findPreference(getString(R.string.settings_key_show_app_notification_icon));
        showNotificationIcon.setOnPreferenceChangeListener(new FUNS.ShowBlockingNotificationIconPrefChangeListener(getActivity()));

        CheckBoxPreference suppressRingingPref = (CheckBoxPreference) getPreferenceManager().findPreference(getString(R.string.settings_key_suppress_ringing));
        suppressRingingPref.setOnPreferenceChangeListener(new FUNS.SuppressRingingPrefChangeListener(getActivity()));

        //CheckBoxPreference suppressCallNotificationPref = (CheckBoxPreference) getPreferenceManager().findPreference(getString(R.string.settings_key_suppress_call_notification));
        //suppressCallNotificationPref.setOnPreferenceChangeListener(new FUNS.SuppressCallNotificationPrefChangeListener());

        CheckBoxPreference dismissCallPref = (CheckBoxPreference) getPreferenceManager().findPreference(getString(R.string.settings_key_dismiss_call));
        dismissCallPref.setOnPreferenceChangeListener(new FUNS.DismissCallPrefChangeListener(getActivity()));

        CheckBoxPreference deleteCallLogPref = (CheckBoxPreference) getPreferenceManager().findPreference(getString(R.string.settings_key_delete_call_log));
        deleteCallLogPref.setOnPreferenceChangeListener(new FUNS.DeleteCallLogPrefChangeListener(getActivity()));
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        Log.d(TAG, ">>>>> onCreatePreferences called");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, ">>>>> onResume called");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        MainActivity mainActivity = (MainActivity)getActivity();
        ActionBar mainActionBar = mainActivity.getSupportActionBar();
        if(mainActionBar != null) {
            mainActionBar.setTitle(R.string.title_settings);
            mainActionBar.setDisplayHomeAsUpEnabled(true);
            Menu mainMenu = mainActivity.getMenu();
            if (mainMenu != null) {
                //mainMenu.findItem(R.id.menuitem_main_onoff_switch_layout).getActionView().findViewById(R.id.action_main_onoff_switch).setVisibility(View.INVISIBLE);
                //mainMenu.findItem(R.id.menuitem_settings).setVisible(false);
                mainMenu.findItem(R.id.menuitem_settings).setEnabled(false);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, ">>>>> onPause called");

        //Unregister preferencechangelistener
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
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
