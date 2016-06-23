package com.owlab.callblocker;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Switch;

import com.owlab.callblocker.fragment.AddByManualDialogFragment;
import com.owlab.callblocker.fragment.AddFromCallLogFragment;
import com.owlab.callblocker.fragment.AddFromContactsFragment;
import com.owlab.callblocker.fragment.AddFromSmsLogFragment;
import com.owlab.callblocker.fragment.SettingsFragment;
import com.owlab.callblocker.fragment.ViewPagerContainerFragment;

import java.util.Objects;

/**
 * Top most setting element is "SERVICE ON/OFF"
 * - If service off then no filtering activity occurs and no notification icon of this app enabled
 * - If service on then filtering occurs
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private String fragmentTagToShow = ViewPagerContainerFragment.TAG; //initial fragment

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, ">>>>> onCreate called with savedInstanceState: " + savedInstanceState);

        //Initialize app
        FUNS.initializeApp(this);


        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                FUNS.showMessageWithOKCancel(
                        this,
                        "This App need REAT CONTACTS permission to view blocked calls",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, CONS.REQUEST_CODE_ASK_PERMISSION_FOR_READ_BLOCKED_CALLS);
                            }
                        },
                        null);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, CONS.REQUEST_CODE_ASK_PERMISSION_FOR_READ_BLOCKED_CALLS);
            }
        } else {
            ViewPagerContainerFragment viewPagerContainerFragment = (ViewPagerContainerFragment) getSupportFragmentManager().findFragmentByTag(ViewPagerContainerFragment.TAG);
            if (viewPagerContainerFragment == null) {
                Log.d(TAG, ">>>>> viewPagerContainerFragment is null, creating...");
                viewPagerContainerFragment = new ViewPagerContainerFragment();
            }
            //TODO filld this gap
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, viewPagerContainerFragment, ViewPagerContainerFragment.TAG)
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, ">>>>> onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d(TAG, ">>>>> onPause");
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
                SettingsFragment settingsFragment = (SettingsFragment) getSupportFragmentManager().findFragmentByTag(SettingsFragment.TAG);
                if (settingsFragment == null) {
                    settingsFragment = new SettingsFragment();

                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, settingsFragment, SettingsFragment.TAG)
                        .addToBackStack(null)
                        .commit();
                return true;
            case android.R.id.home:
                getSupportFragmentManager().popBackStack();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //delegate
        FUNS.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, ">>>>> back pressed");

    }

    public void setMainOnOffSwitch(boolean checked) {
        MenuItem mainOnOffSwitchLayout = menu.findItem(R.id.menuitem_main_onoff_switch_layout);
        Switch mainOnOffSwitch = (Switch) mainOnOffSwitchLayout.getActionView().findViewById(R.id.action_main_onoff_switch);

        mainOnOffSwitch.setChecked(checked);
    }


    @Override
    public void onPostResume() {
        super.onPostResume();
        Log.d(TAG, ">>>>> onPostResume");
        Log.d(TAG, ">>>>> fragmentTagToShow: " + fragmentTagToShow);

        //Start to handle which fragment should be shown
        if (fragmentTagToShow == null) {
            //do nothing
            return;
        }

        Fragment fragmentToShow = getSupportFragmentManager().findFragmentByTag(fragmentTagToShow);

        if (fragmentToShow != null && fragmentToShow.isVisible()) {
            // do nothing
        }

        // transit
        if (AddFromCallLogFragment.TAG.equals(fragmentTagToShow)) {
            getSupportFragmentManager().beginTransaction()
                    .addToBackStack(ViewPagerContainerFragment.TAG)
                    .replace(R.id.fragment_container, fragmentToShow != null ? fragmentToShow : new AddFromCallLogFragment(), AddFromCallLogFragment.TAG)
                    .commit();
        } else if (AddFromSmsLogFragment.TAG.equals(fragmentTagToShow)) {
            getSupportFragmentManager().beginTransaction()
                    .addToBackStack(ViewPagerContainerFragment.TAG)
                    .replace(R.id.fragment_container, fragmentToShow != null ? fragmentToShow : new AddFromSmsLogFragment(), AddFromSmsLogFragment.TAG)
                    .commit();
        } else if (AddFromContactsFragment.TAG.equals(fragmentTagToShow)) {
            getSupportFragmentManager().beginTransaction()
                    .addToBackStack(ViewPagerContainerFragment.TAG)
                    .replace(R.id.fragment_container, fragmentToShow != null ? fragmentToShow : new AddFromContactsFragment(), AddFromContactsFragment.TAG)
                    .commit();
        } else if (AddByManualDialogFragment.TAG.equals(fragmentTagToShow)) {
            Fragment viewPagerContainerFragment = getSupportFragmentManager().findFragmentByTag(ViewPagerContainerFragment.TAG);
            Fragment currentPageFragment = ((ViewPagerContainerFragment) viewPagerContainerFragment).getCurrentPageFragment();
            Log.d(TAG, ">>> fragment found: " + Objects.toString(viewPagerContainerFragment));
            DialogFragment addByManualDialogFragment = new AddByManualDialogFragment();
            //addByManualDialogFragment.setTargetFragment(viewPagerContainer, 0);
            addByManualDialogFragment.setTargetFragment(currentPageFragment != null ? currentPageFragment : viewPagerContainerFragment, 0);
            addByManualDialogFragment.show(getSupportFragmentManager(), AddByManualDialogFragment.TAG);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CONS.REQUEST_CODE_ADD_SOURCE_SELECTION) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, ">>>>> result ok received from add source selection activity");
                fragmentTagToShow = data.getStringExtra(CONS.INTENT_KEY_TARGET_FRAGMENT);
            } else if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, ">>>>> result canceled received");
                fragmentTagToShow = ViewPagerContainerFragment.TAG;
            } else if (resultCode == RESULT_FIRST_USER) {
                Log.d(TAG, ">>>>> result_first_user received");
                //TODO what is this?
            }
        }
    }

    /**
     * Because of bug in support package
     * Without this override, the above onActivityResult will result in exceptions!
     * http://stackoverflow.com/questions/7575921/illegalstateexception-can-not-perform-this-action-after-onsaveinstancestate-wit
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        //getSupportFragmentManager().putFragment(outState, "tag", fragment);
        super.onSaveInstanceState(outState);

        Log.d(TAG, ">>>>> onSaveInstanceState called");
    }

    public static class FragmentSelector {

    }

}
