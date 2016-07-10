package com.owlab.callquieter;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.owlab.callquieter.dialog.HelpDialog;
import com.owlab.callquieter.fragment.AddByManualFragment;
import com.owlab.callquieter.fragment.AddFromCallLogFragment;
import com.owlab.callquieter.fragment.AddFromContactsFragment;
import com.owlab.callquieter.fragment.AddFromSmsLogFragment;
import com.owlab.callquieter.fragment.SettingsFragment;
import com.owlab.callquieter.fragment.ViewPagerContainerFragment;
import com.owlab.callquieter.service.CallQuieterIntentService;

import java.util.ArrayList;
import java.util.List;

/**
 * Top most setting element is "SERVICE ON/OFF"
 * - If service off then no filtering activity occurs and no notification icon of this app enabled
 * - If service on then filtering occurs
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private Toolbar toolbar;

    private String nextFragmentTag;
    private boolean onActivityResultCalled;
    private boolean onRequestPermissionsResultCalled;

    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, ">>>>> onCreate called with savedInstanceState: " + savedInstanceState);

        boolean recovered = false;
        if (savedInstanceState != null) {
            recovered = true;
        }

        if (!recovered) {
            //Initialize app
            FUNS.initializeApp(this);
        }

        setContentView(R.layout.activity_main);


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                || PermissionChecker.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED
                || PermissionChecker.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED
                || PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED
                ) {

            List<String> neededPermissionList = new ArrayList<>();
            boolean shouldShowRequestPermissionRationale = false;

            if (PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                    shouldShowRequestPermissionRationale = true;
                }
                neededPermissionList.add(Manifest.permission.READ_CONTACTS);
            }
            if (PermissionChecker.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)) {
                    shouldShowRequestPermissionRationale = true;
                }
                neededPermissionList.add(Manifest.permission.CALL_PHONE);
            }
            if (PermissionChecker.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.INTERNET)) {
                    shouldShowRequestPermissionRationale = true;
                }
                neededPermissionList.add(Manifest.permission.INTERNET);
            }
            if (PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_NETWORK_STATE)) {
                    shouldShowRequestPermissionRationale = true;
                }
                neededPermissionList.add(Manifest.permission.ACCESS_NETWORK_STATE);
            }

            final String[] neededPermissions = new String[neededPermissionList.size()];
            neededPermissionList.toArray(neededPermissions);
            if (shouldShowRequestPermissionRationale) {

                FUNS.showMessageWithOKCancel(
                        this,
                        "This App need permissions to view blocked calls and make call on the blocked call log",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MainActivity.this, neededPermissions, CONS.REQUEST_CODE_ASK_PERMISSION_FOR_AD_AND_READ_QUIETED_CALLS);
                            }
                        },
                        null);
            } else {
                ActivityCompat.requestPermissions(this, neededPermissions, CONS.REQUEST_CODE_ASK_PERMISSION_FOR_AD_AND_READ_QUIETED_CALLS);
            }
        } else {

            //
            initializeAd();

            if (!recovered) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ViewPagerContainerFragment(), ViewPagerContainerFragment.TAG)
                        .commit();

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
        //getSupportFragmentManager().putFragment(outState, "nextFragmentTag", fragment);
        super.onSaveInstanceState(outState);

        //Log.d(TAG, ">>>>> onSaveInstanceState called");
    }

    @Override
    protected void onPause() {
        //Log.d(TAG, ">>>>> onPause");
        adView.pause();

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Log.d(TAG, ">>>>> onResume");
        adView.resume();


    }

    @Override
    public void onDestroy() {
        adView.destroy();

        super.onDestroy();
    }

    @Override
    public void onPostResume() {
        super.onPostResume();
        //Log.d(TAG, ">>>>> onPostResume");
        //Log.d(TAG, ">>>>> onActivityResultCalled = " + onActivityResultCalled);
        //Log.d(TAG, ">>>>> onRequestPermissionsResultCalled = " + onRequestPermissionsResultCalled);


        if (onActivityResultCalled) {
            showFragmentAfterOnActivityResult(nextFragmentTag);
            onActivityResultCalled = false;
        }

        if (onRequestPermissionsResultCalled) {
            showFragmentAfterOnRequestPermissionsResult(nextFragmentTag);
            onRequestPermissionsResultCalled = false;
        }

        if (newIntentArrived) {
            newIntentArrived = false;
            Intent intent = getIntent();
            Log.d(TAG, ">>>>> intent: " + intent.toString());
            if (intent != null && "OPEN_QUIETED_CALL_LOG".equals(intent.getAction())) {
                int pageNo = intent.getIntExtra("pageNo", 0);
                Log.d(TAG, ">>>>> pageNo: " + pageNo);

                ViewPagerContainerFragment viewPagerContainerFragment = (ViewPagerContainerFragment) getSupportFragmentManager().findFragmentByTag(ViewPagerContainerFragment.TAG);
                Log.d(TAG, ">>>>> pager fragment visible? " + (viewPagerContainerFragment != null ? viewPagerContainerFragment.isVisible() : null));
                if (viewPagerContainerFragment == null) {
                    Log.d(TAG, ">>>>> pager fragment is null");

                    //This is not possible
                    // 1. the notification action - origin of this action
                    // 2. the view pager container fragment is the default fragment in the MainActivity, thus it is always not null, if this App started, even just before.
                }
                if (viewPagerContainerFragment != null) {
                    Log.d(TAG, ">>>>> pager fragment is not null");
                    if (viewPagerContainerFragment.isVisible()) {
                        Log.d(TAG, ">>>>> pager fragment is visible");
                        viewPagerContainerFragment.setPage(pageNo);
                    } else {
                        Log.d(TAG, ">>>>> pager fragment is invisible");
                        //Need to back to the view pager fragment
                        getSupportFragmentManager().popBackStack(ViewPagerContainerFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        viewPagerContainerFragment.setPage(pageNo);

                    }
                }
            }
        }
    }

    private boolean newIntentArrived;

    @Override
    public void onNewIntent(Intent intent) {
        //Log.d(TAG, ">>>>> new intent: " + intent.toString());
        //forward for further processing in onPostResume
        setIntent(intent);
        newIntentArrived = true;
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
        //MenuItem mainOnOffSwitchLayout = menu.findItem(R.id.menuitem_main_onoff_switch_layout);
        //ToggleButton mainOnOffSwitch = (ToggleButton) mainOnOffSwitchLayout.getActionView().findViewById(R.id.action_main_onoff_switch);

        boolean mainOnOffSwichChecked = sharedPreferences.getBoolean(CONS.PREF_KEY_BLOCKING_ON, false);
        if (mainOnOffSwichChecked)
            CallQuieterIntentService.startActionQuieterOn(this, new ResultReceiver(new Handler()) {
                //
            });
        mainOnOffSwitch.setChecked(mainOnOffSwichChecked);
        mainOnOffSwitch.setOnCheckedChangeListener(new FUNS.BlockingSwitchChangeListener(this));

        boolean result = super.onCreateOptionsMenu(menu);

        presentShowcaseForMainOnOffSwitch();

        return result;
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
                        .addToBackStack(ViewPagerContainerFragment.TAG)
                        .commit();
                return true;
            case R.id.menuitem_help:
                //WebView helpWV = new WebView(this);
                //helpWV.loadData("haha", "text/html; charset=utf-8", "utf-8");
                //AlertDialog.Builder builder = new AlertDialog.Builder(this);
                //builder.setTitle("sample")
                //        .setView(helpWV)
                //        .show();
                //        //.setNeutralButton("OK", dispatchPopulateAccessibilityEvent())

                HelpDialog helpDialog = new HelpDialog(MainActivity.this);
                helpDialog.setTitle("Tips & Feedback");
                helpDialog.setCanceledOnTouchOutside(true);
                helpDialog.setCancelable(true);
                helpDialog.show();

                //Dialog helpDialog = new Dialog(this);
                //helpDialog.setTitle("Tips");
                //helpDialog.setCancelable(true);
                //helpDialog.show();
                return true;
            case android.R.id.home:
                getSupportFragmentManager().popBackStack();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, ">>>>> back pressed");

    }

    public void setMainOnOffSwitch(boolean checked) {
        MenuItem mainOnOffSwitchLayout = menu.findItem(R.id.menuitem_main_onoff_switch_layout);
        Switch mainOnOffSwitch = (Switch) mainOnOffSwitchLayout.getActionView().findViewById(R.id.action_main_onoff_switch);
        //ToggleButton mainOnOffSwitch = (ToggleButton) mainOnOffSwitchLayout.getActionView().findViewById(R.id.action_main_onoff_switch);

        mainOnOffSwitch.setChecked(checked);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        onActivityResultCalled = true;
        if (requestCode == CONS.REQUEST_CODE_ADD_SOURCE_SELECTION) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, ">>>>> result ok received from add source selection activity");
                nextFragmentTag = data.getStringExtra(CONS.INTENT_KEY_TARGET_FRAGMENT);
            } else if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, ">>>>> result canceled received");
                nextFragmentTag = ViewPagerContainerFragment.TAG;
            } else if (resultCode == RESULT_FIRST_USER) {
                Log.d(TAG, ">>>>> result_first_user received");
                //TODO what is this?
            }
        }
    }

    private void showFragmentAfterOnActivityResult(String tag) {
        Log.d(TAG, ">>>>> tag: " + tag);
        if (tag == null) {
            return;
        }

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);

        if (ViewPagerContainerFragment.TAG.equals(tag)) {
            //getSupportFragmentManager().beginTransaction()
            //        .addToBackStack(currentFragmentTag)
            //        .replace(R.id.fragment_container, fragment != null ? fragment : new ViewPagerContainerFragment(), ViewPagerContainerFragment.TAG)
            //        .commit();
        } else if (AddFromCallLogFragment.TAG.equals(tag)) {
            getSupportFragmentManager().beginTransaction()
                    .addToBackStack(ViewPagerContainerFragment.TAG)
                    .replace(R.id.fragment_container, fragment != null ? fragment : new AddFromCallLogFragment(), AddFromCallLogFragment.TAG)
                    .commit();
        } else if (AddFromSmsLogFragment.TAG.equals(tag)) {
            getSupportFragmentManager().beginTransaction()
                    .addToBackStack(ViewPagerContainerFragment.TAG)
                    .replace(R.id.fragment_container, fragment != null ? fragment : new AddFromSmsLogFragment(), AddFromSmsLogFragment.TAG)
                    .commit();
        } else if (AddFromContactsFragment.TAG.equals(tag)) {
            getSupportFragmentManager().beginTransaction()
                    .addToBackStack(ViewPagerContainerFragment.TAG)
                    .replace(R.id.fragment_container, fragment != null ? fragment : new AddFromContactsFragment(), AddFromContactsFragment.TAG)
                    .commit();
        } else if (AddByManualFragment.TAG.equals(tag)) {
            getSupportFragmentManager().beginTransaction()
                    .addToBackStack(ViewPagerContainerFragment.TAG)
                    .replace(R.id.fragment_container, fragment != null ? fragment : new AddByManualFragment(), AddByManualFragment.TAG)
                    .commit();
            //Fragment viewPagerContainerFragment = getSupportFragmentManager().findFragmentByTag(ViewPagerContainerFragment.TAG);
            //Fragment currentPageFragment = ((ViewPagerContainerFragment) viewPagerContainerFragment).getCurrentPageFragment();
            //Log.d(TAG, ">>> fragment found: " + Objects.toString(viewPagerContainerFragment));
            //DialogFragment addByManualDialogFragment = new AddByManualDialogFragment();
            ////addByManualDialogFragment.setTargetFragment(viewPagerContainer, 0);
            //addByManualDialogFragment.setTargetFragment(currentPageFragment != null ? currentPageFragment : viewPagerContainerFragment, 0);
            //addByManualDialogFragment.show(getSupportFragmentManager(), AddByManualDialogFragment.TAG);
        }
    }

    private void showFragmentAfterOnRequestPermissionsResult(String tag) {
        if (ViewPagerContainerFragment.TAG.equals(tag)) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ViewPagerContainerFragment(), ViewPagerContainerFragment.TAG)
                    .commit();

        } else {
            throw new UnsupportedOperationException("tag should be " + ViewPagerContainerFragment.TAG + ", but " + tag);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //delegate
        //FUNS.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        Log.d(TAG, ">>>>> called");

        //Because request of multiple permissions may not result in the same number of permissions granted!
        boolean permissionReadPhoneStateGranted = PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
        boolean permissionCallPhoneGranted = PermissionChecker.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED;
        boolean permissionReadCallLogGranted = PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED;
        boolean permissionWriteCallLogGranted = PermissionChecker.checkSelfPermission(this, Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED;
        boolean permissionForAdAndnReadQuietedCallLogGranted = PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                && PermissionChecker.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
                && PermissionChecker.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED
                && PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED;

        boolean permissionBlockHiddenNumberGranted = PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
        boolean permissionBlockUnknownNumberGranted = PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                && PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
        ;
        //boolean permissionReadContactsGranted = PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
        //boolean permissionWriteContactsGranted = PermissionChecker.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED;

        SharedPreferences sharedPreferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(this);

        switch (requestCode) {
            case CONS.REQUEST_CODE_ASK_PERMISSION_FOR_AD_AND_READ_QUIETED_CALLS:
                if (permissionForAdAndnReadQuietedCallLogGranted) {
                    //
                    initializeAd();
                    //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ViewPagerContainerFragment(), ViewPagerContainerFragment.TAG).commit();
                    nextFragmentTag = ViewPagerContainerFragment.TAG;
                    onRequestPermissionsResultCalled = true;
                } else {
                    Toast.makeText(this, "Lack of permissions", Toast.LENGTH_SHORT).show();
                    this.finish();
                }
                break;

            case CONS.REQUEST_CODE_ASK_PERMISSION_FOR_BLOCKING:
                //Log.d(TAG, ">>>>> result of asking remaining " + permissions.length + " permission(s)");

                boolean canStart = true;

                if (sharedPreferences.getBoolean(this.getString(R.string.settings_key_block_hidden_number), false) && !permissionBlockHiddenNumberGranted) {
                    canStart = false;
                }
                if (sharedPreferences.getBoolean(this.getString(R.string.settings_key_block_unknown_number), false) && !permissionBlockUnknownNumberGranted) {
                    canStart = false;
                }
                if (sharedPreferences.getBoolean(this.getString(R.string.settings_key_suppress_ringing), false) && !permissionReadPhoneStateGranted) {
                    canStart = false;
                }
                if (sharedPreferences.getBoolean(this.getString(R.string.settings_key_dismiss_call), false) && !permissionCallPhoneGranted) {
                    canStart = false;
                }
                if (sharedPreferences.getBoolean(this.getString(R.string.settings_key_delete_call_log), false) &&
                        //!(permissionReadCallLogGranted && permissionWriteCallLogGranted && permissionReadContactsGranted && permissionWriteContactsGranted)) {
                        !(permissionReadCallLogGranted && permissionWriteCallLogGranted)) {
                    canStart = false;
                }

                MainActivity mainActivity = (MainActivity) this;
                if (!canStart) {
                    //do nothing
                    //and change the main onOff switch
                    mainActivity.setMainOnOffSwitch(false);
                    Toast.makeText(mainActivity, "Can not ON because of lack of permissions", Toast.LENGTH_SHORT).show();
                } else {
                    //otherwise start action - blcoking on
                    CallQuieterIntentService.startActionQuieterOn(this, new ResultReceiver(new Handler()) {
                        @Override
                        protected void onReceiveResult(int resultCode, Bundle reuslt) {
                            Log.d(TAG, ">>>>> result received");
                            Toast.makeText(getBaseContext(), "Blocking " + (resultCode == CONS.RESULT_SUCCESS ? " ON" : " OFF"), Toast.LENGTH_SHORT).show();
                        }
                    });
                    mainActivity.setMainOnOffSwitch(true);
                    Toast.makeText(mainActivity, "Blocking ON", Toast.LENGTH_SHORT).show();
                }

                break;

            case CONS.REQUEST_CODE_ASK_PERMISSION_FOR_BLOCK_HIDDEN_NUMBER:
                if (permissionBlockHiddenNumberGranted) {
                    sharedPreferences.edit().putBoolean(this.getString(R.string.settings_key_block_hidden_number), true).commit();
                } else {
                    sharedPreferences.edit().putBoolean(this.getString(R.string.settings_key_block_hidden_number), false).commit();
                }

                break;

            case CONS.REQUEST_CODE_ASK_PERMISSION_FOR_BLOCK_UNKNOWN_NUMBER:
                if (permissionBlockUnknownNumberGranted) {
                    sharedPreferences.edit().putBoolean(this.getString(R.string.settings_key_block_unknown_number), true).commit();
                } else {
                    sharedPreferences.edit().putBoolean(this.getString(R.string.settings_key_block_unknown_number), false).commit();
                }

                break;

            case CONS.REQUEST_CODE_ASK_PERMISSION_FOR_SUPPRESS_RINGING:
                if (permissionReadPhoneStateGranted) {
                    sharedPreferences.edit().putBoolean(this.getString(R.string.settings_key_suppress_ringing), true).commit();
                } else {
                    sharedPreferences.edit().putBoolean(this.getString(R.string.settings_key_suppress_ringing), false).commit();
                }

                break;

            case CONS.REQUEST_CODE_ASK_PERMISSION_FOR_DISMISS_CALL:
                if (permissionCallPhoneGranted) {
                    sharedPreferences.edit().putBoolean(this.getString(R.string.settings_key_dismiss_call), true).commit();
                } else {
                    sharedPreferences.edit().putBoolean(this.getString(R.string.settings_key_dismiss_call), false).commit();
                }

                break;

            case CONS.REQUEST_CODE_ASK_PERMISSION_FOR_DELETE_CALL_LOG:
                if (permissionReadCallLogGranted && permissionWriteCallLogGranted) {
                    //    if (permissionReadCallLogGranted && permissionWriteCallLogGranted && permissionReadContactsGranted && permissionWriteContactsGranted) {
                    sharedPreferences.edit().putBoolean(this.getString(R.string.settings_key_delete_call_log), true).commit();
                } else {
                    sharedPreferences.edit().putBoolean(this.getString(R.string.settings_key_delete_call_log), false).commit();
                }

                break;

            default:
                Log.e(TAG, ">>>>> unsupported request code: " + requestCode);
        }
    }

    private void initializeAd() {
        //Initialize AdMob
        MobileAds.initialize(getApplicationContext(), getString(R.string.banner_ad_app_id));
        adView = (AdView) findViewById(R.id.adView);
        adView.setAdListener(new AdListener() {
            private final String TAG = AdListener.class.getSimpleName();

            @Override
            public void onAdFailedToLoad(int errorCode) {
                //Log.d(TAG, ">>>>> failed to load, errorCode: " + errorCode);
                //To minimize adview height when laod failed
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) adView.getLayoutParams();
                layoutParams.height = 0;
                adView.setLayoutParams(layoutParams);

                super.onAdFailedToLoad(errorCode);

            }
        });
        AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
        //My Android Phone for test
        adRequestBuilder.addTestDevice("E03A1FE80C73BF8D4F7E11299269201E");

        AdRequest adRequest = adRequestBuilder.build();
        adView.loadAd(adRequest);
    }

    private void presentShowcaseForMainOnOffSwitch() {

        MenuItem mainOnOffSwitchLayout = menu.findItem(R.id.menuitem_main_onoff_switch_layout);
        final Switch mainOnOffSwitch = (Switch) mainOnOffSwitchLayout.getActionView().findViewById(R.id.action_main_onoff_switch);


        ShowcaseView scView = new ShowcaseView.Builder(this)
                //.withNewStyleShowcase()
                .withMaterialShowcase()
                //.setTarget(scTarget)
                .setTarget(new Target() {
                    @Override
                    public Point getPoint() {
                        return new ViewTarget(mainOnOffSwitch).getPoint();
                    }
                })
                .setContentTitle("Turn on the main switch")
                .setContentText("To disable Call Quieter service, you can turn it off at any time.")
                .setStyle(R.style.ShowcaseTheme2)
                //.hideOnTouchOutside()
                .replaceEndButton(R.layout.custom_showcase_button_gotit_layout)
                .singleShot(101l)
                .build();

        RelativeLayout.LayoutParams buttonPosition = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        buttonPosition.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        buttonPosition.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        int margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();
        buttonPosition.setMargins(margin, margin, margin, margin);

        scView.setButtonPosition(buttonPosition);
    }
}
