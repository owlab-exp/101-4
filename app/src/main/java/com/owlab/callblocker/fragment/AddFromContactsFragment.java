package com.owlab.callblocker.fragment;

import android.annotation.SuppressLint;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.owlab.callblocker.R;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;

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

    //Provider columns
    @SuppressLint("InlineApi")
    private static final String[] FROM_COLUMNS = {
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI
            , Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? ContactsContract.Contacts.DISPLAY_NAME_PRIMARY : ContactsContract.Contacts.DISPLAY_NAME
            , ContactsContract.CommonDataKinds.Phone.NUMBER
            , ContactsContract.CommonDataKinds.Phone.TYPE
    };

    //List row items for the provider columns
    private static final int[] TO_IDS = {
            R.id.add_from_contacts_row_contact_icon
            , R.id.add_from_contacts_row_contact_info
            , R.id.add_from_contacts_row_contact_detail
            , R.id.add_from_contacts_row_contact_detail
    };

    public AddFromContactsFragment() {
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
        View view = inflater.inflate(R.layout.add_from_contacts_layout, container, false);

        enterFab = (FloatingActionButton) view.findViewById(R.id.fab_done);
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

        setupLoader(view);

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

    private void setupLoader(final View fragmentView) {

        cursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.add_from_contacts_row_layout, null, FROM_COLUMNS, TO_IDS, 0);
        cursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(final View view, final Cursor cursor, int columnIndex) {

                if(columnIndex == cursor.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI)) {
                    ImageView photoView = (ImageView) view;
                    //If addTextChangedListener needed, make it clear that this call happens only once per the textview
                    //phoneNumberTextView.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
                    //callerInfoTextView.setText(Utils.formatPhoneNumber(cursor.getString(numberColumnIndex)));
                    //phoneNumberTextView.setText(cursor.getString(phoneNumberColumnIndex));
                    String photoThumbnailUri = cursor.getString(columnIndex);
                    Log.d(TAG, ">>>>> PHOTO_THUMBNAIL_URI: " + photoThumbnailUri);
                    if(photoThumbnailUri != null) {
                        try {
                            AssetFileDescriptor afd = getActivity().getContentResolver().openAssetFileDescriptor(Uri.parse(photoThumbnailUri), "r");
                            FileDescriptor fd = afd.getFileDescriptor();
                            Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fd, null, null);
                            photoView.setImageBitmap(bitmap);
                            return true;
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            return false;
                        }

                    } else {
                        return true;
                    }
                }

                if(columnIndex == cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)) {
                    TextView detailView = (TextView) view;
                    String phoneNumber = cursor.getString(columnIndex);
                    detailView.setText(phoneNumber);
                    return true;
                }

                if(columnIndex == cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE)) {
                    TextView detailView = (TextView) view;
                    int phoneType = cursor.getInt(columnIndex);
                    detailView.append("\n" + ContactsContract.CommonDataKinds.Phone.getTypeLabel(getResources(), phoneType, ""));
                    return true;
                }

                //if(columnIndex == dateColumnIndex) {
                //    String dateStr = cursor.getString(dateColumnIndex);

                //    long dateLong = Long.valueOf(dateStr);
                //    SimpleDateFormat dateFormat = new SimpleDateFormat();
                //    //TODO if today, then do simpler format

                //    TextView detailTextView = (TextView) view;
                //    detailTextView.append(dateFormat.format(dateLong));
                //    return true;
                //}

                //if(columnIndex == durationColumnIndex) {
                //    TextView detailTextView = (TextView) view;
                //    detailTextView.append("\n" + cursor.getString(durationColumnIndex) + " sec");
                //    return true;
                //}

                return false;
            }
        });

        setListAdapter(cursorAdapter);
        //getListView().setOnItemClickListener(this);
        getLoaderManager().initLoader(CONTACTS_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        Log.d(TAG, ">>> onCreateLoader: laoderId: " + loaderId);

        CursorLoader cursorLoader = null;
        switch(loaderId) {
            case CONTACTS_LOADER:
                //cursorLoader = new CursorLoader(getActivity(), ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                //Only having phone number(s)
                //cursorLoader = new CursorLoader(getActivity(), ContactsContract.Contacts.CONTENT_URI, null, ContactsContract.Contacts.HAS_PHONE_NUMBER + " > 0", null, null);
                //cursorLoader = new CursorLoader(getActivity(), ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
                cursorLoader = new CursorLoader(getActivity(), ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.Contacts.HAS_PHONE_NUMBER + " > 0", null, null);
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
    }
}
