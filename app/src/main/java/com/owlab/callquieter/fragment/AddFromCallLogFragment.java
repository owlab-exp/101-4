package com.owlab.callquieter.fragment;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.owlab.callquieter.CONS;
import com.owlab.callquieter.MainActivity;
import com.owlab.callquieter.R;
import com.owlab.callquieter.contentprovider.CallQuieterContentProvider;
import com.owlab.callquieter.contentprovider.CallQuieterDb;
import com.owlab.callquieter.contentprovider.CallQuieterDbHelper;
import com.owlab.callquieter.util.FabMoveOnListScroll;
import com.owlab.callquieter.util.Utils;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class AddFromCallLogFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {
    //public class AddFromCallLogFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{
//public class AddFromCallLogFragment extends ListFragment {
    public static final String TAG = AddFromCallLogFragment.class.getSimpleName();

    private SimpleCursorAdapter cursorAdapter;
    private static final int CALL_LOG_LOADER = 0;
    //private boolean isFabRotated = false;

    FloatingActionButton doneFab;
    Animation rotateForwardAppear;
    Animation rotateBackwardDisappear;

    CallQuieterDbHelper callQuieterDbHelper;
    HashMap<String, String> selectedNumberMap = new HashMap<>();
    HashMap<String, Long> selectedRowIdMap = new HashMap<>();

    private static final String KEY_SELECTED_NUMBER_MAP = "selectedNumberMap";
    private static final String KEY_SELECTED_ROW_ID_MAP = "selectedRowIdMap";

    public AddFromCallLogFragment() {
        Log.d(TAG, ">>>>> instantiated");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rotateForwardAppear = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_forward_appear);
        rotateBackwardDisappear = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_backward_disappear);
        callQuieterDbHelper = new CallQuieterDbHelper(getActivity());

        if(savedInstanceState != null) {
            Object saved = savedInstanceState.getSerializable(KEY_SELECTED_NUMBER_MAP);
            if(saved != null) {
                selectedNumberMap = (HashMap<String, String>) saved;
            }

            saved = savedInstanceState.getSerializable(KEY_SELECTED_ROW_ID_MAP);
            if(saved != null) {
                selectedRowIdMap = (HashMap<String, Long>) saved;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(KEY_SELECTED_NUMBER_MAP, selectedNumberMap);
        outState.putSerializable(KEY_SELECTED_ROW_ID_MAP, selectedRowIdMap);

        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, ">>>>> onCreateView called");
        View view = inflater.inflate(R.layout.add_from_call_log_layout, container, false);

        doneFab = (FloatingActionButton) view.findViewById(R.id.fab_done);
        doneFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedNumberMap.size() > 0) {
                    int numOfAdded = 0;
                    int numOfNotAdded = 0;
                    for (Map.Entry<String, String> entry : selectedNumberMap.entrySet()) {
                        ContentValues values = new ContentValues();
                        values.put(CallQuieterDb.COLS_REGISTERED_NUMBER.PHONE_NUMBER, entry.getKey());
                        values.put(CallQuieterDb.COLS_REGISTERED_NUMBER.DISPLAY_NAME, entry.getValue());
                        Uri newUri = getActivity().getContentResolver().insert(CallQuieterContentProvider.REGISTERED_NUMBER_URI, values);
                        if (Long.parseLong(newUri.getLastPathSegment()) > 0) {
                            //Toast.makeText(getActivity(), entry.getKey() + " added", Toast.LENGTH_SHORT).show();
                            numOfAdded++;
                        } else {
                            //Toast.makeText(getActivity(), entry.getKey() + " failed to add, duplicate?", Toast.LENGTH_SHORT).show();
                            numOfNotAdded++;
                        }
                    }
                    //Clear buckets
                    selectedNumberMap.clear();
                    selectedRowIdMap.clear();

                    if (numOfAdded > 0) {

                        Toast.makeText(getActivity(), numOfAdded + " " + (numOfAdded > 1 ? "phone numbers" : "phone number") + " added", Toast.LENGTH_SHORT).show();
                    }
                    if (numOfNotAdded > 0)
                        Toast.makeText(getActivity(), numOfNotAdded + " " + (numOfNotAdded > 1 ? "phone numbers" : "phone number") + " not added, duplicate?", Toast.LENGTH_SHORT).show();

                    getFragmentManager().popBackStack(ViewPagerContainerFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                } else {
                    //Toast.makeText(getActivity(), "No phone number selected", Toast.LENGTH_SHORT).show();
                    Snackbar.make(getView(), "No phone number selected", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        setupLoader(view);


        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        final ListView listView = getListView();
        listView.setOnScrollListener(new FabMoveOnListScroll(doneFab));

    }

    @Override
    public void onResume() {
        super.onResume();

        //if(isFabRotated) {
        doneFab.startAnimation(rotateForwardAppear);
        getListView().setOnItemClickListener(this);

        MainActivity mainActivity = (MainActivity)getActivity();
        ActionBar mainActionBar = mainActivity.getSupportActionBar();
        if(mainActionBar != null) {
            mainActionBar.setTitle(R.string.title_add_from_call_log);
            mainActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        doneFab.startAnimation(rotateBackwardDisappear);
    }

    final String[] COLUMNS = {
            CallLog.Calls._ID
            , CallLog.Calls.NUMBER
            , CallLog.Calls.TYPE
            , CallLog.Calls.DATE
            , CallLog.Calls.DURATION
            // , CallLog.Calls.NEW
            // , CallLog.Calls.COUNTRY_ISO
    };
    final int[] TO_IDS = new int[]{
            R.id.add_from_call_log_row_holder
            ////R.id.add_from_call_log_row_caller_icon
            //R.id.add_from_call_log_row_caller_info
            //, R.id.add_from_call_log_row_caller_type
            //, R.id.add_from_call_log_row_call_detail
            //, R.id.add_from_call_log_row_call_detail
    };

    private void setupLoader(final View fragmentView) {
        //This also works
        //cursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.add_from_call_log_row_layout, null, null, TO_GROUP_IDS, 0);
        cursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.add_from_call_log_row_layout, null, COLUMNS, TO_IDS, 0);
        cursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            ContentResolver contentResolver = getActivity().getContentResolver();
            //String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID};
            String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI};
            //String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.PHOTO_URI};

            @Override
            public boolean setViewValue(final View view, final Cursor cursor, int idIdx) {
                String phoneNumberRead = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
                String phoneNumberStripped = phoneNumberRead.replaceAll("[^\\d]", "");
                LinearLayout rowView = (LinearLayout) view.getParent();

                if (callQuieterDbHelper.isBlockedNumber(phoneNumberStripped)) {
                    //Already in blocked numbers
                    rowView.setBackgroundColor(Color.parseColor(CONS.ROW_COLOR_ALREADY_BLOCKED));
                } else {
                    //if (selectedNumberMap.containsKey(phoneNumberStripped)) {
                    if (selectedRowIdMap.containsValue(cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls._ID)))) {
                        rowView.setBackgroundColor(Color.parseColor(CONS.ROW_COLOR_SELECTED));
                    } else {
                        rowView.setBackgroundColor(Color.parseColor(CONS.ROW_COLOR_UNSELECTED));
                    }
                }

                ImageView photoView = (ImageView) view.findViewById(R.id.add_from_call_log_row_photo);
                TextView numberView = (TextView) view.findViewById(R.id.add_from_call_log_row_number);
                TextView nameView = (TextView) view.findViewById(R.id.add_from_call_log_row_name);


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
                            //contactId = contactsCursor.getLong(contactsCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
                            photoUriStr = contactsCursor.getString(contactsCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI));
                            //photoUriStr = contactsCursor.getString(contactsCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.PHOTO_URI));
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
                        //Log.d(TAG, ">>>>> set nameView: " + displayName);
                    } else {
                        nameView.setText("");
                    }
                }
                numberView.setText(Utils.formatPhoneNumber(getContext(), phoneNumberStripped));

                ImageView typeIV = (ImageView) view.findViewById(R.id.add_from_call_log_row_type);
                String type = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE));
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

                String dateStr = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE));

                long dateLong = Long.valueOf(dateStr);
                SimpleDateFormat dateFormat = new SimpleDateFormat();
                //TODO if today, then do simpler format

                TextView dateView = (TextView) view.findViewById(R.id.add_from_call_log_row_date);
                dateView.setText(dateFormat.format(dateLong));

                String duration = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION));
                TextView durationView = (TextView) view.findViewById(R.id.add_from_call_log_row_duration);
                durationView.setText(duration + " seconds");

                return true;
            }
        });

        setListAdapter(cursorAdapter);
        getLoaderManager().initLoader(CALL_LOG_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        //Log.d(TAG, ">>> onCreateLoader: laoderId: " + loaderId);

        CursorLoader cursorLoader = null;
        switch (loaderId) {
            case CALL_LOG_LOADER:
                cursorLoader = new CursorLoader(getActivity(), CallLog.Calls.CONTENT_URI, COLUMNS, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);
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
        //public void onListItemClick(ListView listView, View view, int position, long rowId) {
        //super.onListItemClick(listView, view, position, rowId);
        Log.d(TAG, ">>>>> a list item clicked: position = " + position + ", rowId = " + rowId);
        TextView numberView = (TextView) view.findViewById(R.id.add_from_call_log_row_number);
        TextView nameView = (TextView) view.findViewById(R.id.add_from_call_log_row_name);
        //String displayName = infoView.getText().toString();
        //TextView detailView = (TextView) view.findViewById(R.id.add_from_contacts_row_contact_detail);
        //String detail = detailView.getText().toString();
        String phoneNumberFormatted = numberView.getText().toString();
        String phoneNumber = phoneNumberFormatted.replaceAll("[^\\d]", "");
        String displayName = nameView.getText().toString();

        Log.d(TAG, ">>>>> phoneNumber: " + phoneNumber);

        if (callQuieterDbHelper.isBlockedNumber(phoneNumber)) {
            //Toast.makeText(getActivity(), phoneNumber + " already in the block list", Toast.LENGTH_SHORT).show();
            Snackbar.make(getView(), phoneNumber + " already in the block list", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (phoneNumber.trim().equals("")) {
            Snackbar.make(getView(), "Phone number is unknown", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (selectedNumberMap.containsKey(phoneNumber)) {
            if (rowId != selectedRowIdMap.get(phoneNumber)) {
                //Toast.makeText(getActivity(), phoneNumber + " already in the bucket", Toast.LENGTH_SHORT).show();
                Snackbar.make(getView(), phoneNumber + " already in the bucket", Snackbar.LENGTH_SHORT).show();
                return;
            } else {
                selectedNumberMap.remove(phoneNumber);
                selectedRowIdMap.remove(phoneNumber);
                Log.d(TAG, ">>>>> removed");
                view.setBackgroundColor(Color.parseColor(CONS.ROW_COLOR_UNSELECTED));
                //Toast.makeText(getActivity(), phoneNumber + " removed from the bucket", Toast.LENGTH_SHORT).show();
                Snackbar.make(getView(), phoneNumber + " removed from the bucket", Snackbar.LENGTH_SHORT).show();
            }
        } else {
            selectedNumberMap.put(phoneNumber, displayName);
            selectedRowIdMap.put(phoneNumber, rowId);
            Log.d(TAG, ">>>>> added");
            view.setBackgroundColor(Color.parseColor(CONS.ROW_COLOR_SELECTED));
            //Toast.makeText(getActivity(), phoneNumber + " added to the bucket", Toast.LENGTH_SHORT).show();
            Snackbar.make(getView(), phoneNumber + " added to the bucket", Snackbar.LENGTH_SHORT).show();
        }

    }
}
