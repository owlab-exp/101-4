package com.owlab.callquieter.contentprovider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by ernest on 5/26/16.
 */
public class CallQuieterContentProvider extends ContentProvider {
    private static final String TAG = CallQuieterContentProvider.class.getSimpleName();

    public static final String AUTHORITY = "com.owlab.callquieter.contentprovider";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    //REGISTERED_NUMBER
    public static final String REGISTERED_NUMBER_PATH = CallQuieterDb.TBL_REGISTERED_NUMBER;
    public static final Uri REGISTERED_NUMBER_URI = Uri.parse("content://" + AUTHORITY + "/" + REGISTERED_NUMBER_PATH);

    private static final int REGISTERED_NUMBERS = 1;
    private static final int REGISTERED_NUMBER_ID = 2;
    private static final int REGISTERED_NUMBER_PHONE_NUMBER = 3;
    private static final int REGISTERED_NUMBER_DISPLAY_NAME = 4;
    private static final int REGISTERED_NUMBER_IS_ACTIVE = 5;
    private static final int REGISTERED_NUMBER_CREATED_AT = 6;

    static {
        sURIMatcher.addURI(AUTHORITY, REGISTERED_NUMBER_PATH, REGISTERED_NUMBERS);
        sURIMatcher.addURI(AUTHORITY, REGISTERED_NUMBER_PATH + "/#", REGISTERED_NUMBER_ID);
        sURIMatcher.addURI(AUTHORITY, REGISTERED_NUMBER_PATH + "/#/" + CallQuieterDb.COLS_REGISTERED_NUMBER.PHONE_NUMBER, REGISTERED_NUMBER_PHONE_NUMBER);
        sURIMatcher.addURI(AUTHORITY, REGISTERED_NUMBER_PATH + "/#/" + CallQuieterDb.COLS_REGISTERED_NUMBER.DISPLAY_NAME, REGISTERED_NUMBER_DISPLAY_NAME);
        sURIMatcher.addURI(AUTHORITY, REGISTERED_NUMBER_PATH + "/#/" + CallQuieterDb.COLS_REGISTERED_NUMBER.IS_ACTIVE, REGISTERED_NUMBER_IS_ACTIVE);
        sURIMatcher.addURI(AUTHORITY, REGISTERED_NUMBER_PATH + "/#/" + CallQuieterDb.COLS_REGISTERED_NUMBER.CREATED_AT, REGISTERED_NUMBER_CREATED_AT);
    }

    //BLOCKED CALL
    public static final String QUIETED_CALL_PATH = CallQuieterDb.TBL_QUIETED_CALL;
    public static final Uri QUIETED_CALL_URI = Uri.parse("content://" + AUTHORITY + "/" + QUIETED_CALL_PATH);

    private static final int QUIETED_CALLS = 11;
    private static final int QUIETED_CALL_ID = 12;
    private static final int QUIETED_CALL_NUMBER = 13;
    private static final int QUIETED_CALL_TYPE = 14;
    private static final int QUIETED_CALL_DATE = 15;
    private static final int QUIETED_CALL_DURATION = 16;

    static {
        sURIMatcher.addURI(AUTHORITY, QUIETED_CALL_PATH, QUIETED_CALLS);
        sURIMatcher.addURI(AUTHORITY, QUIETED_CALL_PATH + "/#", QUIETED_CALL_ID);
        sURIMatcher.addURI(AUTHORITY, QUIETED_CALL_PATH + "/#/" + CallQuieterDb.COLS_QUIETED_CALL.NUMBER, QUIETED_CALL_NUMBER);
        sURIMatcher.addURI(AUTHORITY, QUIETED_CALL_PATH + "/#/" + CallQuieterDb.COLS_QUIETED_CALL.TYPE, QUIETED_CALL_TYPE);
        sURIMatcher.addURI(AUTHORITY, QUIETED_CALL_PATH + "/#/" + CallQuieterDb.COLS_QUIETED_CALL.DATE, QUIETED_CALL_DATE);
        sURIMatcher.addURI(AUTHORITY, QUIETED_CALL_PATH + "/#/" + CallQuieterDb.COLS_QUIETED_CALL.DURATION, QUIETED_CALL_DURATION);
    }

    private CallQuieterDbHelper mCallQuieterDbHelper;// = new CallQuieterDbHelper(getContext());

    public CallQuieterContentProvider() {

    }

    ContentResolver contentResolver;

    @Override
    public boolean onCreate() {
        mCallQuieterDbHelper =  new CallQuieterDbHelper(getContext());
        contentResolver = getContext().getContentResolver();
        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        //Log.d(TAG, ">>>>> getType called, uri: " + uri.toString());
        String type = null;
        switch(sURIMatcher.match(uri)) {
            case REGISTERED_NUMBERS:
                type = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + CallQuieterDb.TBL_REGISTERED_NUMBER;
                break;

            case QUIETED_CALLS:
                type = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + CallQuieterDb.TBL_QUIETED_CALL;
                break;

            default:
                Log.e(TAG, ">>>>> unsupported uri: " + uri.toString());
        }
        return type;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        //Log.d(TAG, ">>>>> selection: " + selection);

        SQLiteDatabase db = null;
        Cursor cursor = null;
        switch(sURIMatcher.match(uri)) {
            case REGISTERED_NUMBERS:
                if (TextUtils.isEmpty(sortOrder)) sortOrder = CallQuieterDb.COLS_REGISTERED_NUMBER.CREATED_AT + " DESC";
                db = mCallQuieterDbHelper.getReadableDatabase();
                cursor = db.query(CallQuieterDb.TBL_REGISTERED_NUMBER, projection, selection, selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(contentResolver, REGISTERED_NUMBER_URI);
                //return cursor;
                break;

            case QUIETED_CALLS:
                if (TextUtils.isEmpty(sortOrder)) sortOrder = CallQuieterDb.COLS_QUIETED_CALL.DATE + " DESC";
                db = mCallQuieterDbHelper.getReadableDatabase();
                cursor = db.query(CallQuieterDb.TBL_QUIETED_CALL, projection, selection, selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(contentResolver, QUIETED_CALL_URI);
                //return cursor;
                break;

            default:
                Log.e(TAG, ">>>>> unsupported uri: " + uri.toString());
        }

        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        Log.d(TAG, ">>>>> insert called with uri: " + uri.toString() + ", values: " + contentValues.toString());
        SQLiteDatabase db = mCallQuieterDbHelper.getWritableDatabase();

        Uri resultUri = null;
        long rowId = 0L;

        try {
            switch(sURIMatcher.match(uri)) {
                case REGISTERED_NUMBERS:
                    rowId = db.insertOrThrow(CallQuieterDb.TBL_REGISTERED_NUMBER, null, contentValues);
                    resultUri = Uri.parse(CallQuieterDb.TBL_REGISTERED_NUMBER + "/" + rowId);
                    break;
                case QUIETED_CALLS:
                    rowId = db.insertOrThrow(CallQuieterDb.TBL_QUIETED_CALL, null, contentValues);
                    resultUri = Uri.parse(CallQuieterDb.TBL_QUIETED_CALL + "/" + rowId);
                    break;
                default:
                    Log.e(TAG, ">>>>> unsupported uri: " + uri.toString());
            }
        } catch(SQLiteConstraintException e) {
            Log.e(TAG, ">>>>> Exception occurred: " + e.getMessage());
            //If db.insert then the DATABASE will return -1, so ...
            //rowId = -1;
        }

        if(rowId > 0) contentResolver.notifyChange(uri, null);

        return resultUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mCallQuieterDbHelper.getWritableDatabase();
        String id = null;
        int rowsDeleted = 0;

        switch (sURIMatcher.match(uri)) {
            case REGISTERED_NUMBER_ID:
                id = uri.getLastPathSegment();
                if(TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(CallQuieterDb.TBL_REGISTERED_NUMBER, CallQuieterDb.COLS_REGISTERED_NUMBER._ID + "=" + id, null);
                } else {
                    //TODO Meaningfull?
                    rowsDeleted = db.delete(CallQuieterDb.TBL_REGISTERED_NUMBER, CallQuieterDb.COLS_REGISTERED_NUMBER._ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            case REGISTERED_NUMBERS:
                rowsDeleted = db.delete(CallQuieterDb.TBL_REGISTERED_NUMBER, selection, selectionArgs);
                break;
            case QUIETED_CALL_ID:
                id = uri.getLastPathSegment();
                if(TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(CallQuieterDb.TBL_QUIETED_CALL, CallQuieterDb.COLS_QUIETED_CALL._ID + "=" + id, null);
                } else {
                    rowsDeleted = db.delete(CallQuieterDb.TBL_QUIETED_CALL, CallQuieterDb.COLS_QUIETED_CALL._ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            case QUIETED_CALLS:
                rowsDeleted = db.delete(CallQuieterDb.TBL_QUIETED_CALL, selection, selectionArgs);
                break;
            default:
                Log.e(TAG, ">>>>> unsupported uri: " + uri.toString());
        }

        if(rowsDeleted > 0) contentResolver.notifyChange(uri, null);

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        Log.d(TAG, ">>> onContentChanged called: contentVaues = " + contentValues + ", selection = " + selection);
        SQLiteDatabase db = mCallQuieterDbHelper.getWritableDatabase();
        int rowsUpdated = 0;
        switch(sURIMatcher.match(uri)) {
            case REGISTERED_NUMBERS:
                rowsUpdated = db.update(CallQuieterDb.TBL_REGISTERED_NUMBER, contentValues, selection, selectionArgs);
                break;
            case QUIETED_CALLS:
                rowsUpdated = db.update(CallQuieterDb.TBL_QUIETED_CALL, contentValues, selection, selectionArgs);
                break;
            default:
                Log.e(TAG, ">>>>> unsupported uri: " + uri.toString());
        }

        if(rowsUpdated > 0) contentResolver.notifyChange(uri, null);

        return rowsUpdated;
    }
}
