package com.owlab.callblocker;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class AddSourceSelectionActivity extends AppCompatActivity {
    private static final String TAG = AddSourceSelectionActivity.class.getSimpleName();

    private String transitionSource;
    private String transitionTarget;

    FloatingActionButton addFromCallLogFab;
    TextView addFromCallLogLabel;
    FloatingActionButton addFromSmsLogFab;
    TextView addFromSmsLogLabel;
    FloatingActionButton addFromContactsFab;
    TextView addFromContactsLabel;
    FloatingActionButton addByManualFab;
    TextView addByManualLabel;
    Animation rotateForwardAppear;
    Animation rotateBackwardAppear;
    Animation miniFabOpen;
    Animation rotateForwardDisappear;
    Animation rotateBackwardDisappear;
    Animation miniFabClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, ">>>>> onCreate called");

        setContentView(R.layout.activity_add_source_selection);

        Intent createIntent = getIntent();
        transitionSource = createIntent.getStringExtra(CONS.INTENT_KEY_TRANSITION_SOURCE);

        //buttons
        addFromCallLogFab = (FloatingActionButton) findViewById(R.id.fab_add_from_call_log);
        addFromCallLogLabel = (TextView) findViewById(R.id.label_add_from_call_log);
        addFromSmsLogFab = (FloatingActionButton) findViewById(R.id.fab_add_from_sms_log);
        addFromSmsLogLabel = (TextView) findViewById(R.id.label_add_from_sms_log);
        addFromContactsFab = (FloatingActionButton) findViewById(R.id.fab_add_from_contacts);
        addFromContactsLabel = (TextView) findViewById(R.id.label_add_from_contacts);
        addByManualFab = (FloatingActionButton) findViewById(R.id.fab_add_by_manual);
        addByManualLabel = (TextView) findViewById(R.id.label_add_by_manual);

        //animations
        rotateForwardAppear = AnimationUtils.loadAnimation(this, R.anim.rotate_forward_appear);
        rotateBackwardAppear = AnimationUtils.loadAnimation(this, R.anim.rotate_backward_appear);
        rotateForwardDisappear = AnimationUtils.loadAnimation(this, R.anim.rotate_forward_disappear);
        rotateBackwardDisappear = AnimationUtils.loadAnimation(this, R.anim.rotate_backward_disappear);
        miniFabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open);
        miniFabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close);

        View.OnClickListener fromCallLogOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<String> neededPermissionList = new ArrayList<>();
                boolean shouldShowRequestPermissionRationale = false;
                if (PermissionChecker.checkSelfPermission(getBaseContext(), Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(AddSourceSelectionActivity.this, Manifest.permission.READ_CALL_LOG)) {
                        shouldShowRequestPermissionRationale = true;
                    }
                    neededPermissionList.add(Manifest.permission.READ_CALL_LOG);
                }
                if (PermissionChecker.checkSelfPermission(getBaseContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(AddSourceSelectionActivity.this, Manifest.permission.READ_CONTACTS)) {
                        shouldShowRequestPermissionRationale = true;
                    }
                    neededPermissionList.add(Manifest.permission.READ_CONTACTS);
                }

                if (neededPermissionList.size() == 0) {
                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    intent.putExtra(CONS.INTENT_KEY_TARGET_FRAGMENT, CONS.FRAGMENT_CALL_LOG);
                    setResult(RESULT_OK, intent);
                    finish();
                    return;
                }

                final String[] neededPermissions = new String[neededPermissionList.size()];
                neededPermissionList.toArray(neededPermissions);

                if (shouldShowRequestPermissionRationale) {
                    FUNS.showMessageWithOKCancel(
                            AddSourceSelectionActivity.this,
                            "This feature need the following permission. Denying may cause not to function as intended.",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int which) {
                                    //Request permission
                                    ActivityCompat.requestPermissions(AddSourceSelectionActivity.this, neededPermissions, CONS.REQUEST_CODE_ASK_PERMISSION_FOR_READ_CALL_LOG);
                                }
                            }
                            , null);
                } else {
                    //Request permissions
                    ActivityCompat.requestPermissions(AddSourceSelectionActivity.this, neededPermissions, CONS.REQUEST_CODE_ASK_PERMISSION_FOR_READ_CALL_LOG);
                }
            }
        };

        addFromCallLogFab.setOnClickListener(fromCallLogOnClickListener);
        addFromCallLogLabel.setOnClickListener(fromCallLogOnClickListener);

        View.OnClickListener fromSmsLogOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<String> neededPermissionList = new ArrayList<>();
                boolean shouldShowRequestPermissionRationale = false;
                if (PermissionChecker.checkSelfPermission(getBaseContext(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(AddSourceSelectionActivity.this, Manifest.permission.READ_SMS)) {
                        shouldShowRequestPermissionRationale = true;
                    }
                    neededPermissionList.add(Manifest.permission.READ_SMS);
                }
                if (PermissionChecker.checkSelfPermission(getBaseContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(AddSourceSelectionActivity.this, Manifest.permission.READ_CONTACTS)) {
                        shouldShowRequestPermissionRationale = true;
                    }
                    neededPermissionList.add(Manifest.permission.READ_CONTACTS);
                }

                if (neededPermissionList.size() == 0) {
                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    intent.putExtra(CONS.INTENT_KEY_TARGET_FRAGMENT, CONS.FRAGMENT_SMS_LOG);
                    setResult(RESULT_OK, intent);
                    finish();
                    return;
                }

                final String[] neededPermissions = new String[neededPermissionList.size()];
                neededPermissionList.toArray(neededPermissions);

                if (shouldShowRequestPermissionRationale) {
                    FUNS.showMessageWithOKCancel(
                            AddSourceSelectionActivity.this,
                            "This feature need the following permission. Denying may cause not to function as intended.",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int which) {
                                    //Request permission
                                    ActivityCompat.requestPermissions(AddSourceSelectionActivity.this, neededPermissions, CONS.REQUEST_CODE_ASK_PERMISSION_FOR_READ_SMS_LOG);
                                }
                            }
                            , null);
                } else {
                    //Request permissions
                    ActivityCompat.requestPermissions(AddSourceSelectionActivity.this, neededPermissions, CONS.REQUEST_CODE_ASK_PERMISSION_FOR_READ_SMS_LOG);
                }
            }
        };

        addFromSmsLogFab.setOnClickListener(    fromSmsLogOnClickListener);
        addFromSmsLogLabel.setOnClickListener(  fromSmsLogOnClickListener);

        View.OnClickListener addFromContactsOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if (PermissionChecker.checkSelfPermission(getBaseContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                if (PermissionChecker.checkSelfPermission(getBaseContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    intent.putExtra(CONS.INTENT_KEY_TARGET_FRAGMENT, CONS.FRAGMENT_CONTACTS);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(AddSourceSelectionActivity.this, Manifest.permission.READ_CONTACTS)) {
                        FUNS.showMessageWithOKCancel(
                                AddSourceSelectionActivity.this,
                                "This feature need the following permission. Denying may cause not to function as intended.",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int which) {
                                        //Request permission
                                        ActivityCompat.requestPermissions(AddSourceSelectionActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, CONS.REQUEST_CODE_ASK_PERMISSION_FOR_READ_CONTACTS);
                                    }
                                }
                                , null);
                    } else {
                        //Request permission
                        ActivityCompat.requestPermissions(AddSourceSelectionActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, CONS.REQUEST_CODE_ASK_PERMISSION_FOR_READ_CONTACTS);
                    }
                }
            }
        };

        addFromContactsFab.setOnClickListener(addFromContactsOnClickListener);
        addFromContactsLabel.setOnClickListener(addFromContactsOnClickListener);

        View.OnClickListener addByManualOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                intent.putExtra(CONS.INTENT_KEY_TARGET_FRAGMENT, CONS.FRAGMENT_ADD_BY_MANUAL);
                setResult(RESULT_OK, intent);
                finish();
            }
        };

        addByManualFab.setOnClickListener(addByManualOnClickListener);
        addByManualLabel.setOnClickListener(addByManualOnClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, ">>>>> onResume called");
        Log.d(TAG, ">>>>> transitionSource ? " + transitionSource);

        //initialize for onPause
        transitionTarget = null;

        if (transitionSource != null && transitionSource.equals(CONS.FRAGMENT_VIEW_PAGER_CONTAINER)) {
            addFromCallLogFab.startAnimation(rotateForwardAppear);
            //addFromCallLogFab.startAnimation(rotateForwardDisappear);
        } else {
            //addFromCallLogFab.startAnimation(rotateBackwardAppear);
            addFromCallLogFab.startAnimation(rotateBackwardAppear);
        }

        addFromCallLogLabel.startAnimation(miniFabOpen);
        addFromSmsLogFab.startAnimation(miniFabOpen);
        addFromSmsLogLabel.startAnimation(miniFabOpen);
        addFromContactsFab.startAnimation(miniFabOpen);
        addFromContactsLabel.startAnimation(miniFabOpen);
        addByManualFab.startAnimation(miniFabOpen);
        addByManualLabel.startAnimation(miniFabOpen);
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d(TAG, ">>>>> onPause called");
        Log.d(TAG, ">>>>> transitionTarget ? " + transitionTarget);

        //initialize for onResume
        transitionSource = null;

        if (transitionTarget != null && transitionTarget.equals(CONS.FRAGMENT_CALL_LOG)) {
            addFromCallLogFab.startAnimation(rotateForwardDisappear);
        } else {
            addFromCallLogFab.startAnimation(rotateBackwardDisappear);
        }

        addByManualLabel.startAnimation(miniFabClose);
        addByManualFab.startAnimation(miniFabClose);
        addFromContactsLabel.startAnimation(miniFabClose);
        addFromContactsFab.startAnimation(miniFabClose);
        addFromCallLogLabel.startAnimation(miniFabClose);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, ">>>>> back pressed");
        super.onBackPressed();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean readCallLogPermissionGranted = PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
                && PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
        boolean readSmsLogPermissionGranted = PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
                && PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
        boolean readContactsPermissionGranted = PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;

        switch (requestCode) {
            case CONS.REQUEST_CODE_ASK_PERMISSION_FOR_READ_CALL_LOG:
                if (readCallLogPermissionGranted) {
                    //open read & import call log fragment
                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    intent.putExtra(CONS.INTENT_KEY_TARGET_FRAGMENT, CONS.FRAGMENT_CALL_LOG);
                    //startActivity(intent);
                    setResult(RESULT_OK, intent);
                    finish();

                } else {
                    Toast.makeText(this, "Can not open call log by lack of permission.", Toast.LENGTH_SHORT).show();
                    //Snackbar.make(findViewById(android.R.id.content), "Can not open the call by lack of permission.", Snackbar.LENGTH_SHORT).show();
                }
                break;
            case CONS.REQUEST_CODE_ASK_PERMISSION_FOR_READ_SMS_LOG:
                if (readSmsLogPermissionGranted) {
                    //open read & import call log fragment
                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    intent.putExtra(CONS.INTENT_KEY_TARGET_FRAGMENT, CONS.FRAGMENT_SMS_LOG);
                    //startActivity(intent);
                    setResult(RESULT_OK, intent);
                    finish();

                } else {
                    Toast.makeText(this, "Can not open sms log by lack of permission.", Toast.LENGTH_SHORT).show();
                    //Snackbar.make(findViewById(android.R.id.content), "Can not open the call by lack of permission.", Snackbar.LENGTH_SHORT).show();
                }
                break;
            case CONS.REQUEST_CODE_ASK_PERMISSION_FOR_READ_CONTACTS:
                if (readContactsPermissionGranted) {
                    //open read & import call log fragment
                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    intent.putExtra(CONS.INTENT_KEY_TARGET_FRAGMENT, CONS.FRAGMENT_CONTACTS);
                    //startActivity(intent);
                    setResult(RESULT_OK, intent);
                    finish();

                } else {
                    Toast.makeText(this, "Can not open contacts by lack of permission.", Toast.LENGTH_SHORT).show();
                    //Snackbar.make(findViewById(android.R.id.content), "Can not open the call by lack of permission.", Snackbar.LENGTH_SHORT).show();
                }
                break;
            default:
                Log.e(TAG, ">>>>> request code unsupported: " + requestCode);
        }
    }
}
