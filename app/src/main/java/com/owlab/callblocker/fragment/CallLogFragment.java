package com.owlab.callblocker.fragment;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.owlab.callblocker.R;

/**
 * A placeholder fragment containing a simple view.
 */
//public class CallLogFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
public class CallLogFragment extends ListFragment {
    private static final String TAG = CallLogFragment.class.getSimpleName();

    //private SimpleCursorAdapter cursorAdapter;
    //private static final int DB_LOADER = 0;
    //private boolean isFabRotated = false;

    public CallLogFragment() {
        Log.d(TAG, ">>>>> instantiated");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, ">>>>> onCreateView called");
        View view = inflater.inflate(R.layout.call_history_layout, container, false);

        ////Floating Action Button
        //final FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab_check);
        //fab.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View view) {
        //        Animation rotateForward = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_forward);
        //        fab.startAnimation(rotateForward);
        //        isFabRotated = true;

        //        //Intent startAddActivityIntent = new Intent(getActivity(), AddActivity.class);
        //        //getActivity().startActivity(startAddActivityIntent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
        //    }
        //});

        ////Animation fabOpen = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_open);
        ////fab.startAnimation(fabOpen);

        //setLoader(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        //if(isFabRotated) {
        //    final FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab_add);
        //    Animation rotateBackward = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_backward);
        //    fab.startAnimation(rotateBackward);
        //    //Over coding?
        //    isFabRotated = false;
        //}
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /**
    private void setLoader(final View fragmentView) {
        final String[] columns = {
                CallBlockerTbl.Schema.COLUMN_NAME_PHONE_NUMBER,
                CallBlockerTbl.Schema.COLUMN_NAME_DESCRIPTION,
                CallBlockerTbl.Schema.COLUMN_NAME_IS_ACTIVE,
                CallBlockerTbl.Schema.COLUMN_NAME_CREATED_AT
        };
        final int[] rowItems = new int[]{
                R.id.phone_number_list_row_phone_number,
                R.id.phone_number_list_row_description,
                R.id.phone_number_list_row_is_active_switch,
                R.id.phone_number_list_row_delete_icon
        };

        cursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.phone_list_row_layout, null, columns, rowItems, 0);
        cursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            private int _idColumnIndex = -1;
            private int phoneNumberColumnIndex = -1;
            private int descriptionColumnIndex = -1;
            private int isActiveColumnIndex = -1;
            @Override
            public boolean setViewValue(final View view, final Cursor cursor, int columnIndex) {
                //final int _id = cursor.getInt(cursor.getColumnIndexOrThrow(CallBlockerTbl.Schema._ID));
                if(_idColumnIndex == -1)
                    _idColumnIndex = cursor.getColumnIndexOrThrow(CallBlockerTbl.Schema._ID);
                if(phoneNumberColumnIndex == -1)
                    phoneNumberColumnIndex = cursor.getColumnIndexOrThrow(CallBlockerTbl.Schema.COLUMN_NAME_PHONE_NUMBER);
                if(descriptionColumnIndex == -1)
                    descriptionColumnIndex = cursor.getColumnIndexOrThrow(CallBlockerTbl.Schema.COLUMN_NAME_DESCRIPTION);
                if(isActiveColumnIndex == -1)
                    isActiveColumnIndex = cursor.getColumnIndexOrThrow(CallBlockerTbl.Schema.COLUMN_NAME_IS_ACTIVE);

                if(columnIndex == phoneNumberColumnIndex) {
                    //Log.d(TAG, ">>>>> phone number formatting...");
                    TextView phoneNumberTextView = (TextView) view;
                    //If addTextChangedListener needed, make it clear that this call happens only once per the textview
                    //phoneNumberTextView.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
                    phoneNumberTextView.setText(Utils.formatPhoneNumber(cursor.getString(phoneNumberColumnIndex)));
                    //phoneNumberTextView.setText(cursor.getString(phoneNumberColumnIndex));
                    return true;
                }

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
                            changeDescriptionDialogFragment.setTargetFragment(CallLogFragment.this, 0);
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
                            deletePhoneDialogFragment.setTargetFragment(CallLogFragment.this, 0);
                            deletePhoneDialogFragment.show(getFragmentManager(), "tag_delete_phone_dialog");
                        }
                    });
                    return true;
                }

                return false;
            }
        });

        setListAdapter(cursorAdapter);
        getLoaderManager().initLoader(DB_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        CursorLoader cursorLoader = null;
        switch(loaderId) {
            case DB_LOADER:
                String[] projection = {
                        CallBlockerTbl.Schema._ID,
                        CallBlockerTbl.Schema.COLUMN_NAME_PHONE_NUMBER,
                        CallBlockerTbl.Schema.COLUMN_NAME_DESCRIPTION,
                        CallBlockerTbl.Schema.COLUMN_NAME_IS_ACTIVE,
                        CallBlockerTbl.Schema.COLUMN_NAME_CREATED_AT
                };
                cursorLoader = new CursorLoader(this.getActivity(), CallBlockerContentProvider.CONTENT_URI, projection, null, null, null);
                break;
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
    */
}
