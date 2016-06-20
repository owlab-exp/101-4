package com.owlab.callblocker.fragment;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
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
import com.owlab.callblocker.content.CallBlockerContentProvider;
import com.owlab.callblocker.content.CallBlockerDbHelper;
import com.owlab.callblocker.content.CallBlockerTbl;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class AddFromContactsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {
    private static final String TAG = AddFromContactsFragment.class.getSimpleName();

    private SimpleCursorAdapter cursorAdapter;
    private static final int CONTACTS_LOADER = 0;
    //private boolean isFabRotated = false;

    FloatingActionButton enterFab;
    Animation rotateForwardAppear;
    Animation rotateBackwardDisappear;

    CallBlockerDbHelper callBlockerDbHelper; //= new CallBlockerDbHelper(getActivity());
    Map<String, String> selectedPhoneMap = new HashMap<>();

    public AddFromContactsFragment() {
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
        View view = inflater.inflate(R.layout.add_from_contacts_layout, container, false);


        enterFab = (FloatingActionButton) view.findViewById(R.id.fab_done);
        enterFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedPhoneMap.size() > 0) {
                    int numOfAdded = 0;
                    int numOfNotAdded = 0;
                    for(Map.Entry<String, String> entry : selectedPhoneMap.entrySet()) {
                        ContentValues values = new ContentValues();
                        values.put(CallBlockerTbl.Schema.COLUMN_NAME_PHONE_NUMBER, entry.getKey());
                        values.put(CallBlockerTbl.Schema.COLUMN_NAME_DISPLAY_NAME, entry.getValue());
                        Uri newUri = getActivity().getContentResolver().insert(CallBlockerContentProvider.CONTENT_URI, values);
                        if(Long.parseLong(newUri.getLastPathSegment()) > 0)
                            numOfAdded++;
                        else {
                            numOfNotAdded++;
                        }
                    }

                    if(numOfAdded > 0)
                        Toast.makeText(getActivity(), numOfAdded + " " + (numOfAdded > 1 ? "phone numbers":"phone number") + " added", Toast.LENGTH_SHORT).show();
                    if(numOfNotAdded > 0)
                        Toast.makeText(getActivity(), numOfNotAdded + " " + (numOfNotAdded > 1 ? "phone numbers":"phone number") + " not added, duplicate?", Toast.LENGTH_SHORT).show();

                    selectedPhoneMap.clear();
                    //Fragment fragment = getFragmentManager().findFragmentByTag(CONS.FRAGMENT_PHONE_LIST);
                    //getActivity().getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment, CONS.FRAGMENT_PHONE_LIST).commit();
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.popBackStack(CONS.FRAGMENT_PHONE_LIST, FragmentManager.POP_BACK_STACK_INCLUSIVE);
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

    Context parentContext;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        parentContext = context;

        if(parentContext instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity)parentContext;
            //mainActivity.changeActionBarContent("Settings");
            android.support.v7.app.ActionBar mainActionBar =  mainActivity.getSupportActionBar();
            if(mainActionBar != null) {
                mainActionBar.setTitle("Contacts");
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(parentContext != null && parentContext instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) parentContext;
            android.support.v7.app.ActionBar mainActionBar = mainActivity.getSupportActionBar();
            if (mainActionBar != null) {
                mainActionBar.setTitle(R.string.app_name);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        enterFab.startAnimation(rotateBackwardDisappear);
    }

    //Provider FROM_COLUMNS
    @SuppressLint("InlineApi")
    private static final String[] FROM_COLUMNS = {
            //ContactsContract.Contacts.PHOTO_THUMBNAIL_URI
            //ContactsContract.Data.CONTACT_ID
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
            //, Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? ContactsContract.Contacts.DISPLAY_NAME_PRIMARY : ContactsContract.Contacts.DISPLAY_NAME
            , ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            , ContactsContract.CommonDataKinds.Phone.NUMBER
            , ContactsContract.CommonDataKinds.Phone.TYPE
    };

    //List row items for the provider FROM_COLUMNS
    private static final int[] TO_IDS = {
            R.id.add_from_contacts_row_contact_icon
            , R.id.add_from_contacts_row_contact_info
            , R.id.add_from_contacts_row_contact_detail
            , R.id.add_from_contacts_row_contact_detail
    };

    private void setupLoader(final View fragmentView) {

        cursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.add_from_contacts_row_layout, null, FROM_COLUMNS, TO_IDS, 0);
        cursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(final View view, final Cursor cursor, int columnIndex) {

                boolean hideListRow = false;
                String phoneNumberR = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));

                LinearLayout rowView = (LinearLayout) view.getParent();
                if(callBlockerDbHelper.hasPhoneNumber(phoneNumberR)) {
                //if(blockedPhoneCursor.getCount() > 0) {
                    //Log.d(TAG, ">>> found phone number: " + phoneNumberR);
                    rowView.setBackgroundColor(Color.parseColor(CONS.ROW_COLOR_ALREADY_BLOCKED));
                    //rowView.setOnClickListener(null);
                    //rowView.getLayoutParams().height = 0;
                    hideListRow = true;
                } else {
                    //This is needed because the layout is reused for other rows
                    if(selectedPhoneMap.containsKey(phoneNumberR.replaceAll("[^\\d]", ""))) {
                        rowView.setBackgroundColor(Color.parseColor(CONS.ROW_COLOR_SELECTED));

                    } else {
                        rowView.setBackgroundColor(Color.parseColor(CONS.ROW_COLOR_UNSELECTED));
                    }
                }
                //blockedPhoneCursor.close();

                //if(columnIndex == cursor.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI)) {
                //if(columnIndex == cursor.getColumnIndexOrThrow(ContactsContract.Data.CONTACT_ID)) {
                if(columnIndex == cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)) {
                    ImageView photoView = (ImageView) view;
                    if(hideListRow) {
                        ////Image height should be suppressed
                        //photoView.getLayoutParams().height = 0;
                    }
                    //If addTextChangedListener needed, make it clear that this call happens only once per the textview
                    //phoneNumberTextView.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
                    //callerInfoTextView.setText(Utils.formatPhoneNumber(cursor.getString(numberColumnIndex)));
                    //phoneNumberTextView.setText(cursor.getString(phoneNumberColumnIndex));
                    String photoThumbnailUri = cursor.getString(columnIndex);
                    //Log.d(TAG, ">>>>> PHOTO_THUMBNAIL_URI: " + photoThumbnailUri);
                    if(photoThumbnailUri != null) {
                        photoView.setImageURI(Uri.parse(photoThumbnailUri));
                    } else {
                        photoView.setImageResource(R.drawable.ic_contact_28);
                    }
                    return true;
                }

                if(columnIndex == cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)) {
                    TextView numberView = (TextView) view.findViewById(R.id.add_from_contacts_row_contact_number);
                    String phoneNumber = cursor.getString(columnIndex);
                    //Log.d(TAG, ">>>>> phone number in contact: " + phoneNumber);
                    numberView.setText(Utils.formatPhoneNumber(phoneNumber));
                    return true;
                }

                if(columnIndex == cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE)) {
                    TextView typeView = (TextView) view.findViewById(R.id.add_from_contacts_row_contact_type);
                    int phoneType = cursor.getInt(columnIndex);
                    typeView.setText(ContactsContract.CommonDataKinds.Phone.getTypeLabel(getResources(), phoneType, ""));
                    return true;
                }

                return false;
            }
        });

        setListAdapter(cursorAdapter);
        getLoaderManager().initLoader(CONTACTS_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        Log.d(TAG, ">>> onCreateLoader: laoderId: " + loaderId);

        CursorLoader cursorLoader = null;
        switch(loaderId) {
            case CONTACTS_LOADER:

                cursorLoader = new CursorLoader(getActivity(), ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.Contacts.HAS_PHONE_NUMBER + " > 0", null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
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
        Log.d(TAG, ">>>>> a list item clicked: position = " + position + ", rowId = " + rowId);
        TextView infoView = (TextView) view.findViewById(R.id.add_from_contacts_row_contact_info);
        String displayName = infoView.getText().toString();
        TextView numberView = (TextView) view.findViewById(R.id.add_from_contacts_row_contact_number);
        String phoneNumberFormatted = numberView.getText().toString();
        String phoneNumber = phoneNumberFormatted.split("\n")[0].replaceAll("[^\\d]", "");
        Log.d(TAG, ">>>>> phoneNumber: " + phoneNumber + ", displayName: " + displayName);

        if(callBlockerDbHelper.hasPhoneNumber(phoneNumber)) {
            Toast.makeText(getActivity(), phoneNumber + " already in the block list", Toast.LENGTH_SHORT).show();
            return;
        }

        if(selectedPhoneMap.containsKey(phoneNumber)) {
            selectedPhoneMap.remove(phoneNumber);
            Log.d(TAG, ">>>>> removed");
            view.setBackgroundColor(Color.parseColor(CONS.ROW_COLOR_UNSELECTED));
            Toast.makeText(getActivity(), phoneNumber + " removed from the bucket", Toast.LENGTH_SHORT).show();
        } else {
            selectedPhoneMap.put(phoneNumber, displayName);
            Log.d(TAG, ">>>>> added");
            view.setBackgroundColor(Color.parseColor(CONS.ROW_COLOR_SELECTED));
            Toast.makeText(getActivity(), phoneNumber + " added to the bucket", Toast.LENGTH_SHORT).show();
        }

        ////RotateAnimation rotate = new RotateAnimation(0.0f, -10.0f * 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        //RotateAnimation rotate = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        //rotate.setDuration(300);
        //ImageView contactIcon = (ImageView)view.findViewById(R.id.add_from_contacts_row_contact_icon);
        //contactIcon.startAnimation(rotate);

    }
}
