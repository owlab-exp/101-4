package com.owlab.callblocker.fragment;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.owlab.callblocker.R;
import com.owlab.callblocker.Utils;

import java.text.SimpleDateFormat;

/**
 */
public class AddFromCallLogFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
//public class AddFromCallLogFragment extends ListFragment {
    private static final String TAG = AddFromCallLogFragment.class.getSimpleName();

    private SimpleCursorAdapter cursorAdapter;
    private static final int CALL_LOG_LOADER = 0;
    //private boolean isFabRotated = false;

    FloatingActionButton enterFab;
    Animation rotateForwardAppear;
    Animation rotateBackwardDisappear;

    public AddFromCallLogFragment() {
        Log.d(TAG, ">>>>> instantiated");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rotateForwardAppear = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_forward_appear);
        rotateBackwardDisappear = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_backward_disappear);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, ">>>>> onCreateView called");
        View view = inflater.inflate(R.layout.add_from_call_log_layout, container, false);

        enterFab = (FloatingActionButton) view.findViewById(R.id.fab_enter);
        ////Floating Action Button
        //final FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab_check);
        //fab.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View view) {
        //        Animation rotateForward = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_forward);
        //        fab.startAnimation(rotateForward);
        //        isFabRotated = true;

        //        //Intent startAddActivityIntent = new Intent(getActivity(), AddSourceSelectionActivity.class);
        //        //getActivity().startActivity(startAddActivityIntent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
        //    }
        //});

        ////Animation fabOpen = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_open);
        ////fab.startAnimation(fabOpen);

        setLoader(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        //if(isFabRotated) {
        enterFab.startAnimation(rotateForwardAppear);
    }

    @Override
    public void onPause() {
        super.onPause();

        enterFab.startAnimation(rotateBackwardDisappear);
    }

    private void setLoader(final View fragmentView) {
        final String[] columns = {
                CallLog.Calls._ID
                , CallLog.Calls.NUMBER
                , CallLog.Calls.TYPE
                , CallLog.Calls.DATE
                , CallLog.Calls.DURATION
               // , CallLog.Calls.NEW
               // , CallLog.Calls.COUNTRY_ISO
        };
        final int[] rowItems = new int[]{
                R.id.add_from_call_log_row_caller_icon
                , R.id.add_from_call_log_row_caller_info
                , R.id.add_from_call_log_row_caller_type
                , R.id.add_from_call_log_row_call_detail
                , R.id.add_from_call_log_row_call_detail
        };

        cursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.add_from_call_log_row_layout, null, columns, rowItems, 0);
        cursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            private int idColumnIndex = -1;
            private int numberColumnIndex = -1;
            private int typeColumnIndex = -1;
            private int dateColumnIndex = -1;
            private int durationColumnIndex = -1;
            //private int newColumnIndex = -1;
            //private int countryISOColumnIndex = -1;

            @Override
            public boolean setViewValue(final View view, final Cursor cursor, int columnIndex) {
                //final int _id = cursor.getInt(cursor.getColumnIndexOrThrow(CallBlockerTbl.Schema._ID));
                if(idColumnIndex == -1)
                    idColumnIndex = cursor.getColumnIndexOrThrow(CallLog.Calls._ID);
                if(numberColumnIndex == -1)
                    numberColumnIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER);
                if(typeColumnIndex == -1)
                    typeColumnIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE);
                if(dateColumnIndex == -1)
                    dateColumnIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.DATE);
                if(durationColumnIndex == -1)
                    durationColumnIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION);
                //if(newColumnIndex == -1)
                //    newColumnIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.NEW);
                //if(countryISOColumnIndex == -1)
                //    countryISOColumnIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.COUNTRY_ISO);

                if(columnIndex == idColumnIndex) {

                    return true;
                }

                if(columnIndex == numberColumnIndex) {
                    //Log.d(TAG, ">>>>> phone number formatting...");
                    TextView callerInfoTextView = (TextView) view;
                    //If addTextChangedListener needed, make it clear that this call happens only once per the textview
                    //phoneNumberTextView.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
                    callerInfoTextView.setText(Utils.formatPhoneNumber(cursor.getString(numberColumnIndex)));
                    //phoneNumberTextView.setText(cursor.getString(phoneNumberColumnIndex));
                    return true;
                }

                if(columnIndex == typeColumnIndex) {
                    ImageView typeIV = (ImageView) view;

                    String type = cursor.getString(typeColumnIndex);
                    switch(Integer.parseInt(type)) {
                        case CallLog.Calls.OUTGOING_TYPE:
                            //detailTextView.setText("Outgoing call");
                            typeIV.setImageResource(R.drawable.ic_call_made_black_18dp);
                            break;
                        case CallLog.Calls.INCOMING_TYPE:
                            //detailTextView.setText("Incoming call");
                            typeIV.setImageResource(R.drawable.ic_call_received_black_18dp);
                            break;
                        case CallLog.Calls.MISSED_TYPE:
                            //detailTextView.setText("Missed call");
                            typeIV.setImageResource(R.drawable.ic_call_missed_black_18dp);
                            break;
                    }
                    return true;
                }

                if(columnIndex == dateColumnIndex) {
                    String dateStr = cursor.getString(dateColumnIndex);

                    long dateLong = Long.valueOf(dateStr);
                    //Date date = new Date(dateLong);
                    SimpleDateFormat dateFormat = new SimpleDateFormat();
                    //dateFormat.format(dateLong);

                    TextView detailTextView = (TextView) view;
                    detailTextView.append(dateFormat.format(dateLong));
                    return true;
                }

                if(columnIndex == durationColumnIndex) {
                    TextView detailTextView = (TextView) view;
                    detailTextView.append("\n" + cursor.getString(durationColumnIndex) + " sec");
                    return true;
                }

                /**
                if(columnIndex == descriptionColumnIndex) {
                    final int _id = cursor.getInt(_idColumnIndex);
                    final String phoneNumber = cursor.getString(phoneNumberColumnIndex);
                    final String description = cursor.getString(descriptionColumnIndex);
                    TextView descriptionTextView = (TextView) view;
                    descriptionTextView.setText(description);
                    descriptionTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View descriptionView) {
                            ChangeDescriptionDialogFragment changeDescriptionDialogFragment = new ChangeDescriptionDialogFragment();
                            Bundle argument = new Bundle();
                            argument.putInt("_id", _id);
                            argument.putString("phoneNumber", phoneNumber);
                            argument.putString("description", description);
                            changeDescriptionDialogFragment.setArguments(argument);
                            changeDescriptionDialogFragment.setTargetFragment(AddFromCallLogFragment.this, 0);
                            changeDescriptionDialogFragment.show(getFragmentManager(), "tag_change_description_diag");
                        }
                    });
                    return true;
                }

                if(columnIndex == isActiveColumnIndex) {
                    final int _id = cursor.getInt(_idColumnIndex);
                    final String phoneNumber = cursor.getString(phoneNumberColumnIndex);
                    Switch isActiveSwitch = (Switch) view;
                    isActiveSwitch.setChecked(cursor.getInt(isActiveColumnIndex) > 0);
                    isActiveSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(CallBlockerTbl.Schema.COLUMN_NAME_IS_ACTIVE, isChecked ? 1:0);
                            int updateCount = getActivity().getContentResolver().update(
                                    CallBlockerContentProvider.CONTENT_URI,
                                    contentValues, CallBlockerTbl.Schema._ID + " = " + _id,
                                    null);
                            if(updateCount > 0) {
                                //TODO test if getRootView is right
                                Snackbar.make(fragmentView, "Blocking " + (isChecked ? "enabled" : "disabled") + " for " + phoneNumber, Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });
                    return true;
                }

                if(view.getId() == R.id.phone_number_list_row_delete_icon) {
                    final int _id = cursor.getInt(_idColumnIndex);
                    final String phoneNumber = cursor.getString(phoneNumberColumnIndex);
                    final String description = cursor.getString(descriptionColumnIndex);
                    ImageView deleteIconView = (ImageView) view;
                    deleteIconView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DialogFragment deletePhoneDialogFragment = new DeletePhoneDialogFragment();
                            Bundle argument = new Bundle();
                            argument.putInt("_id", _id);
                            argument.putString("phoneNumber", phoneNumber);
                            argument.putString("description", description);
                            deletePhoneDialogFragment.setArguments(argument);
                            deletePhoneDialogFragment.setTargetFragment(AddFromCallLogFragment.this, 0);
                            deletePhoneDialogFragment.show(getFragmentManager(), "tag_delete_phone_dialog");
                        }
                    });
                    return true;
                }

                */
                return false;
            }
        });

        setListAdapter(cursorAdapter);
        getLoaderManager().initLoader(CALL_LOG_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        Log.d(TAG, ">>> onCreateLoader: laoderId: " + loaderId);

        CursorLoader cursorLoader = null;
        switch(loaderId) {
            case CALL_LOG_LOADER:
                cursorLoader = new CursorLoader(getActivity(), CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC");
                break;
            default:
                Log.e(TAG, ">>>>> Loader ID not recognized: " + loaderId);
        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        cursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }
}
