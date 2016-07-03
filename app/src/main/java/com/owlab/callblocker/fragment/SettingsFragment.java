package com.owlab.callblocker.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.owlab.callblocker.FUNS;
import com.owlab.callblocker.MainActivity;
import com.owlab.callblocker.R;
import com.owlab.callblocker.custom.SpinnerPreference;
import com.owlab.callblocker.custom.SpinnerPreferenceDialogFragmentCompat;

import java.util.Locale;
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

        SpinnerPreference selectCountryPref = (SpinnerPreference) getPreferenceManager().findPreference(getString(R.string.settings_key_country_and_code));
        selectCountryPref.setDialogTitle("Select a country");
        String countryAndCode = selectCountryPref.getText();
        if(TextUtils.isEmpty(countryAndCode)) {
            Locale locale = Locale.getDefault();
            String countryNameNative = locale.getDisplayCountry(locale);
            String countryName = locale.getDisplayCountry();
            String countryNameComposite = countryName + "(" + countryNameNative + ")";
            //selectCountryPref.setSummary(locale.getDisplayCountry(locale));
            selectCountryPref.setSummary(countryNameComposite);
        } else {
            selectCountryPref.setSummary(countryAndCode.split(":")[0]);
        }
        //selectCountryPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
        //    @Override
        //    public boolean onPreferenceChange(Preference preference, Object o) {
        //        Log.d(TAG, ">>>>> value: "  + o.toString());
        //        return true;
        //    }
        //});

        SwitchPreference blockHiddenNumberPref = (SwitchPreference) getPreferenceManager().findPreference(getString(R.string.settings_key_block_hidden_number));
        blockHiddenNumberPref.setOnPreferenceChangeListener(new FUNS.BlockHiddenNumberPrefChangeListener(getActivity()));

        SwitchPreference blockUnknownNumberPref = (SwitchPreference) getPreferenceManager().findPreference(getString(R.string.settings_key_block_unknown_number));
        blockUnknownNumberPref.setOnPreferenceChangeListener(new FUNS.BlockUnknownNumberPrefChangeListener(getActivity()));

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
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if(preference instanceof SpinnerPreference) {
            //Please refer to
            //android.framework.support.v7.preference.src.android.support.v7.preference.PreferenceFragmentCompat
            DialogFragment fragment = SpinnerPreferenceDialogFragmentCompat.newInstance(preference.getKey());
            fragment.setTargetFragment(this, 0);
            fragment.show(getFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
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

        if(preference instanceof SwitchPreference) {
            SwitchPreference switchPreference = (SwitchPreference) preference;
            switchPreference.setChecked(sharedPreferences.getBoolean(key, false));
        }

        if(preference instanceof SpinnerPreference) {
            String countryAndCode = sharedPreferences.getString(key, "");
            if(!countryAndCode.isEmpty()) {
                preference.setSummary(countryAndCode.split(":")[0]); // country's display name

            }
        }
    }
}
