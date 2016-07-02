package com.owlab.callblocker;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.owlab.callblocker.service.CallBlockerIntentService;
import com.owlab.callblocker.service.CallQuieterIntentService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ernest on 6/10/16.
 */
public class FUNS {
    private static final String TAG = FUNS.class.getSimpleName();

    public static void initializeApp(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        //initialize blocking on, if not
        //if(sharedPreferences.contains(context.getString(R.string.pref_key_blocking_on))) {
        //    //do nothing because it is already initialized
        //} else {
        //    sharedPreferences.edit().putBoolean(context.getString(R.string.pref_key_blocking_on), true).commit();
        //}

        //initialize settings to default values, if not before
        PreferenceManager.setDefaultValues(context, R.xml.settings, false);
    }

    /**
     *
     */
    public static class BlockingSwitchChangeListener implements CompoundButton.OnCheckedChangeListener {
        //private Context context;
        private Activity activity;

        //public BlockingSwitchChangeListener(Context context) {
        public BlockingSwitchChangeListener(Activity activity) {
            //this.context = context;
            this.activity = activity;
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
            if (checked) {
                //First warm up the phone state chagne receiver
                //dealing with permission
                //firstly listing needed permissions
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
                boolean blockHiddenNumberOn = sharedPreferences.getBoolean(activity.getString(R.string.settings_key_block_hidden_number), false);
                boolean blockUnknownNumberOn = sharedPreferences.getBoolean(activity.getString(R.string.settings_key_block_unknown_number), false);
                boolean suppressRiningOn = sharedPreferences.getBoolean(activity.getString(R.string.settings_key_suppress_ringing), false);
                boolean dismissCallOn = sharedPreferences.getBoolean(activity.getString(R.string.settings_key_dismiss_call), false);
                boolean deleteCallLogOn = sharedPreferences.getBoolean(activity.getString(R.string.settings_key_delete_call_log), false);

                boolean needExplanation = false;

                Set<String> requiredPermissionList = new HashSet<>();

                if (!suppressRiningOn && !dismissCallOn && !deleteCallLogOn) {
                    //TODO actually blocking does nothing in this case
                }

                if (blockHiddenNumberOn && !(PermissionChecker.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_PHONE_STATE)) {
                        needExplanation = true;
                    }
                    requiredPermissionList.add(Manifest.permission.READ_PHONE_STATE);
                }

                if (blockUnknownNumberOn && !(PermissionChecker.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_PHONE_STATE)) {
                        needExplanation = true;
                    }
                    requiredPermissionList.add(Manifest.permission.READ_PHONE_STATE);
                }

                if (blockUnknownNumberOn && !(PermissionChecker.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_CONTACTS)) {
                        needExplanation = true;
                    }
                    requiredPermissionList.add(Manifest.permission.READ_CONTACTS);
                }

                if (suppressRiningOn && !(PermissionChecker.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_PHONE_STATE)) {
                        needExplanation = true;
                    }
                    requiredPermissionList.add(Manifest.permission.READ_PHONE_STATE);
                }

                if (dismissCallOn && !(PermissionChecker.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED)) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CALL_PHONE)) {
                        needExplanation = true;
                    }
                    requiredPermissionList.add(Manifest.permission.CALL_PHONE);
                }

                if (deleteCallLogOn && !(PermissionChecker.checkSelfPermission(activity, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED)) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_CALL_LOG)) {
                        needExplanation = true;
                    }
                    requiredPermissionList.add(Manifest.permission.READ_CALL_LOG);
                }

                if (deleteCallLogOn && !(PermissionChecker.checkSelfPermission(activity, Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED)) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_CALL_LOG)) {
                        needExplanation = true;
                    }
                    requiredPermissionList.add(Manifest.permission.WRITE_CALL_LOG);
                }

                //if (deleteCallLogOn && !(PermissionChecker.checkSelfPermission(activity, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED)) {
                //    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_CONTACTS)) {
                //        needExplanation = true;
                //    }
                //    requiredPermissionList.add(Manifest.permission.WRITE_CONTACTS);
                //}

                //if (deleteCallLogOn && !(PermissionChecker.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)) {
                //    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_CONTACTS)) {
                //        needExplanation = true;
                //    }
                //    requiredPermissionList.add(Manifest.permission.READ_CONTACTS);
                //}

                if (requiredPermissionList.size() == 0) {
                    Log.d(TAG, "sending quieter on request");
                    //turn blocking on here
                    CallQuieterIntentService.startActionQuieterOn(activity, new ResultReceiver(new Handler()) {
                        @Override
                        protected void onReceiveResult(int resultCode, Bundle result) {
                            Log.d(TAG, ">>>>> quieter on reuslt received");
                            Toast.makeText(activity, "Call quieter service " + (resultCode == CONS.RESULT_SUCCESS ? " started" : " not started"), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    //that is greater than zero
                    final String[] requiredPermissions = new String[requiredPermissionList.size()];
                    requiredPermissionList.toArray(requiredPermissions);
                    Log.d(TAG, ">>>>> number of permissions asked: " + requiredPermissions.length);

                    if (needExplanation) {
                        showMessageWithOKCancel(activity, "This app needs few permissions in the following dialog. Denying may cause not to function as intended.", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int which) {
                                        ActivityCompat.requestPermissions(activity, requiredPermissions, CONS.REQUEST_CODE_ASK_PERMISSION_FOR_BLOCKING);
                                    }
                                },
                                null);
                    } else {
                        ActivityCompat.requestPermissions(activity, requiredPermissions, CONS.REQUEST_CODE_ASK_PERMISSION_FOR_BLOCKING);
                    }
                    //status of the switch will be determined in the permission request response handling section
                    compoundButton.setChecked(false);
                }

                //TODO think about the right places of this code
            } else {
                Log.d(TAG, "sending quieter off request");
                CallQuieterIntentService.startActionQuieterOff(activity, new ResultReceiver(new Handler()) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle result) {
                        Toast.makeText(activity, "Call quieter service " + (resultCode == CONS.RESULT_SUCCESS ? " stopped" : "not stopped"), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    public static void showMessageWithOKCancel(Activity activity, String message, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener cancelListener) {
        new AlertDialog.Builder(activity)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", cancelListener)
                .create()
                .show();
    }

    /**
     *
     */
    public static class ShowBlockingNotificationIconPrefChangeListener implements Preference.OnPreferenceChangeListener {
        private static final String TAG = ShowBlockingNotificationIconPrefChangeListener.class.getSimpleName();

        private Context context;

        public ShowBlockingNotificationIconPrefChangeListener(Context context) {
            this.context = context;
        }


        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Log.d(TAG, ">>> changed to " + newValue.toString());

            boolean checked = (Boolean) newValue;
            if (checked) {
                //Show notification icon if not
                CallBlockerIntentService.startActionStatusbarNotificationOn(context);
            } else {
                //Remove notification icon if shown
                CallBlockerIntentService.startActionStatusbarNotificationOff(context);
            }

            return true;
        }
    }

    public static class BlockHiddenNumberPrefChangeListener implements Preference.OnPreferenceChangeListener {
        private static final String TAG = BlockHiddenNumberPrefChangeListener.class.getSimpleName();


        private Activity activity;

        public BlockHiddenNumberPrefChangeListener(Activity activity) {
            this.activity = activity;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Log.d(TAG, ">>> changed to " + newValue.toString());

            boolean checked = (Boolean) newValue;
            if (checked) {
                if (PermissionChecker.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    //returning true means changing the checkbox finally
                    return true;
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_PHONE_STATE)) {
                        showMessageWithOKCancel(activity, "This app needs the permission in the following dialog. Denying may cause not to function as intended.", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int which) {
                                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, CONS.REQUEST_CODE_ASK_PERMISSION_FOR_BLOCK_HIDDEN_NUMBER);
                                    }
                                },
                                null);
                    } else {
                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, CONS.REQUEST_CODE_ASK_PERMISSION_FOR_BLOCK_HIDDEN_NUMBER);
                    }
                    return false;
                }
            } else {
                return true;
            }
        }
    }

    public static class BlockUnknownNumberPrefChangeListener implements Preference.OnPreferenceChangeListener {
        private static final String TAG = BlockUnknownNumberPrefChangeListener.class.getSimpleName();


        private Activity activity;

        public BlockUnknownNumberPrefChangeListener(Activity activity) {
            this.activity = activity;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Log.d(TAG, ">>> changed to " + newValue.toString());

            boolean checked = (Boolean) newValue;
            if (checked) {
                boolean permissionReadCallLogGranted = PermissionChecker.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
                boolean permissionWriteCallLogGranted = PermissionChecker.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
                //boolean permissionReadContactsGranted = PermissionChecker.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
                //boolean permissionWriteContactsGranted = PermissionChecker.checkSelfPermission(activity, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED;
                //returning true means changing the checkbox finally

                boolean needExplanation = false;

                List<String> neededPermissionList = new ArrayList<>();

                if (!permissionReadCallLogGranted) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_PHONE_STATE)) {
                        needExplanation = true;
                    }
                    neededPermissionList.add(Manifest.permission.READ_PHONE_STATE);
                }

                if (!permissionWriteCallLogGranted) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_CONTACTS)) {
                        needExplanation = true;
                    }
                    neededPermissionList.add(Manifest.permission.READ_CONTACTS);
                }

                //if (!permissionReadContactsGranted) {
                //    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_CONTACTS)) {
                //        needExplanation = true;
                //    }
                //    neededPermissionList.add(Manifest.permission.READ_CONTACTS);
                //}

                //if (!permissionWriteContactsGranted) {
                //    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_CONTACTS)) {
                //        needExplanation = true;
                //    }
                //    neededPermissionList.add(Manifest.permission.WRITE_CONTACTS);
                //}

                if(neededPermissionList.size() == 0) {
                    return true;
                }

                final String[] neededPermissions = new String[neededPermissionList.size()];
                neededPermissionList.toArray(neededPermissions);

                if(needExplanation) {
                    showMessageWithOKCancel(activity, "This app needs permissions in the following dialog. Denying may cause not to function as intended.", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int which) {
                                    ActivityCompat.requestPermissions(activity, neededPermissions, CONS.REQUEST_CODE_ASK_PERMISSION_FOR_BLOCK_UNKNOWN_NUMBER);
                                }
                            },
                            null);
                } else {
                    ActivityCompat.requestPermissions(activity, neededPermissions, CONS.REQUEST_CODE_ASK_PERMISSION_FOR_BLOCK_UNKNOWN_NUMBER);
                }
                return false;
            } else {
                return true;
            }
        }
    }

    /**
     *
     */
    public static class SuppressRingingPrefChangeListener implements Preference.OnPreferenceChangeListener {
        private static final String TAG = SuppressRingingPrefChangeListener.class.getSimpleName();

        private Activity activity;

        public SuppressRingingPrefChangeListener(Activity activity) {
            this.activity = activity;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Log.d(TAG, ">>> changed to " + newValue.toString());

            boolean checked = (Boolean) newValue;
            if (checked) {
                if (PermissionChecker.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    //returning true means changing the checkbox finally
                    return true;
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_PHONE_STATE)) {
                        showMessageWithOKCancel(activity, "This app needs the permission in the following dialog. Denying may cause not to function as intended.", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int which) {
                                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, CONS.REQUEST_CODE_ASK_PERMISSION_FOR_SUPPRESS_RINGING);
                                    }
                                },
                                null);
                    } else {
                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, CONS.REQUEST_CODE_ASK_PERMISSION_FOR_SUPPRESS_RINGING);
                    }
                    return false;
                }
            } else {
                return true;
            }
        }
    }

    /**
     *
     public static class SuppressCallNotificationPrefChangeListener implements Preference.OnPreferenceChangeListener {
     private static final String TAG = SuppressCallNotificationPrefChangeListener.class.getSimpleName();

     @Override public boolean onPreferenceChange(Preference preference, Object newValue) {
     Log.d(TAG, ">>> changed to " + newValue.toString());

     //returning true means changing the checkbox finally
     return true;
     }
     }
     */

    /**
     *
     */
    public static class DismissCallPrefChangeListener implements Preference.OnPreferenceChangeListener {
        private static final String TAG = DismissCallPrefChangeListener.class.getSimpleName();

        private Activity activity;

        public DismissCallPrefChangeListener(Activity activity) {
            this.activity = activity;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Log.d(TAG, ">>> changed to " + newValue.toString());

            boolean checked = (Boolean) newValue;
            if (checked) {
                if (PermissionChecker.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    //returning true means changing the checkbox finally
                    return true;
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CALL_PHONE)) {
                        showMessageWithOKCancel(activity, "This app needs the permission in the following dialog. Denying may cause not to function as intended.", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int which) {
                                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CALL_PHONE}, CONS.REQUEST_CODE_ASK_PERMISSION_FOR_DISMISS_CALL);
                                    }
                                },
                                null);
                    } else {
                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CALL_PHONE}, CONS.REQUEST_CODE_ASK_PERMISSION_FOR_DISMISS_CALL);
                    }
                    return false;
                }
            } else {
                return true;
            }
        }
    }

    /**
     *
     */
    public static class DeleteCallLogPrefChangeListener implements Preference.OnPreferenceChangeListener {
        private static final String TAG = DeleteCallLogPrefChangeListener.class.getSimpleName();

        private Activity activity;

        public DeleteCallLogPrefChangeListener(Activity activity) {
            this.activity = activity;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Log.d(TAG, ">>> changed to " + newValue.toString());

            boolean checked = (Boolean) newValue;
            if (checked) {
                boolean permissionReadCallLogGranted = PermissionChecker.checkSelfPermission(activity, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED;
                boolean permissionWriteCallLogGranted = PermissionChecker.checkSelfPermission(activity, Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED;
                //boolean permissionReadContactsGranted = PermissionChecker.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
                //boolean permissionWriteContactsGranted = PermissionChecker.checkSelfPermission(activity, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED;
                //returning true means changing the checkbox finally

                boolean needExplanation = false;

                List<String> neededPermissionList = new ArrayList<>();

                if (!permissionReadCallLogGranted) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_CALL_LOG)) {
                        needExplanation = true;
                    }
                    neededPermissionList.add(Manifest.permission.READ_CALL_LOG);
                }

                if (!permissionWriteCallLogGranted) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_CALL_LOG)) {
                        needExplanation = true;
                    }
                    neededPermissionList.add(Manifest.permission.WRITE_CALL_LOG);
                }

                //if (!permissionReadContactsGranted) {
                //    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_CONTACTS)) {
                //        needExplanation = true;
                //    }
                //    neededPermissionList.add(Manifest.permission.READ_CONTACTS);
                //}

                //if (!permissionWriteContactsGranted) {
                //    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_CONTACTS)) {
                //        needExplanation = true;
                //    }
                //    neededPermissionList.add(Manifest.permission.WRITE_CONTACTS);
                //}

                if(neededPermissionList.size() == 0) {
                    return true;
                }

                final String[] neededPermissions = new String[neededPermissionList.size()];
                neededPermissionList.toArray(neededPermissions);

                if(needExplanation) {
                    showMessageWithOKCancel(activity, "This app needs permissions in the following dialog. Denying may cause not to function as intended.", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int which) {
                                    ActivityCompat.requestPermissions(activity, neededPermissions, CONS.REQUEST_CODE_ASK_PERMISSION_FOR_DELETE_CALL_LOG);
                                }
                            },
                            null);
                } else {
                    ActivityCompat.requestPermissions(activity, neededPermissions, CONS.REQUEST_CODE_ASK_PERMISSION_FOR_DELETE_CALL_LOG);
                }
                return false;
            } else {
                return true;
            }
        }
    }
}
