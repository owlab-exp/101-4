package com.owlab.callblocker.fragment;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
import com.owlab.callblocker.content.CallBlockerDb;
import com.owlab.callblocker.content.CallBlockerDbHelper;
import com.owlab.callblocker.content.CallBlockerProvider;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * List up blocked call log
 * Each row is expanded if clicked,to show "call" and "delete" button
 */
public class BlockedCallLogFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {
//public class AddFromCallLogFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{
//public class AddFromCallLogFragment extends ListFragment {
    private static final String TAG = BlockedCallLogFragment.class.getSimpleName();

    private SimpleCursorAdapter cursorAdapter;
    private static final int BLOCKED_CALL_LOG_LOADER = 0;
    //private boolean isFabRotated = false;

    FloatingActionButton enterFab;
    Animation rotateForwardAppear;
    Animation rotateBackwardDisappear;

    CallBlockerDbHelper callBlockerDbHelper;
    Map<String, String> selectedPhoneMap = new HashMap<>();
    Map<String, Long> selectedPhoneRowIdMap = new HashMap<>();

    public BlockedCallLogFragment() {
        //Log.d(TAG, ">>>>> instantiated");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, ">>>>> onCreate called with savedInstanceState: " + Objects.toString(savedInstanceState));

        callBlockerDbHelper = new CallBlockerDbHelper(getActivity());
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, ">>>>> onSaveInstanceState called");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Log.d(TAG, ">>>>> onCreateView called");
        View view = inflater.inflate(R.layout.blocked_call_log_layout, container, false);

        /**
        enterFab = (FloatingActionButton) view.findViewById(R.id.fab_done);
        enterFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedPhoneMap.size() > 0) {
                    int numOfAdded = 0;
                    int numOfNotAdded = 0;
                    for(Map.Entry<String, String> entry : selectedPhoneMap.entrySet()) {
                        ContentValues values = new ContentValues();
                        values.put(CallBlockerDb.COLS_BLOCKED_NUMBER.PHONE_NUMBER, entry.getKey());
                        values.put(CallBlockerDb.COLS_BLOCKED_NUMBER.DISPLAY_NAME, entry.getValue());
                        Uri newUri = getActivity().getContentResolver().insert(CallBlockerProvider.BLOCKED_NUMBER_URI, values);
                        if(Long.parseLong(newUri.getLastPathSegment()) > 0) {
                            //Toast.makeText(getActivity(), entry.getKey() + " added", Toast.LENGTH_SHORT).show();
                            numOfAdded++;
                        } else {
                            //Toast.makeText(getActivity(), entry.getKey() + " failed to add, duplicate?", Toast.LENGTH_SHORT).show();
                            numOfNotAdded++;
                        }
                    }
                    //Clear buckets
                    selectedPhoneMap.clear();
                    selectedPhoneRowIdMap.clear();
                    if(numOfAdded > 0)
                        Toast.makeText(getActivity(), numOfAdded + " " + (numOfAdded > 1 ? "phone numbers":"phone number") + " added", Toast.LENGTH_SHORT).show();
                    if(numOfNotAdded > 0)
                        Toast.makeText(getActivity(), numOfNotAdded + " " + (numOfNotAdded > 1 ? "phone numbers":"phone number") + " not added, duplicate?", Toast.LENGTH_SHORT).show();
                    getFragmentManager().popBackStack(CONS.FRAGMENT_VIEW_PAGER_CONTAINER, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                } else {
                    Toast.makeText(getActivity(), "No phone number selected", Toast.LENGTH_SHORT).show();
                }
            }
        });
         */

        setupLoader(view);



        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, ">>>>> onResume called");

        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, ">>>>> onPause called");
        //enterFab.startAnimation(rotateBackwardDisappear);
        getListView().setOnItemClickListener(null);
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

                String phoneNumberRead = cursor.getString(cursor.getColumnIndexOrThrow(CallBlockerDb.COLS_BLOCKED_CALL.NUMBER));
                String phoneNumberStripped = phoneNumberRead.replaceAll("[^\\d]", "");

                LinearLayout rowView = (LinearLayout) view.getParent();

                if(selectedPhoneMap.containsKey(phoneNumberStripped)) {
                    rowView.setBackgroundColor(Color.parseColor(CONS.ROW_COLOR_SELECTED));
                } else {
                    rowView.setBackgroundColor(Color.parseColor(CONS.ROW_COLOR_UNSELECTED));
                }

                ImageView photoView = (ImageView) view.findViewById(R.id.blocked_call_log_row_photo);
                TextView numberView = (TextView) view.findViewById(R.id.blocked_call_log_row_number);
                TextView nameView = (TextView) view.findViewById(R.id.blocked_call_log_row_name);


                    if(phoneNumberStripped.equals("")) {
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

                        if(displayName != null) {
                            nameView.setText(displayName);
                        } else {
                            nameView.setText("");
                        }
                    }

                    numberView.setText(Utils.formatPhoneNumber(phoneNumberStripped));

                    ImageView typeIV = (ImageView) view.findViewById(R.id.blocked_call_log_row_type);
                    String type = cursor.getString(cursor.getColumnIndexOrThrow(CallBlockerDb.COLS_BLOCKED_CALL.TYPE));

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
        switch(loaderId) {
            case BLOCKED_CALL_LOG_LOADER:
                cursorLoader = new CursorLoader(getActivity(), CallBlockerProvider.BLOCKED_CALL_URI, null, null, null, CallBlockerDb.COLS_BLOCKED_CALL.DATE + " DESC");
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
        //Log.d(TAG, ">>>>> a list item clicked: position = " + position + ", rowId = " + rowId);
        TextView numberView = (TextView) view.findViewById(R.id.blocked_call_log_row_number);
        TextView nameView = (TextView) view.findViewById(R.id.blocked_call_log_row_name);
        //String displayName = infoView.getText().toString();
        //TextView detailView = (TextView) view.findViewById(R.id.add_from_contacts_row_contact_detail);
        //String detail = detailView.getText().toString();
        String phoneNumberRead = numberView.getText().toString();
        String phoneNumberStripped = phoneNumberRead.replaceAll("[^\\d]", "");
        String displayName = nameView.getText().toString();

        //Log.d(TAG, ">>>>> phoneNumber: " + phoneNumber);

        //if(callBlockerDbHelper.isBlockedNumber(phoneNumber)) {
        //    Toast.makeText(getActivity(), phoneNumber + " already in the block list", Toast.LENGTH_SHORT).show();
        //    return;
        //}

        //if(phoneNumberStripped.equals("")) {
        //    Toast.makeText(getActivity(), "Phone number is unknown", Toast.LENGTH_SHORT).show();
        //    return;
        //}

        if(selectedPhoneMap.containsKey(phoneNumberStripped)) {
            if(rowId != selectedPhoneRowIdMap.get(phoneNumberStripped)) {
                Toast.makeText(getActivity(), phoneNumberStripped + " already in the bucket", Toast.LENGTH_SHORT).show();
                return;
            } else {
                selectedPhoneMap.remove(phoneNumberStripped);
                selectedPhoneRowIdMap.remove(phoneNumberStripped);
                Log.d(TAG, ">>>>> removed");
                view.setBackgroundColor(Color.parseColor(CONS.ROW_COLOR_UNSELECTED));
                Toast.makeText(getActivity(), phoneNumberStripped + " removed from the bucket", Toast.LENGTH_SHORT).show();
            }
        } else {
            selectedPhoneMap.put(phoneNumberStripped, displayName);
            selectedPhoneRowIdMap.put(phoneNumberStripped, rowId);
            Log.d(TAG, ">>>>> added");
            view.setBackgroundColor(Color.parseColor(CONS.ROW_COLOR_SELECTED));
            Toast.makeText(getActivity(), phoneNumberStripped + " added to the bucket", Toast.LENGTH_SHORT).show();
        }

    }
}
