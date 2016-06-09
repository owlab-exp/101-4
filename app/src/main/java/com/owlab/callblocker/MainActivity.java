package com.owlab.callblocker;

import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.owlab.callblocker.service.CallBlockerIntentService;

/**
 * Top most setting element is "SERVICE ON/OFF"
 * - If service off then no filtering activity occurs and no notification icon of this app enabled
 * - If service on then filtering occurs
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getFragmentManager().beginTransaction().add(R.id.fragment_container, new PhoneListFragment()).commit();

        // set default setting values, if not initialized
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        //TODO delete
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        for(String key: sharedPreferences.getAll().keySet()) {
            Log.d(TAG, ">>>>> key: " + key);
        }
    }

    private Menu menu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        MenuItem mainOnOffSwitchLayout = menu.findItem(R.id.menuitem_main_onoff_switch_layout);
        Switch mainOnOffSwitch = (Switch) mainOnOffSwitchLayout.getActionView().findViewById(R.id.action_main_onoff_switch);

        mainOnOffSwitch.setChecked(sharedPreferences.getBoolean(Constant.PREF_KEY_SERVICE_ON, false));
        mainOnOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if(checked) {
                    CallBlockerIntentService.startActionBlockingOn(MainActivity.this);
                } else {
                    CallBlockerIntentService.stopActionBlockingOff(MainActivity.this);
                }
                sharedPreferences.edit().putBoolean(Constant.PREF_KEY_SERVICE_ON, checked).commit();
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.menuitem_settings:
                SettingsFragment settingsFragment = (SettingsFragment) getFragmentManager().findFragmentByTag("SETTINGS_FRAGMENT");
                if(settingsFragment == null || !settingsFragment.isVisible()) {
                    getFragmentManager().beginTransaction()
                            .addToBackStack("PhoneListFragment")
                            .replace(R.id.fragment_container, new SettingsFragment(), "SETTINGS_FRAGMENT")
                            .commit();
                    return true;
                }
                return false;
            case android.R.id.home:
                getFragmentManager().popBackStack("PhoneListFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new PhoneListFragment())
                        .commit();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void changeActionBar(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (menu != null) {
            menu.findItem(R.id.menuitem_main_onoff_switch_layout).getActionView().findViewById(R.id.action_main_onoff_switch).setVisibility(View.INVISIBLE);
            menu.findItem(R.id.menuitem_settings).setVisible(false);
        }
    }

    public void restoreActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        if(menu != null) {
            menu.findItem(R.id.menuitem_main_onoff_switch_layout).getActionView().findViewById(R.id.action_main_onoff_switch).setVisibility(View.VISIBLE);
            menu.findItem(R.id.menuitem_settings).setVisible(true);
        }
    }
}
