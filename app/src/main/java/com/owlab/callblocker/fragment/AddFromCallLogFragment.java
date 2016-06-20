package com.owlab.callblocker.fragment;

import android.app.FragmentManager;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
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
import com.owlab.callblocker.R;
import com.owlab.callblocker.content.CallBlockerContentProvider;
import com.owlab.callblocker.content.CallBlockerDbHelper;
import com.owlab.callblocker.content.CallBlockerTbl;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class AddFromCallLogFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {
//public class AddFromCallLogFragment extends ListFragment {
    private static final String TAG = AddFromCallLogFragment.class.getSimpleName();

    private SimpleCursorAdapter cursorAdapter;
    private static final int CALL_LOG_LOADER = 0;
    //private boolean isFabRotated = false;

    FloatingActionButton enterFab;
    Animation rotateForwardAppear;
    Animation rotateBackwardDisappear;

    CallBlockerDbHelper callBlockerDbHelper;
    Map<String, String> selectedPhoneMap = new HashMap<>();

    public AddFromCallLogFragment() {
        Log.d(TAG, ">>>>> instantiated");
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
        View view = inflater.inflate(R.layout.add_from_call_log_layout, container, false);

        enterFab = (FloatingActionButton) view.findViewById(R.id.fab_done);
        enterFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedPhoneMap.size() > 0) {
                    for(Map.Entry<String, String> entry : selectedPhoneMap.entrySet()) {
                        ContentValues values = new ContentValues();
                        values.put(CallBlockerTbl.Schema.COLUMN_NAME_PHONE_NUMBER, entry.getKey());
                        values.put(CallBlockerTbl.Schema.COLUMN_NAME_DESCRIPTION, entry.getValue());
                        Uri newUri = getActivity().getContentResolver().insert(CallBlockerContentProvider.CONTENT_URI, values);
                        if(Long.parseLong(newUri.getLastPathSegment()) > 0) {
                            Toast.makeText(getActivity(), entry.getKey() + " added", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), entry.getKey() + " failed to add, duplicate?", Toast.LENGTH_SHORT).show();
                        }
                    }
                    selectedPhoneMap.clear();
                    getFragmentManager().popBackStack(CONS.FRAGMENT_PHONE_LIST, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                } else {
                    Toast.makeText(getActivity(), "No phone number selected", Toast.LENGTH_SHORT).show();
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
    }

    @Override
    public void onPause() {
        super.onPause();

        enterFab.startAnimation(rotateBackwardDisappear);
    }

    final String[] FROM_COLUMNS = {
            //CallLog.Calls._ID
            CallLog.Calls.NUMBER
            , CallLog.Calls.NUMBER
            , CallLog.Calls.TYPE
            , CallLog.Calls.DATE
            , CallLog.Calls.DURATION
            // , CallLog.Calls.NEW
            // , CallLog.Calls.COUNTRY_ISO
    };
    final int[] TO_IDS = new int[]{
            R.id.add_from_call_log_row_caller_icon
            , R.id.add_from_call_log_row_caller_info
            , R.id.add_from_call_log_row_caller_type
            , R.id.add_from_call_log_row_call_detail
            , R.id.add_from_call_log_row_call_detail
    };

    private void setupLoader(final View fragmentView) {
        cursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.add_from_call_log_row_layout, null, FROM_COLUMNS, TO_IDS, 0);
        cursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            ContentResolver contentResolver = getActivity().getContentResolver();
            //String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID};
            String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI};
            //String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.PHOTO_URI};

            @Override
            public boolean setViewValue(final View view, final Cursor cursor, int columnIndex) {
                boolean hideListRow = false;
                String phoneNumberR = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
                LinearLayout rowView = (LinearLayout) view.getParent();
                if(callBlockerDbHelper.hasPhoneNumber(phoneNumberR)) {
                    rowView.setBackgroundColor(Color.parseColor(CONS.ROW_COLOR_ALREADY_BLOCKED));
                    hideListRow = true;
                } else {
                    if(selectedPhoneMap.containsKey(phoneNumberR.replaceAll("[^\\d]", ""))) {
                        rowView.setBackgroundColor(Color.parseColor(CONS.ROW_COLOR_SELECTED));
                    } else {
                        rowView.setBackgroundColor(Color.parseColor(CONS.ROW_COLOR_UNSELECTED));
                    }
                }

                if(columnIndex == cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)) {
                    String phoneNumber = cursor.getString(columnIndex);
                    if(view instanceof ImageView) {
                        //icon view
                        ImageView photoView = (ImageView) view;
                        if(hideListRow) {
                            //If this row should be shrinked, then
                            //photoView.getLayoutParams().height = 0;
                        }
                        //TODO get photo if exists
                        if(!phoneNumber.trim().equals("")) {
                            //long contactId = -1l;
                            String photoUriStr = null;
                            Log.d(TAG, ">>>>> looking for: " + phoneNumber);
                            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
                            Log.d(TAG, ">>>>> uri: " + uri.toString());
                            Cursor contactsCursor = contentResolver.query(uri, projection, null, null, null);
                            if (contactsCursor != null) {
                                if (contactsCursor.getCount() > 0 && contactsCursor.moveToFirst()) {
                                    //contactId = contactsCursor.getLong(contactsCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
                                    photoUriStr = contactsCursor.getString(contactsCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI));
                                    //photoUriStr = contactsCursor.getString(contactsCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.PHOTO_URI));
                                }
                                contactsCursor.close();
                            }

                            //if(contactId != -1l) {
                            if (photoUriStr != null) {
                                //Bitmap photo = null;
                                //try {
                                //    InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(getActivity().getContentResolver(), ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId));
                                //    if (inputStream != null) {
                                //        photo = BitmapFactory.decodeStream(inputStream);
                                //        inputStream.close();
                                //    }
                                //} catch (IOException e) {
                                //    e.printStackTrace();
                                //}
                                //if (photo != null) {
                                //    photoView.setImageBitmap(photo);
                                //    return true;
                                //}
                                photoView.setImageURI(Uri.parse(photoUriStr));
                                return true;
                            }
                        }

                        photoView.setImageResource(R.drawable.ic_contact_28);
                        return true;

                    } else if(view instanceof TextView) {
                        //info iew
                        TextView infoTV = (TextView)view;
                        infoTV.setText(phoneNumber);
                        //infoTV.setText(Utils.formatPhoneNumber(phoneNumber));
                        return true;
                    }
                }

                if(columnIndex == cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE)) {
                    ImageView typeIV = (ImageView) view;

                    String type = cursor.getString(columnIndex);
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

                if(columnIndex == cursor.getColumnIndexOrThrow(CallLog.Calls.DATE)) {
                    String dateStr = cursor.getString(columnIndex);

                    long dateLong = Long.valueOf(dateStr);
                    SimpleDateFormat dateFormat = new SimpleDateFormat();
                    //TODO if today, then do simpler format

                    TextView detailTextView = (TextView) view;
                    detailTextView.setText(dateFormat.format(dateLong));
                    return true;
                }

                if(columnIndex == cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION)) {
                    TextView detailTextView = (TextView) view;
                    detailTextView.append("\n" + cursor.getString(columnIndex) + " sec");
                    return true;
                }
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

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //Log.d(TAG, ">>>>> a list item clicked: position = " + position + ", rowId = " + rowId);
        TextView infoView = (TextView) view.findViewById(R.id.add_from_call_log_row_caller_info);
        //String displayName = infoView.getText().toString();
        //TextView detailView = (TextView) view.findViewById(R.id.add_from_contacts_row_contact_detail);
        //String detail = detailView.getText().toString();
        String info = infoView.getText().toString();
        String phoneNumber = info.replaceAll("[^\\d]", "");
        String displayName = "";
        Log.d(TAG, ">>>>> phoneNumber: " + phoneNumber);

        if(callBlockerDbHelper.hasPhoneNumber(phoneNumber)) {
            return;
        }

        if(selectedPhoneMap.containsKey(phoneNumber)) {
            selectedPhoneMap.remove(phoneNumber);
            Log.d(TAG, ">>>>> removed");
            view.setBackgroundColor(Color.parseColor(CONS.ROW_COLOR_UNSELECTED));
        } else {
            selectedPhoneMap.put(phoneNumber, displayName);
            Log.d(TAG, ">>>>> added");
            view.setBackgroundColor(Color.parseColor(CONS.ROW_COLOR_SELECTED));
        }
    }
}
