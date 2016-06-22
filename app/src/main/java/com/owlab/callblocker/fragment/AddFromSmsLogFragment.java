package com.owlab.callblocker.fragment;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
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
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.owlab.callblocker.CONS;
import com.owlab.callblocker.MainActivity;
import com.owlab.callblocker.R;
import com.owlab.callblocker.Utils;
import com.owlab.callblocker.content.CallBlockerDb;
import com.owlab.callblocker.content.CallBlockerDbHelper;
import com.owlab.callblocker.content.CallBlockerProvider;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class AddFromSmsLogFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    //public class AddFromCallLogFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{
//public class AddFromCallLogFragment extends ListFragment {
    private static final String TAG = AddFromSmsLogFragment.class.getSimpleName();

    private SimpleCursorAdapter cursorAdapter;
    private static final int SMS_LOG_LOADER = 0;
    //private boolean isFabRotated = false;

    FloatingActionButton enterFab;
    Animation rotateForwardAppear;
    Animation rotateBackwardDisappear;

    CallBlockerDbHelper callBlockerDbHelper;
    Map<String, String> selectedPhoneMap = new HashMap<>();
    Map<String, Long> selectedPhoneRowIdMap = new HashMap<>();

    public AddFromSmsLogFragment() {
        //Log.d(TAG, ">>>>> instantiated");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rotateForwardAppear = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_forward_appear);
        rotateBackwardDisappear = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_backward_disappear);
        callBlockerDbHelper = new CallBlockerDbHelper(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, ">>>>> onCreateView called");
        View view = inflater.inflate(R.layout.add_from_sms_log_layout, container, false);

        enterFab = (FloatingActionButton) view.findViewById(R.id.fab_done);
        enterFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedPhoneMap.size() > 0) {
                    int numOfAdded = 0;
                    int numOfNotAdded = 0;
                    for (Map.Entry<String, String> entry : selectedPhoneMap.entrySet()) {
                        ContentValues values = new ContentValues();
                        values.put(CallBlockerDb.COLS_BLOCKED_NUMBER.PHONE_NUMBER, entry.getKey());
                        values.put(CallBlockerDb.COLS_BLOCKED_NUMBER.DISPLAY_NAME, entry.getValue());
                        Uri newUri = getActivity().getContentResolver().insert(CallBlockerProvider.BLOCKED_NUMBER_URI, values);
                        if (Long.parseLong(newUri.getLastPathSegment()) > 0) {
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

                    if (numOfAdded > 0)
                        Toast.makeText(getActivity(), numOfAdded + " " + (numOfAdded > 1 ? "phone numbers" : "phone number") + " added", Toast.LENGTH_SHORT).show();
                    if (numOfNotAdded > 0)
                        Toast.makeText(getActivity(), numOfNotAdded + " " + (numOfNotAdded > 1 ? "phone numbers" : "phone number") + " not added, duplicate?", Toast.LENGTH_SHORT).show();

                    getFragmentManager().popBackStack(CONS.FRAGMENT_VIEW_PAGER_CONTAINER, FragmentManager.POP_BACK_STACK_INCLUSIVE);
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
    public void onResume() {
        super.onResume();

        //if(isFabRotated) {
        enterFab.startAnimation(rotateForwardAppear);

        getListView().setOnItemClickListener(this);
        getListView().setOnItemLongClickListener(this);

        MainActivity mainActivity = (MainActivity)getActivity();
        ActionBar mainActionBar = mainActivity.getSupportActionBar();
        if(mainActionBar != null) {
            mainActionBar.setTitle(R.string.title_add_from_sms_log);
            mainActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    Context parentContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        parentContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onPause() {
        super.onPause();

        enterFab.startAnimation(rotateBackwardDisappear);

        getListView().setOnItemClickListener(null);
        getListView().setOnItemLongClickListener(null);
    }

    final String[] FROM_COLUMNS = {
            Telephony.TextBasedSmsColumns.ADDRESS
            , Telephony.TextBasedSmsColumns.TYPE
            , Telephony.TextBasedSmsColumns.SUBJECT
            , Telephony.TextBasedSmsColumns.BODY
            , Telephony.TextBasedSmsColumns.DATE
            , Telephony.TextBasedSmsColumns.DATE_SENT
            //, Telephony.TextBasedSmsColumns.PROTOCOL
            //, Telephony.TextBasedSmsColumns.STATUS
            //, Telephony.TextBasedSmsColumns.SEEN
            //, Telephony.TextBasedSmsColumns.READ
    };
    final int[] TO_IDS = new int[]{
            R.id.add_from_sms_log_row_holder
    };

    private void setupLoader(final View fragmentView) {
        //cursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.add_from_sms_log_row_layout, null, FROM_COLUMNS, TO_IDS, 0);
        cursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.add_from_sms_log_row_layout, null, FROM_COLUMNS, TO_IDS, 0);
        cursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            ContentResolver contentResolver = getActivity().getContentResolver();
            //String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID};
            String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI};
            //String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.PHOTO_URI};

            @Override
            public boolean setViewValue(final View view, final Cursor cursor, int idIdx) {
                String phoneNumberRead = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.TextBasedSmsColumns.ADDRESS));
                String phoneNumberStripped = phoneNumberRead.replaceAll("[^\\d]", "");
                LinearLayout rowView = (LinearLayout) view.getParent();

                if (callBlockerDbHelper.isBlockedNumber(phoneNumberStripped)) {
                    //Already in blocked numbers
                    rowView.setBackgroundColor(Color.parseColor(CONS.ROW_COLOR_ALREADY_BLOCKED));
                } else {
                    if (selectedPhoneMap.containsKey(phoneNumberStripped)) {
                        rowView.setBackgroundColor(Color.parseColor(CONS.ROW_COLOR_SELECTED));
                    } else {
                        rowView.setBackgroundColor(Color.parseColor(CONS.ROW_COLOR_UNSELECTED));
                    }
                }

                //Log.d(TAG, ">>>>> seen: " + cursor.getString(cursor.getColumnIndexOrThrow(Telephony.TextBasedSmsColumns.SEEN)));
                //Log.d(TAG, ">>>>> read: " + cursor.getString(cursor.getColumnIndexOrThrow(Telephony.TextBasedSmsColumns.READ)));

                ImageView photoView = (ImageView) view.findViewById(R.id.add_from_sms_log_row_photo);
                TextView numberView = (TextView) view.findViewById(R.id.add_from_sms_log_row_number);
                TextView nameView = (TextView) view.findViewById(R.id.add_from_sms_log_row_name);


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

                numberView.setText(Utils.formatPhoneNumber(phoneNumberStripped));


                String type = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.TextBasedSmsColumns.TYPE));
                String dateReceivedStr = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.TextBasedSmsColumns.DATE));
                //DATE_SENT is 0 always, but?
                //String dateSentStr = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.TextBasedSmsColumns.DATE_SENT));
                String dateSentStr = dateReceivedStr;

                SimpleDateFormat dateFormat = new SimpleDateFormat();

                ImageView typeIV = (ImageView) view.findViewById(R.id.add_from_sms_log_row_type);
                TextView dateView = (TextView) view.findViewById(R.id.add_from_sms_log_row_date);

                switch (Integer.parseInt(type)) {
                    /**
                     * Followings are filtered in onCreateLoader
                    case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_DRAFT:
                        //typeIV.setImageResource(R.drawable.ic_call_made_black_18dp);
                        dateView.setText("");
                        break;
                    case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX:
                        //typeIV.setImageResource(R.drawable.ic_call_made_black_18dp);
                        dateView.setText("");
                        break;
                    case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_QUEUED:
                        //typeIV.setImageResource(R.drawable.ic_call_made_black_18dp);
                        dateView.setText("");
                        break;
                    */
                    case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_FAILED:
                        //typeIV.setImageResource(R.drawable.ic_call_made_black_18dp);
                        typeIV.setImageResource(R.drawable.ic_call_missed_outgoing_black_18dp);
                        dateView.setText("");
                        break;
                    case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT:
                        typeIV.setImageResource(R.drawable.ic_call_made_black_18dp);
                        long dateSentLong = Long.valueOf(dateSentStr);
                        dateView.setText(dateFormat.format(dateSentLong));
                        break;
                    case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX:
                        typeIV.setImageResource(R.drawable.ic_call_received_black_18dp);
                        long dateReceivedLong = Long.valueOf(dateReceivedStr);
                        dateView.setText(dateFormat.format(dateReceivedLong));
                        break;
                    default:
                        Log.e(TAG, "Unsupported type: " + type);
                }

                String subject = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.TextBasedSmsColumns.BODY));

                TextView durationView = (TextView) view.findViewById(R.id.add_from_sms_log_row_subject);
                durationView.setText(subject);

                //TODO message body in the drawer of this row

                return true;
            }
        });

        setListAdapter(cursorAdapter);
        getLoaderManager().initLoader(SMS_LOG_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        //Log.d(TAG, ">>> onCreateLoader: laoderId: " + loaderId);

        CursorLoader cursorLoader = null;
        switch (loaderId) {
            case SMS_LOG_LOADER:
                //default sort order: date desc
                //Log.d(TAG, ">>>>> content uri: " + Telephony.Sms.CONTENT_URI);
                String selection = Telephony.Sms.TYPE + " IN (?, ?, ?)";
                String[] selectionArgs = new String[] {
                        Integer.toString(Telephony.Sms.MESSAGE_TYPE_INBOX),
                        Integer.toString(Telephony.Sms.MESSAGE_TYPE_FAILED),
                        Integer.toString(Telephony.Sms.MESSAGE_TYPE_SENT)
                };
                cursorLoader = new CursorLoader(getActivity(), Telephony.Sms.CONTENT_URI, null, selection, selectionArgs, Telephony.Sms.DEFAULT_SORT_ORDER);
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
        TextView numberView = (TextView) view.findViewById(R.id.add_from_sms_log_row_number);
        TextView nameView = (TextView) view.findViewById(R.id.add_from_sms_log_row_name);
        //String displayName = infoView.getText().toString();
        //TextView detailView = (TextView) view.findViewById(R.id.add_from_contacts_row_contact_detail);
        //String detail = detailView.getText().toString();
        String phoneNumberFormatted = numberView.getText().toString();
        String phoneNumber = phoneNumberFormatted.replaceAll("[^\\d]", "");
        String displayName = nameView.getText().toString();

        Log.d(TAG, ">>>>> phoneNumber: " + phoneNumber);

        if (callBlockerDbHelper.isBlockedNumber(phoneNumber)) {
            //Toast.makeText(getActivity(), phoneNumber + " already in the block list", Toast.LENGTH_SHORT).show();
            Snackbar.make(getView(), phoneNumber + " already in the block list", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (phoneNumber.trim().equals("")) {
            Snackbar.make(getView(), "Phone number is unknown", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (selectedPhoneMap.containsKey(phoneNumber)) {
            if (rowId != selectedPhoneRowIdMap.get(phoneNumber)) {
                //Toast.makeText(getActivity(), phoneNumber + " already in the bucket", Toast.LENGTH_SHORT).show();
                Snackbar.make(getView(), phoneNumber + " already in the bucket", Snackbar.LENGTH_SHORT).show();
                return;
            } else {
                selectedPhoneMap.remove(phoneNumber);
                selectedPhoneRowIdMap.remove(phoneNumber);
                Log.d(TAG, ">>>>> removed");
                view.setBackgroundColor(Color.parseColor(CONS.ROW_COLOR_UNSELECTED));
                //Toast.makeText(getActivity(), phoneNumber + " removed from the bucket", Toast.LENGTH_SHORT).show();
                Snackbar.make(getView(), phoneNumber + " removed from the bucket", Snackbar.LENGTH_SHORT).show();
            }
        } else {
            selectedPhoneMap.put(phoneNumber, displayName);
            selectedPhoneRowIdMap.put(phoneNumber, rowId);
            Log.d(TAG, ">>>>> added");
            view.setBackgroundColor(Color.parseColor(CONS.ROW_COLOR_SELECTED));
            //Toast.makeText(getActivity(), phoneNumber + " added to the bucket", Toast.LENGTH_SHORT).show();
            Snackbar.make(getView(), phoneNumber + " added to the bucket", Snackbar.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long rowId) {

        Log.d(TAG, ">>>>> long click fired: position=" + position + ", rowId=" + rowId);
        return true;
    }
}
