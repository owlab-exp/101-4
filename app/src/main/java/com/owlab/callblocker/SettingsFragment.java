package com.owlab.callblocker;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by ernest on 6/5/16.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String previousTitle = ((MainActivity)getActivity()).changeActionBar("Settings");
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity)getActivity()).restoreActionBar();
    }

}
