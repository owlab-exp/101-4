package com.owlab.callblocker.contentprovider;

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
public class CallBlockerProvider extends ContentProvider {
    private static final String TAG = CallBlockerProvider.class.getSimpleName();

    public static final String AUTHORITY = "com.owlab.callblocker.contentprovider";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    //BLOCKED_NUMBER
    public static final String BLOCKED_NUMBER_PATH = CallBlockerDb.TBL_BLOCKED_NUMBER;
    public static final Uri BLOCKED_NUMBER_URI = Uri.parse("content://" + AUTHORITY + "/" + BLOCKED_NUMBER_PATH);

    private static final int BLOCKED_NUMBERS = 1;
    private static final int BLOCKED_NUMBER_ID = 2;
    private static final int BLOCKED_NUMBER_PHONE_NUMBER = 3;
    private static final int BLOCKED_NUMBER_DISPLAY_NAME = 4;
    private static final int BLOCKED_NUMBER_IS_ACTIVE = 5;
    private static final int BLOCKED_NUMBER_CREATED_AT = 6;

    static {
        sURIMatcher.addURI(AUTHORITY, BLOCKED_NUMBER_PATH, BLOCKED_NUMBERS);
        sURIMatcher.addURI(AUTHORITY, BLOCKED_NUMBER_PATH + "/#", BLOCKED_NUMBER_ID);
        sURIMatcher.addURI(AUTHORITY, BLOCKED_NUMBER_PATH + "/#/" + CallBlockerDb.COLS_BLOCKED_NUMBER.PHONE_NUMBER, BLOCKED_NUMBER_PHONE_NUMBER);
        sURIMatcher.addURI(AUTHORITY, BLOCKED_NUMBER_PATH + "/#/" + CallBlockerDb.COLS_BLOCKED_NUMBER.DISPLAY_NAME, BLOCKED_NUMBER_DISPLAY_NAME);
        sURIMatcher.addURI(AUTHORITY, BLOCKED_NUMBER_PATH + "/#/" + CallBlockerDb.COLS_BLOCKED_NUMBER.IS_ACTIVE, BLOCKED_NUMBER_IS_ACTIVE);
        sURIMatcher.addURI(AUTHORITY, BLOCKED_NUMBER_PATH + "/#/" + CallBlockerDb.COLS_BLOCKED_NUMBER.CREATED_AT, BLOCKED_NUMBER_CREATED_AT);
    }

    //BLOCKED CALL
    public static final String BLOCKED_CALL_PATH = CallBlockerDb.TBL_BLOCKED_CALL;
    public static final Uri BLOCKED_CALL_URI = Uri.parse("content://" + AUTHORITY + "/" + BLOCKED_CALL_PATH);

    private static final int BLOCKED_CALLS = 11;
    private static final int BLOCKED_CALL_ID = 12;
    private static final int BLOCKED_CALL_NUMBER = 13;
    private static final int BLOCKED_CALL_TYPE = 14;
    private static final int BLOCKED_CALL_DATE = 15;
    private static final int BLOCKED_CALL_DURATION = 16;

    static {
        sURIMatcher.addURI(AUTHORITY, BLOCKED_CALL_PATH, BLOCKED_CALLS);
        sURIMatcher.addURI(AUTHORITY, BLOCKED_CALL_PATH + "/#", BLOCKED_CALL_ID);
        sURIMatcher.addURI(AUTHORITY, BLOCKED_CALL_PATH + "/#/" + CallBlockerDb.COLS_BLOCKED_CALL.NUMBER, BLOCKED_CALL_NUMBER);
        sURIMatcher.addURI(AUTHORITY, BLOCKED_CALL_PATH + "/#/" + CallBlockerDb.COLS_BLOCKED_CALL.TYPE, BLOCKED_CALL_TYPE);
        sURIMatcher.addURI(AUTHORITY, BLOCKED_CALL_PATH + "/#/" + CallBlockerDb.COLS_BLOCKED_CALL.DATE, BLOCKED_CALL_DATE);
        sURIMatcher.addURI(AUTHORITY, BLOCKED_CALL_PATH + "/#/" + CallBlockerDb.COLS_BLOCKED_CALL.DURATION, BLOCKED_CALL_DURATION);
    }

    private CallBlockerDbHelper mCallBlockerDbHelper;// = new CallBlockerDbHelper(getContext());

    public CallBlockerProvider() {

    }

    ContentResolver contentResolver;

    @Override
    public boolean onCreate() {
        mCallBlockerDbHelper =  new CallBlockerDbHelper(getContext());
        contentResolver = getContext().getContentResolver();
        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        //Log.d(TAG, ">>>>> getType called, uri: " + uri.toString());
        String type = null;
        switch(sURIMatcher.match(uri)) {
            case BLOCKED_NUMBERS:
                type = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + CallBlockerDb.TBL_BLOCKED_NUMBER;
                break;

            case BLOCKED_CALLS:
                type = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + CallBlockerDb.TBL_BLOCKED_CALL;
                break;

            default:
                //TODO what needed for non-matching uri?
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
            case BLOCKED_NUMBERS:
                if (TextUtils.isEmpty(sortOrder)) sortOrder = CallBlockerDb.COLS_BLOCKED_NUMBER.CREATED_AT + " DESC";
                db = mCallBlockerDbHelper.getReadableDatabase();
                cursor = db.query(CallBlockerDb.TBL_BLOCKED_NUMBER, projection, selection, selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(contentResolver, BLOCKED_NUMBER_URI);
                //return cursor;
                break;

            case BLOCKED_CALLS:
                if (TextUtils.isEmpty(sortOrder)) sortOrder = CallBlockerDb.COLS_BLOCKED_CALL.DATE + " DESC";
                db = mCallBlockerDbHelper.getReadableDatabase();
                cursor = db.query(CallBlockerDb.TBL_BLOCKED_CALL, projection, selection, selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(contentResolver, BLOCKED_CALL_URI);
                //return cursor;
                break;

            default:
                Log.e(TAG, ">>>>> unsupported uri: " + uri.toString());
                //TODO no supported uri, how to handle?
        }

        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        Log.d(TAG, ">>>>> insert called with uri: " + uri.toString() + ", values: " + contentValues.toString());
        SQLiteDatabase db = mCallBlockerDbHelper.getWritableDatabase();

        Uri resultUri = null;
        long rowId = 0L;

        try {
            switch(sURIMatcher.match(uri)) {
                case BLOCKED_NUMBERS:
                    rowId = db.insertOrThrow(CallBlockerDb.TBL_BLOCKED_NUMBER, null, contentValues);
                    resultUri = Uri.parse(CallBlockerDb.TBL_BLOCKED_NUMBER + "/" + rowId);
                    break;
                case BLOCKED_CALLS:
                    rowId = db.insertOrThrow(CallBlockerDb.TBL_BLOCKED_CALL, null, contentValues);
                    resultUri = Uri.parse(CallBlockerDb.TBL_BLOCKED_CALL + "/" + rowId);
                    break;
                default:
                    //TODO make default
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
        SQLiteDatabase db = mCallBlockerDbHelper.getWritableDatabase();
        String id = null;
        int rowsDeleted = 0;

        switch (sURIMatcher.match(uri)) {
            case BLOCKED_NUMBER_ID:
                id = uri.getLastPathSegment();
                if(TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(CallBlockerDb.TBL_BLOCKED_NUMBER, CallBlockerDb.COLS_BLOCKED_NUMBER._ID + "=" + id, null);
                } else {
                    //TODO Meaningfull?
                    rowsDeleted = db.delete(CallBlockerDb.TBL_BLOCKED_NUMBER, CallBlockerDb.COLS_BLOCKED_NUMBER._ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            case BLOCKED_NUMBERS:
                rowsDeleted = db.delete(CallBlockerDb.TBL_BLOCKED_NUMBER, selection, selectionArgs);
                break;
            case BLOCKED_CALL_ID:
                id = uri.getLastPathSegment();
                if(TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(CallBlockerDb.TBL_BLOCKED_CALL, CallBlockerDb.COLS_BLOCKED_CALL._ID + "=" + id, null);
                } else {
                    rowsDeleted = db.delete(CallBlockerDb.TBL_BLOCKED_CALL, CallBlockerDb.COLS_BLOCKED_CALL._ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            case BLOCKED_CALLS:
                rowsDeleted = db.delete(CallBlockerDb.TBL_BLOCKED_CALL, selection, selectionArgs);
                break;
            default:
                Log.e(TAG, ">>>>> unsupported uri: " + uri.toString());
        }

        if(rowsDeleted > 0) contentResolver.notifyChange(uri, null);

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        Log.d(TAG, ">>> update called: contentVaues = " + contentValues + ", selection = " + selection);
        SQLiteDatabase db = mCallBlockerDbHelper.getWritableDatabase();
        int rowsUpdated = 0;
        switch(sURIMatcher.match(uri)) {
            case BLOCKED_NUMBERS:
                rowsUpdated = db.update(CallBlockerDb.TBL_BLOCKED_NUMBER, contentValues, selection, selectionArgs);
                break;
            case BLOCKED_CALLS:
                rowsUpdated = db.update(CallBlockerDb.TBL_BLOCKED_CALL, contentValues, selection, selectionArgs);
                break;
            default:
                Log.e(TAG, ">>>>> unsupported uri: " + uri.toString());
        }

        if(rowsUpdated > 0) contentResolver.notifyChange(uri, null);

        return rowsUpdated;
    }
}
