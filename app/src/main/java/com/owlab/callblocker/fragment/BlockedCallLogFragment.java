package com.owlab.callblocker.fragment;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.owlab.callblocker.CONS;
import com.owlab.callblocker.R;
import com.owlab.callblocker.Utils;
import com.owlab.callblocker.contentprovider.CallBlockerDb;
import com.owlab.callblocker.contentprovider.CallBlockerDbHelper;
import com.owlab.callblocker.contentprovider.CallBlockerProvider;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Objects;

/**
 * List up blocked call log
 * Each row is expanded if clicked,to show "call" and "delete" button
 */
public class BlockedCallLogFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    public static final String TAG = BlockedCallLogFragment.class.getSimpleName();

    private SimpleCursorAdapter cursorAdapter;
    private static final int BLOCKED_CALL_LOG_LOADER = 0;
    //private boolean isFabRotated = false;

    FloatingActionButton enterFab;
    Animation rotateForwardAppear;
    Animation rotateBackwardDisappear;

    CallBlockerDbHelper callBlockerDbHelper;
    HashSet<Long> selectedRowIdSet = new HashSet<>();

    private final static String KEY_SELECTED_ROW_ID_SET = "selectedRowIdSet";

    public BlockedCallLogFragment() {
        //Log.d(TAG, ">>>>> instantiated");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, ">>>>> onCreate called with savedInstanceState: " + Objects.toString(savedInstanceState));

        if (savedInstanceState != null) {

            HashSet<Long> selectedRowIdSetSaved = (HashSet<Long>) savedInstanceState.getSerializable(KEY_SELECTED_ROW_ID_SET);
            if (selectedRowIdSetSaved != null) {
                //Log.d(TAG, ">>>>> restore selectedRowIdSet");
                selectedRowIdSet = selectedRowIdSetSaved;
            }
        }

        callBlockerDbHelper = new CallBlockerDbHelper(getActivity());
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        outState.putSerializable(KEY_SELECTED_ROW_ID_SET, selectedRowIdSet);
        Log.d(TAG, ">>>>> onSaveInstanceState called");

        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Log.d(TAG, ">>>>> onCreateView called");
        View view = inflater.inflate(R.layout.blocked_call_log_layout, container, false);

        enterFab = (FloatingActionButton) view.findViewById(R.id.fab_done);
        enterFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedRowIdSet.size() > 0) {
                    int numOfUpdated = 0;
                    //Long[] rowIds = new Long[selectedRowIdSet.size()];
                    //selectedRowIdSet.toArray(rowIds);
                    final ContentValues values = new ContentValues();
                    for (Long _id : selectedRowIdSet) {
                        values.put(CallBlockerDb.COLS_BLOCKED_CALL.MARK_DELETED, 1);
                        int updateCount = getActivity().getContentResolver().update(CallBlockerProvider.BLOCKED_CALL_URI, values, CallBlockerDb.COLS_BLOCKED_CALL._ID + " = " + _id, null);
                        //int deleteCount = getActivity().getContentResolver().delete(CallBlockerProvider.BLOCKED_CALL_URI, CallBlockerDb.COLS_BLOCKED_CALL._ID + " = ?", new String[]{Long.toString(_id)});
                        if (updateCount > 0) {
                            //Toast.makeText(getActivity(), entry.getKey() + " added", Toast.LENGTH_SHORT).show();
                            numOfUpdated++;
                        } else {
                        }
                        values.clear();
                    }


                    if (numOfUpdated > 0) {
                        //Toast.makeText(getActivity(), numOfDeleted + " " + (numOfDeleted > 1 ? "calls" : "call") + " deleted", Toast.LENGTH_SHORT).show();
                        final Snackbar snackbar = Snackbar.make(getView(), numOfUpdated + " " + (numOfUpdated > 1 ? "calls" : "call") + " deleted", Snackbar.LENGTH_LONG);

                        snackbar.setCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                super.onDismissed(snackbar, event);
                                if(event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                                    getActivity().getContentResolver().delete(CallBlockerProvider.BLOCKED_CALL_URI, CallBlockerDb.COLS_BLOCKED_CALL.MARK_DELETED + " > 1", null);
                                    selectedRowIdSet.clear();
                                }
                            }
                        });

                        snackbar.setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //recover
                                for (Long _id : selectedRowIdSet) {
                                    values.put(CallBlockerDb.COLS_BLOCKED_CALL.MARK_DELETED, 0);
                                    getActivity().getContentResolver().update(CallBlockerProvider.BLOCKED_CALL_URI, values, CallBlockerDb.COLS_BLOCKED_CALL._ID + " = " + _id, null);
                                    values.clear();
                                }
                            }
                        });

                        snackbar.show();
                    }
                } else {
                    Toast.makeText(getActivity(), "No call selected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        setupLoader(view);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, ">>>>> onResume called");

        getListView().setOnItemClickListener(this);
        getListView().setOnItemLongClickListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, ">>>>> onPause called");
        //enterFab.startAnimation(rotateBackwardDisappear);
        getListView().setOnItemClickListener(null);
        getListView().setOnItemLongClickListener(null);
    }

    final String[] FROM_COLUMNS = {
            CallBlockerDb.COLS_BLOCKED_CALL._ID
            , CallBlockerDb.COLS_BLOCKED_CALL.NUMBER
            , CallBlockerDb.COLS_BLOCKED_CALL.TYPE
            , CallBlockerDb.COLS_BLOCKED_CALL.DATE
            , CallBlockerDb.COLS_BLOCKED_CALL.DURATION
    };
    final int[] TO_IDS = new int[]{
            R.id.blocked_call_log_row_holder
    };

    private void setupLoader(final View fragmentView) {
        cursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.blocked_call_log_row_layout, null, FROM_COLUMNS, TO_IDS, 0);
        cursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            ContentResolver contentResolver = getActivity().getContentResolver();
            String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI};

            @Override
            public boolean setViewValue(final View view, final Cursor cursor, int idIdx) {

                Long rowId = cursor.getLong(cursor.getColumnIndexOrThrow(CallBlockerDb.COLS_BLOCKED_CALL._ID));
                String phoneNumberRead = cursor.getString(cursor.getColumnIndexOrThrow(CallBlockerDb.COLS_BLOCKED_CALL.NUMBER));
                String phoneNumberStripped = phoneNumberRead.replaceAll("[^\\d]", "");

                LinearLayout rowView = (LinearLayout) view.getParent();

                if (selectedRowIdSet.contains(rowId)) {
                    rowView.setBackgroundColor(Color.parseColor(CONS.ROW_COLOR_SELECTED));
                } else {
                    rowView.setBackgroundColor(Color.parseColor(CONS.ROW_COLOR_UNSELECTED));
                }

                ImageView photoView = (ImageView) view.findViewById(R.id.blocked_call_log_row_photo);
                TextView numberView = (TextView) view.findViewById(R.id.blocked_call_log_row_number);
                TextView nameView = (TextView) view.findViewById(R.id.blocked_call_log_row_name);


                if (phoneNumberStripped.equals("")) {
                    photoView.setImageResource(R.drawable.ic_contact_28);
                    nameView.setText("Private number");
                } else {
                    String displayName = null;
                    String photoUriStr = null;
                    //long contactId = -1l;
                    //Log.d(TAG, ">>>>> looking for: " + phoneNumber);
                    Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumberStripped));
                    //Log.d(TAG, ">>>>> uri: " + uri.toString());
                    Cursor contactsCursor = contentResolver.query(uri, projection, null, null, null);
                    if (contactsCursor != null) {
                        if (contactsCursor.getCount() > 0 && contactsCursor.moveToFirst()) {
                            displayName = contactsCursor.getString(contactsCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME));
                            photoUriStr = contactsCursor.getString(contactsCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI));
                        }
                        contactsCursor.close();
                    }

                    if (photoUriStr != null) {
                        photoView.setImageURI(Uri.parse(photoUriStr));
                    } else {
                        photoView.setImageResource(R.drawable.ic_contact_28);
                    }

                    if (displayName != null) {
                        nameView.setText(displayName);
                    } else {
                        nameView.setText("");
                    }
                }

                numberView.setText(Utils.formatPhoneNumber(phoneNumberStripped));

                ImageView typeIV = (ImageView) view.findViewById(R.id.blocked_call_log_row_type);
                String type = cursor.getString(cursor.getColumnIndexOrThrow(CallBlockerDb.COLS_BLOCKED_CALL.TYPE));

                switch (Integer.parseInt(type)) {
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

                String dateStr = cursor.getString(cursor.getColumnIndexOrThrow(CallBlockerDb.COLS_BLOCKED_CALL.DATE));

                long dateLong = Long.valueOf(dateStr);
                SimpleDateFormat dateFormat = new SimpleDateFormat();
                //TODO if today, then do simpler format

                TextView dateView = (TextView) view.findViewById(R.id.blocked_call_log_row_date);
                dateView.setText(dateFormat.format(dateLong));

                String duration = cursor.getString(cursor.getColumnIndexOrThrow(CallBlockerDb.COLS_BLOCKED_CALL.DURATION));
                TextView durationView = (TextView) view.findViewById(R.id.blocked_call_log_row_duration);
                durationView.setText(duration + " seconds");

                return true;
            }
        });

        setListAdapter(cursorAdapter);
        getLoaderManager().initLoader(BLOCKED_CALL_LOG_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        Log.d(TAG, ">>> onCreateLoader: laoderId: " + loaderId);

        CursorLoader cursorLoader = null;
        switch (loaderId) {
            case BLOCKED_CALL_LOG_LOADER:
                String selection = CallBlockerDb.COLS_BLOCKED_CALL.MARK_DELETED + " = 0";
                cursorLoader = new CursorLoader(getActivity(), CallBlockerProvider.BLOCKED_CALL_URI, null, selection, null, CallBlockerDb.COLS_BLOCKED_CALL.DATE + " DESC");
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

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long rowId) {

        if (selectedRowIdSet.contains(rowId)) {

            view.setBackgroundColor(Color.parseColor(CONS.ROW_COLOR_UNSELECTED));
            selectedRowIdSet.remove(rowId);

            Toast.makeText(getActivity(), "Call removed from the bucket", Toast.LENGTH_SHORT).show();
        } else {

            view.setBackgroundColor(Color.parseColor(CONS.ROW_COLOR_SELECTED));
            selectedRowIdSet.add(rowId);

            Toast.makeText(getActivity(), "Call added to the bucket", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long rowId) {
        TextView numberView = (TextView) view.findViewById(R.id.blocked_call_log_row_number);
        String phoneNumberRead = numberView.getText().toString();
        String phoneNumberStripped = phoneNumberRead.replaceAll("[^\\d]", "");
        if(phoneNumberStripped.isEmpty()) {
            Toast.makeText(getActivity(), "Empty number", Toast.LENGTH_SHORT).show();
        } else {
            //Permission check is performed in the main activity before this view shown
            if (PermissionChecker.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                //if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CALL_PHONE)) {
                //    FUNS.showMessageWithOKCancel(
                //            getActivity(),
                //            "This App need CALL PHONE permission to make a call",
                //            new DialogInterface.OnClickListener() {
                //                @Override
                //                public void onClick(DialogInterface dialogInterface, int i) {
                //                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, CONS.REQUEST_CODE_ASK_PERMISSION_FOR_MAKE_CALL);
                //                }
                //            },
                //            null);
                //} else {
                //    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, CONS.REQUEST_CODE_ASK_PERMISSION_FOR_MAKE_CALL);
                //}
                Toast.makeText(getActivity(), "No PHONE CALL permission", Toast.LENGTH_SHORT).show();
            } else {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + phoneNumberStripped));
                startActivity(callIntent);
            }
        }
        return true;
    }


}
