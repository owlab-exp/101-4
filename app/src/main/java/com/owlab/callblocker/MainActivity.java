package com.owlab.callblocker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Switch;

import com.owlab.callblocker.fragment.CallLogFragment;
import com.owlab.callblocker.fragment.PhoneListFragment;
import com.owlab.callblocker.fragment.SettingsFragment;

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

        //Initialize app
        FUNS.initializeApp(this);


        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Intent intent = getIntent();
        String fragment = intent.getStringExtra(CONS.INTENT_KEY_TARGET_FRAGMENT);
        //getFragmentManager().beginTransaction().replace(R.id.fragment_container, new PhoneListFragment(), "PHONE_LIST_FRAGMENT").commit();
        if(intent != null && fragment != null && fragment.equals(CONS.FRAGMENT_CALL_LOG)) {
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, new CallLogFragment(), CONS.FRAGMENT_CALL_LOG).commit();
        } else {
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, new PhoneListFragment(), CONS.FRAGMENT_PHONE_LIST).commit();
        }
    }

    private Menu menu;

    public Menu getMenu() {
        return this.menu;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        MenuItem mainOnOffSwitchLayout = menu.findItem(R.id.menuitem_main_onoff_switch_layout);
        Switch mainOnOffSwitch = (Switch) mainOnOffSwitchLayout.getActionView().findViewById(R.id.action_main_onoff_switch);

        mainOnOffSwitch.setChecked(sharedPreferences.getBoolean(getString(R.string.pref_key_blocking_on), false));
        mainOnOffSwitch.setOnCheckedChangeListener(new FUNS.BlockingSwitchChangeListener(this));

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
                SettingsFragment settingsFragment = (SettingsFragment) getFragmentManager().findFragmentByTag(CONS.FRAGMENT_SETTINGS);
                if (settingsFragment == null) {
                    settingsFragment = new SettingsFragment();

                }
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, settingsFragment, CONS.FRAGMENT_SETTINGS)
                        .addToBackStack(null)
                        .commit();
                return true;
            case android.R.id.home:
                getFragmentManager().popBackStack();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //delegate
        FUNS.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    public void setMainOnOffSwitch(boolean checked) {
        MenuItem mainOnOffSwitchLayout = menu.findItem(R.id.menuitem_main_onoff_switch_layout);
        Switch mainOnOffSwitch = (Switch) mainOnOffSwitchLayout.getActionView().findViewById(R.id.action_main_onoff_switch);

        mainOnOffSwitch.setChecked(checked);
    }
}
