package com.owlab.callblocker;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.View;

/**
 * Created by ernest on 6/5/16.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
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
