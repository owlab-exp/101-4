package com.owlab.callblocker.fragment;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.owlab.callblocker.CONS;
import com.owlab.callblocker.MainActivity;
import com.owlab.callblocker.R;
import com.owlab.callblocker.util.Utils;
import com.owlab.callblocker.contentprovider.CallBlockerDb;
import com.owlab.callblocker.contentprovider.CallBlockerDbHelper;
import com.owlab.callblocker.contentprovider.CallBlockerProvider;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

/**
 */
public class AddFromSmsLogFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemLongClickListener, ExpandableListView.OnGroupClickListener, ExpandableListView.OnChildClickListener {
    public static final String TAG = AddFromSmsLogFragment.class.getSimpleName();

    Context parentContext;

    private SmsLogSimpleCursorTreeAdapter cursorTreeAdapter;
    private ExpandableListView expandableListView;
    //the number should not be a positive, not to be confused when handle child
    private static final int SMS_LOG_LOADER = -11;
    //private boolean isFabRotated = false;

    FloatingActionButton enterFab;
    Animation rotateForwardAppear;
    Animation rotateBackwardDisappear;

    CallBlockerDbHelper callBlockerDbHelper;
    HashMap<String, String> selectedPhoneMap = new HashMap<>();
    HashMap<String, Long> selectedPhoneRowIdMap = new HashMap<>();

    HashSet<Long> recoveredExpandedGroupIdSet;

    private static final String KEY_SELECTED_NUMBER_MAP = "selectedPhoneMap";
    private static final String KEY_SELECTED_ROW_ID_MAP = "selectedPhoneRowIdMap";
    private static final String KEY_EXPANDED_GROUP_ID_SET = "expandedGroupIdSet";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rotateForwardAppear = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_forward_appear);
        rotateBackwardDisappear = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_backward_disappear);
        callBlockerDbHelper = new CallBlockerDbHelper(getActivity());


        if(savedInstanceState != null) {
            Object saved = savedInstanceState.getSerializable(KEY_SELECTED_NUMBER_MAP);
            if(saved != null) {
                selectedPhoneMap = (HashMap<String, String>) saved;
            }

            saved = savedInstanceState.getSerializable(KEY_SELECTED_ROW_ID_MAP);
            if(saved != null) {
                selectedPhoneRowIdMap = (HashMap<String, Long>) saved;
            }

            saved = savedInstanceState.getSerializable(KEY_EXPANDED_GROUP_ID_SET);
            if(saved != null) {
                recoveredExpandedGroupIdSet = (HashSet<Long>) saved;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(KEY_SELECTED_NUMBER_MAP, selectedPhoneMap);
        outState.putSerializable(KEY_SELECTED_ROW_ID_MAP, selectedPhoneRowIdMap);

        BaseExpandableListAdapter bela = (BaseExpandableListAdapter)  expandableListView.getExpandableListAdapter();
        if(bela != null) {
            int groupSize = bela.getGroupCount();

            HashSet<Long> expandedGroupIdSet = new HashSet<>();
            for(int i = 0; i < groupSize; i++) {
                if(expandableListView.isGroupExpanded(i)) {
                    expandedGroupIdSet.add(bela.getGroupId(i));
                }
            }
            Log.d(TAG, ">>>>> expandedGroupIdSet size: " + expandedGroupIdSet.size());
            outState.putSerializable(KEY_EXPANDED_GROUP_ID_SET, expandedGroupIdSet);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, ">>>>> onCreateView called with: " + Objects.toString(savedInstanceState));
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
                        if (newUri != null && Long.parseLong(newUri.getLastPathSegment()) > 0) {
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
    public void onResume() {
        super.onResume();

        //if(isFabRotated) {
        enterFab.startAnimation(rotateForwardAppear);

        expandableListView.setOnGroupClickListener(this);
        expandableListView.setOnItemLongClickListener(this);
        expandableListView.setOnChildClickListener(this);

        MainActivity mainActivity = (MainActivity) getActivity();
        ActionBar mainActionBar = mainActivity.getSupportActionBar();
        if (mainActionBar != null) {
            mainActionBar.setTitle(R.string.title_add_from_sms_log);
            mainActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        parentContext = context;
    }

    @Override
    public void onDetach() {
        parentContext = null;
        super.onDetach();
    }

    @Override
    public void onPause() {
        super.onPause();

        enterFab.startAnimation(rotateBackwardDisappear);

        expandableListView.setOnGroupClickListener(null);
        expandableListView.setOnItemLongClickListener(null);
        expandableListView.setOnChildClickListener(null);
    }

    final String[] CONTACTS_PROJECTION = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI};

    final String[] GROUP_PROJECTION = {
            Telephony.Sms._ID
            , Telephony.TextBasedSmsColumns.ADDRESS
            , Telephony.TextBasedSmsColumns.TYPE
            , Telephony.TextBasedSmsColumns.BODY
            , Telephony.TextBasedSmsColumns.DATE
            , Telephony.TextBasedSmsColumns.DATE_SENT
    };

    final String[] CHILD_PROJECTION = {
            Telephony.Sms._ID
            , Telephony.TextBasedSmsColumns.SUBJECT
            , Telephony.TextBasedSmsColumns.BODY
    };
    final String[] FROM_GROUP_COLUMNS = {
            Telephony.Sms._ID
            , Telephony.TextBasedSmsColumns.ADDRESS
            , Telephony.TextBasedSmsColumns.TYPE
            , Telephony.TextBasedSmsColumns.BODY
            , Telephony.TextBasedSmsColumns.DATE
            , Telephony.TextBasedSmsColumns.DATE_SENT
    };
    final int[] TO_GROUP_IDS = new int[]{
            R.id.add_from_sms_log_row_holder
    };
    final String[] FROM_CHILD_COLUMNS = {
            Telephony.Sms._ID
            , Telephony.TextBasedSmsColumns.SUBJECT
            , Telephony.TextBasedSmsColumns.BODY
    };
    final int[] TO_CHILD_IDS = new int[]{
            R.id.add_from_sms_log_row_child_holder
    };

    private void setupLoader(final View fragmentView) {
        //
        expandableListView = (ExpandableListView) fragmentView.findViewById(R.id.expandable_list_view);
        //TODO what is this?
        //expandableListView.setSaveEnabled(true);
        cursorTreeAdapter = new SmsLogSimpleCursorTreeAdapter(
                getActivity(),
                R.layout.add_from_sms_log_row_layout,
                R.layout.add_from_sms_log_row_layout,
                FROM_GROUP_COLUMNS,
                TO_GROUP_IDS,
                R.layout.add_from_sms_log_row_child_layout,
                FROM_CHILD_COLUMNS,
                TO_CHILD_IDS
        );

        cursorTreeAdapter.setViewBinder(new SimpleCursorTreeAdapter.ViewBinder() {

            ContentResolver contentResolver = getActivity().getContentResolver();

            @Override
            public boolean setViewValue(View view, Cursor cursor, int idIdx) {
                //Log.d(TAG, ">>>>> view: " + view.toString());

                if(view.getId() == R.id.add_from_sms_log_row_holder) {
                    String phoneNumberRead = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.TextBasedSmsColumns.ADDRESS));
                    String phoneNumberStripped = phoneNumberRead.replaceAll("[^\\d]", "");
                    LinearLayout rowView = (LinearLayout) view.getParent();

                    if (callBlockerDbHelper.isBlockedNumber(phoneNumberStripped)) {
                        //Already in blocked numbers
                        rowView.setBackgroundColor(Color.parseColor(CONS.ROW_COLOR_ALREADY_BLOCKED));
                    } else {
                        //if (selectedPhoneMap.containsKey(phoneNumberStripped)) {
                        if (selectedPhoneRowIdMap.containsValue(cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms._ID)))) {
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
                        Cursor contactsCursor = contentResolver.query(uri, CONTACTS_PROJECTION, null, null, null);
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

                } else if(view.getId() == R.id.add_from_sms_log_row_child_holder) {
                    String subject = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.SUBJECT));
                    String body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY));

                    //TextView subjectTV = (TextView) view.findViewById(R.id.add_from_sms_log_row_child_subject);
                    TextView bodyTV = (TextView) view.findViewById(R.id.add_from_sms_log_row_child_body);

                    if(subject == null || subject.trim().equals("")) {
                        //subjectTV.setVisibility(View.INVISIBLE);
                        bodyTV.setText(body);
                    } else {
                        //subjectTV.setText(subject);
                        bodyTV.setText(subject + "\n");
                        bodyTV.setText(body);
                    }
                }
                //TODO message body in the drawer of this row

                return true;
            }
        });

        expandableListView.setAdapter(cursorTreeAdapter);
        Loader<Cursor> cursorLoader = getLoaderManager().getLoader(SMS_LOG_LOADER);
        if (cursorLoader != null && !cursorLoader.isReset()) {
            getLoaderManager().restartLoader(SMS_LOG_LOADER, null, this);
        } else {
            getLoaderManager().initLoader(SMS_LOG_LOADER, null, this);
        }

        // FROM: http://stackoverflow.com/questions/23002878/expandablelistview-using-simplecursortreeadapter-scrolls-to-top-on-update
        getActivity().getContentResolver().registerContentObserver(Telephony.Sms.CONTENT_URI, false, new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                Log.d(TAG, ">>>>> content observer: changed by self? " + selfChange);
                //super.onChange(selfChange);
            }
        });


    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        //Log.d(TAG, ">>> onCreateLoader: laoderId: " + loaderId);

        CursorLoader cursorLoader = null;
        switch (loaderId) {
            case SMS_LOG_LOADER:
                //default sort order: date desc
                //Log.d(TAG, ">>>>> content uri: " + Telephony.Sms.CONTENT_URI);
                String mainSelection = Telephony.Sms.TYPE + " IN (?, ?, ?)";
                String[] mainSelectionArgs = new String[]{
                        Integer.toString(Telephony.Sms.MESSAGE_TYPE_INBOX),
                        Integer.toString(Telephony.Sms.MESSAGE_TYPE_FAILED),
                        Integer.toString(Telephony.Sms.MESSAGE_TYPE_SENT)
                };
                //TODO optimise projection
                cursorLoader = new CursorLoader(getActivity(), Telephony.Sms.CONTENT_URI, null, mainSelection, mainSelectionArgs, Telephony.Sms.DEFAULT_SORT_ORDER);
                break;

            default:
                //loaderId is not SMS_LOG_LOADER
                // loaderId is the key for child query
                String childSelection = Telephony.Sms._ID + " = ?";
                String[] childSelectionArgs = new String[] {
                        Integer.toString(loaderId)
                };
                cursorLoader = new CursorLoader(getActivity(), Telephony.Sms.CONTENT_URI, null, childSelection, childSelectionArgs, Telephony.Sms.DEFAULT_SORT_ORDER);
                Log.d(TAG, ">>>>> child loader created: loaderId: " + loaderId);
        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        int loaderId = loader.getId();
        Log.d(TAG, ">>>>> onLoaderFinished: loaderId: " + loaderId);
        switch (loaderId) {
            case SMS_LOG_LOADER:
                cursorTreeAdapter.setGroupCursor(cursor);

                //Now expand groups if needed, that is after onSaveInstanceState.
                //Log.d(TAG, ">>>>> groupSize: " + cursorTreeAdapter.getGroupCount());
                if(recoveredExpandedGroupIdSet != null) {
                    int groupSize = cursorTreeAdapter.getGroupCount();
                    //Log.d(TAG, ">>>>> groupSize: " + groupSize);
                    for (int i = 0; i < groupSize; i++) {
                        //Log.d(TAG, "groupPosition: " + i);
                        long groupId = cursorTreeAdapter.getGroupId(i);
                        if (recoveredExpandedGroupIdSet.contains(groupId)) {
                            //Log.d(TAG, "expanding");
                            expandableListView.expandGroup(i);
                        }
                    }
                    //reinitialize
                    recoveredExpandedGroupIdSet = null;
                }
                break;
            default:
                //child loader
                if(!cursor.isClosed()) {
                    SparseArray<Integer> groupIdPositionMap = cursorTreeAdapter.getGroupIdPositionMap();
                    //Map<Integer, Integer> groupIdPositionMap = cursorTreeAdapter.getGroupIdPositionMap();
                    int groupPosition = groupIdPositionMap.get(loaderId);
                    cursorTreeAdapter.setChildrenCursor(groupPosition, cursor);
                }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        int loaderId = loader.getId();

        switch (loaderId) {
            case SMS_LOG_LOADER:
                cursorTreeAdapter.setGroupCursor(null);
                break;
            default:
                //TODO Is this sufficient?
                //Log.d(TAG, ">>>>> onLoaderReset: loaderId: " + loaderId);
                //try {
                //    //if(cursorTreeAdapter != null && cursorTreeAdapter.getC) {
                //    SparseArray<Integer> groupIdPositionMap = cursorTreeAdapter.getGroupIdPositionMap();
                //    int groupPosition = groupIdPositionMap.get(loaderId);

                //    Cursor childCursor = cursorTreeAdapter.getChild(groupPosition, 0);
                //    Log.d(TAG, "child cursor: " + (childCursor != null? childCursor.toString():null));


                //    cursorTreeAdapter.setChildrenCursor(groupPosition, null);


                //    //}
                //} catch(NullPointerException npe) {
                //    Log.w(TAG, ">>>>> NPE occurred");
                //}
        }
    }

    @Override
    public boolean onGroupClick(ExpandableListView expandableListView, View view, int position, long rowId) {
        Log.d(TAG, ">>>>> group click fired: position=" + position + ", rowId=" + rowId);
        doOnRowClick(view, position, rowId);
        return true;
    }

    private void doOnRowClick(View view, int position, long rowId) {
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
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int flatPosition, long rowId) {
        Log.d(TAG, ">>>>> long click fired: flatPosition =" + flatPosition + ", rowId=" + rowId);
        //Log.d(TAG, ">>>>> adapterView: " + adapterView.toString());
        //Log.d(TAG, ">>>>> view: " + view.toString());

        ExpandableListView localExpandableListView = (ExpandableListView) adapterView;
        long packedGroupPosition = localExpandableListView.getExpandableListPosition(flatPosition);
        Log.d(TAG, ">>>>> packed position: " + packedGroupPosition);
        int groupPosition = ExpandableListView.getPackedPositionGroup(localExpandableListView.getExpandableListPosition(flatPosition));
        Log.d(TAG, ">>>>> group position: " + groupPosition);
        if(localExpandableListView.isGroupExpanded(groupPosition)) {
            localExpandableListView.collapseGroup(groupPosition);
            //expandedGroupIdSet.remove(rowId);
        } else {
            localExpandableListView.expandGroup(groupPosition, true);
            //expandedGroupIdSet.add(rowId);
        }

        return true;
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
        parent.collapseGroup(groupPosition);
        return true;
    }

    /**
     * A descendant of SimpleCursorTreeAdapter
     */
    public class SmsLogSimpleCursorTreeAdapter extends SimpleCursorTreeAdapter {
        private final String TAG = SmsLogSimpleCursorTreeAdapter.class.getSimpleName();

        final SparseArray<Integer>  groupIdPositionMap;
        //Map<Integer, Integer> groupIdPositionMap;
        public SmsLogSimpleCursorTreeAdapter(Context context, int collapsedGroupLayout, int expandedGroupLayout, String[] groupFrom, int[] groupTo, int childLayout, String[] childFrom, int[] childTo) {
            //Cursor parameter is null not to query on main thread
            super(context, null, collapsedGroupLayout, expandedGroupLayout, groupFrom, groupTo, childLayout, childFrom, childTo);
            groupIdPositionMap = new SparseArray<>();
            //groupIdPositionMap = new HashMap<>();
        }

        @Override
        protected Cursor getChildrenCursor(Cursor groupCursor) {
            Log.d(TAG, ">>>>> getting child cursor...");
            int groupPosition = groupCursor.getPosition();
            int smsId = groupCursor.getInt(groupCursor.getColumnIndexOrThrow(Telephony.Sms._ID));

            groupIdPositionMap.put(smsId, groupPosition);

            if(parentContext != null) {
                Loader loader = AddFromSmsLogFragment.this.getLoaderManager().getLoader(smsId);
                Loader renewedLoader = null;
                if (loader != null && !loader.isReset()) {
                    renewedLoader = getLoaderManager().restartLoader(smsId, null, AddFromSmsLogFragment.this);
                } else {
                    renewedLoader = getLoaderManager().initLoader(smsId, null, AddFromSmsLogFragment.this);
                }
                CursorLoader cursorLoader = (CursorLoader) renewedLoader;
            } else {
                Log.d(TAG, ">>>>> Not attached in the parent activity");
            }


            return null;
        }

        public SparseArray<Integer> getGroupIdPositionMap() {
        //public Map<Integer, Integer> getGroupIdPositionMap() {
            return groupIdPositionMap;
        }
    }
}
