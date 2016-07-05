package com.owlab.callquieter.fragment;

import android.app.ActivityOptions;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.owlab.callquieter.AddSourceSelectionActivity;
import com.owlab.callquieter.CONS;
import com.owlab.callquieter.R;
import com.owlab.callquieter.contentprovider.CallQuieterDb;
import com.owlab.callquieter.util.Utils;
import com.owlab.callquieter.contentprovider.CallQuieterContentProvider;

/**
 * A placeholder fragment containing a simple view.
 */
public class RegisteredNumberListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = RegisteredNumberListFragment.class.getSimpleName();

    private SimpleCursorAdapter cursorAdapter;
    private static final int DB_LOADER = 0;
    private boolean isFabRotated = false;

    public RegisteredNumberListFragment() {
        Log.d(TAG, ">>>>> instantiated");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Log.d(TAG, ">>>>> onCreateView called");
        View view = inflater.inflate(R.layout.registered_number_list_layout, container, false);

        //Floating Action Button
        final FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //final OvershootInterpolator interpolator = new OvershootInterpolator();
                //ViewCompat.animate(fab).rotation(45f).withLayer().setDuration(300).setInterpolator(interpolator).start();
                Animation rotateForward = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_forward_disappear);
                fab.startAnimation(rotateForward);
                isFabRotated = true;

                Intent startAddActivityIntent = new Intent(getActivity(), AddSourceSelectionActivity.class);
                startAddActivityIntent.putExtra(CONS.INTENT_KEY_TRANSITION_SOURCE, ViewPagerContainerFragment.TAG);

                //getActivity().startActivity(startAddActivityIntent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                //To return the enclosing activity of this
                getActivity().startActivityForResult(startAddActivityIntent, CONS.REQUEST_CODE_ADD_SOURCE_SELECTION, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            }
        });

        //Animation fabOpen = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_open);
        //fab.startAnimation(fabOpen);

        setLoader(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isFabRotated) {
            final FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab_add);
            Animation rotateBackward = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_backward_disappear);
            fab.startAnimation(rotateBackward);
            //Over coding?
            isFabRotated = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private final String[] FROM_COLUMNS = {
            CallQuieterDb.COLS_REGISTERED_NUMBER._ID
            , CallQuieterDb.COLS_REGISTERED_NUMBER.PHONE_NUMBER
            , CallQuieterDb.COLS_REGISTERED_NUMBER.DISPLAY_NAME
            , CallQuieterDb.COLS_REGISTERED_NUMBER.MATCH_METHOD
            , CallQuieterDb.COLS_REGISTERED_NUMBER.IS_ACTIVE
            //, CallQuieterDb.COLS_REGISTERED_NUMBER.CREATED_AT
    };

    private final int[] TO_IDS = new int[]{
            R.id.phone_number_list_row_holder
            //, R.id.phone_number_list_row_holder
            //, R.id.phone_number_list_row_holder
            //, R.id.phone_number_list_row_holder
            ////R.id.phone_number_list_row_phone_number,
            ////R.id.phone_number_list_row_description,
            ////R.id.phone_number_list_row_is_active_switch,
            ////R.id.phone_number_list_row_delete_icon
    };

    private void setLoader(final View fragmentView) {
        cursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.registered_number_list_row_layout, null, FROM_COLUMNS, TO_IDS, 0);
        cursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            //private int _idColumnIndex = -1;
            //private int phoneNumberColumnIndex = -1;
            //private int displayNameColumnIndex = -1;
            //private int isActiveColumnIndex = -1;
            @Override
            public boolean setViewValue(final View view, final Cursor cursor, int idIndex) {
                final int _id = cursor.getInt(cursor.getColumnIndexOrThrow(CallQuieterDb.COLS_REGISTERED_NUMBER._ID));
                final String phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(CallQuieterDb.COLS_REGISTERED_NUMBER.PHONE_NUMBER));
                final String displayName = cursor.getString(cursor.getColumnIndexOrThrow(CallQuieterDb.COLS_REGISTERED_NUMBER.DISPLAY_NAME));
                final int matchMethod = cursor.getInt(cursor.getColumnIndexOrThrow(CallQuieterDb.COLS_REGISTERED_NUMBER.MATCH_METHOD));
                final boolean checkedRead = cursor.getInt(cursor.getColumnIndexOrThrow(CallQuieterDb.COLS_REGISTERED_NUMBER.IS_ACTIVE)) > 0;

                LinearLayout nonExactMatchLayout = (LinearLayout) view.findViewById(R.id.phone_number_list_row_non_exact_match);
                switch(matchMethod) {
                    case CONS.MATCH_METHOD_EXACT: nonExactMatchLayout.setVisibility(View.INVISIBLE); break;
                    case CONS.MATCH_METHOD_STARTS_WITH: nonExactMatchLayout.setVisibility(View.VISIBLE); break;
                }

                TextView phoneNumberTV = (TextView) view.findViewById(R.id.phone_number_list_row_phone_number);
                phoneNumberTV.setText(Utils.formatPhoneNumber(getContext(), phoneNumber));

                TextView displayNameTV = (TextView) view.findViewById(R.id.phone_number_list_row_display_name);

                //Here defensive code
                displayNameTV.setOnClickListener(null);
                displayNameTV.setText(displayName);
                displayNameTV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View descriptionView) {
                        EditDisplayNameDialogFragment editDisplayNameDialogFragment = new EditDisplayNameDialogFragment();
                        Bundle argument = new Bundle();
                        argument.putInt(CONS.ARG_KEY_REGISTERED_NUMBER_ID, _id);
                        argument.putString(CONS.ARG_KEY_REGISTERED_NUMBER, phoneNumber);
                        argument.putString(CONS.ARG_KEY_DISPLAY_NAME, displayName);
                        editDisplayNameDialogFragment.setArguments(argument);
                        editDisplayNameDialogFragment.setTargetFragment(RegisteredNumberListFragment.this, 0);
                        editDisplayNameDialogFragment.show(getActivity().getSupportFragmentManager(), "tag_change_description_diag");
                    }
                });

                Switch isActiveSwitch = (Switch) view.findViewById(R.id.phone_number_list_row_is_active_switch);
                //This is necessary not to set multiple OnCheckedChangeListener!
                isActiveSwitch.setOnCheckedChangeListener(null);
                isActiveSwitch.setChecked(checkedRead);
                isActiveSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean checkedChanged) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(CallQuieterDb.COLS_REGISTERED_NUMBER.IS_ACTIVE, checkedChanged ? 1 : 0);
                        int updateCount = getActivity().getContentResolver().update(
                                CallQuieterContentProvider.REGISTERED_NUMBER_URI,
                                contentValues, CallQuieterDb.COLS_REGISTERED_NUMBER._ID + " = " + _id,
                                null);
                        if (updateCount > 0) {

                            Snackbar.make(fragmentView, "Quieting " + (checkedChanged ? "enabled" : "disabled") + " for " + phoneNumber, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });

                ImageView deleteIconView = (ImageView) view.findViewById(R.id.phone_number_list_row_delete_icon);
                //Here defensive code
                deleteIconView.setOnClickListener(null);
                deleteIconView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DialogFragment deletePhoneDialogFragment = new DeleteDialogFragment();
                        Bundle argument = new Bundle();
                        argument.putInt(CONS.ARG_KEY_REGISTERED_NUMBER_ID, _id);
                        argument.putString(CONS.ARG_KEY_REGISTERED_NUMBER, phoneNumber);
                        argument.putString(CONS.ARG_KEY_DISPLAY_NAME, displayName);
                        deletePhoneDialogFragment.setArguments(argument);
                        deletePhoneDialogFragment.setTargetFragment(RegisteredNumberListFragment.this, 0);
                        deletePhoneDialogFragment.show(getActivity().getSupportFragmentManager(), "tag_delete_phone_dialog");
                    }
                });

                return true;
            }
        });

        setListAdapter(cursorAdapter);
        getLoaderManager().initLoader(DB_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        CursorLoader cursorLoader = null;
        switch (loaderId) {
            case DB_LOADER:
                String[] projection = {
                        CallQuieterDb.COLS_REGISTERED_NUMBER._ID
                        , CallQuieterDb.COLS_REGISTERED_NUMBER.PHONE_NUMBER
                        , CallQuieterDb.COLS_REGISTERED_NUMBER.DISPLAY_NAME
                        , CallQuieterDb.COLS_REGISTERED_NUMBER.MATCH_METHOD
                        , CallQuieterDb.COLS_REGISTERED_NUMBER.IS_ACTIVE
                };
                String selection = CallQuieterDb.COLS_REGISTERED_NUMBER.MARK_DELETED + " = 0";
                cursorLoader = new CursorLoader(this.getActivity(), CallQuieterContentProvider.REGISTERED_NUMBER_URI, projection, selection, null, null);
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
}
