package com.owlab.callblocker.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;

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
        Log.d(TAG, ">>>>> onCreate called");
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

    //@Override
    //public void onViewCreated(View view, Bundle savedInstanceState) {
    //    ((MainActivity)getActivity()).changeActionBarContent("Settings");
    //}

    //@Override
    //public void onPause() {
    //    super.onPause();
    //    ((MainActivity)getActivity()).restoreActionBar();
    //}

    //@Override
    //public void onResume() {
    //    super.onResume();
    //    ((MainActivity)getActivity()).changeActionBarContent("Settings");
    //}

    Context parentContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, ">>>>> attached");

        parentContext = context;

        if(parentContext instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity)parentContext;
            //mainActivity.changeActionBarContent("Settings");
            android.support.v7.app.ActionBar mainActionBar =  mainActivity.getSupportActionBar();
            if(mainActionBar != null) {
                mainActionBar.setTitle("Settings");
                mainActionBar.setDisplayHomeAsUpEnabled(true);

                Menu mainMenu = mainActivity.getMenu();
                if (mainMenu != null) {
                    //mainMenu.findItem(R.id.menuitem_main_onoff_switch_layout).getActionView().findViewById(R.id.action_main_onoff_switch).setVisibility(View.INVISIBLE);
                    //mainMenu.findItem(R.id.menuitem_settings).setVisible(false);
                    mainMenu.findItem(R.id.menuitem_settings).setEnabled(false);
                }
            }
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, ">>>>> detached");

        if(parentContext != null && parentContext instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) parentContext;
            android.support.v7.app.ActionBar mainActionBar =  mainActivity.getSupportActionBar();
            if(mainActionBar != null) {
                mainActionBar.setTitle(R.string.app_name);
                mainActionBar.setDisplayHomeAsUpEnabled(false);

                //Regenerate menu
                mainActivity.invalidateOptionsMenu();
                //Menu mainMenu = mainActivity.getMenu();
                //if (mainMenu != null) {
                //    //mainMenu.findItem(R.id.menuitem_main_onoff_switch_layout).getActionView().findViewById(R.id.action_main_onoff_switch).setVisibility(View.INVISIBLE);
                //    //mainMenu.findItem(R.id.menuitem_settings).setVisible(true);
                //    mainMenu.findItem(R.id.menuitem_settings).setEnabled(true);
                //}
            }
        }

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
