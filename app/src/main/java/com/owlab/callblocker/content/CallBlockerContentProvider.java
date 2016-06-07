package com.owlab.callblocker.content;

import android.content.ContentProvider;
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
public class CallBlockerContentProvider extends ContentProvider {
    private static final String TAG = CallBlockerContentProvider.class.getSimpleName();

    public static final String AUTHORITY = "com.owlab.callblocker.contentprovider";
    public static final String BASE_PATH = CallBlockerTbl.Schema.TABLE_NAME;
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int FILTERED_ITEMS = 1;
    private static final int FILTERED_ITEM_ID = 2;
    private static final int FILTERED_ITEM_PHONE_NUMBER = 3;
    private static final int FILTERED_ITEM_DESCRIPTION = 4;
    private static final int FILTERED_ITEM_IS_ACTIVE = 5;
    private static final int FILTERED_ITEM_CREATED_AT = 6;

    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, FILTERED_ITEMS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", FILTERED_ITEM_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#/phone_number", FILTERED_ITEM_PHONE_NUMBER);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#/description", FILTERED_ITEM_DESCRIPTION);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#/is_active", FILTERED_ITEM_IS_ACTIVE);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#/created_at", FILTERED_ITEM_CREATED_AT);
    }

    private CallBlockerDbHelper mCallBlockerDbHelper;


    @Override
    public boolean onCreate() {
        mCallBlockerDbHelper =  new CallBlockerDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        switch(sURIMatcher.match(uri)) {
            case FILTERED_ITEMS:
                Log.d(TAG, ">>>>> case : FILTERED_ITEMS");
                if (TextUtils.isEmpty(sortOrder)) sortOrder = CallBlockerTbl.Schema.COLUMN_NAME_CREATED_AT + " DESC";
                break;
            default:
                Log.e(TAG, ">>>>> unsupported uri: " + uri.toString());
                //TODO no supported uri, how to handle?
        }

        SQLiteDatabase db = mCallBlockerDbHelper.getReadableDatabase();
        Cursor cursor = db.query(CallBlockerTbl.Schema.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), CONTENT_URI);
        Log.d(TAG, ">>>>> cursor.getCount(): " + cursor.getCount());
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        Log.d(TAG, ">>>>> getType called, uri: " + uri.toString());
        String type = null;
        switch(sURIMatcher.match(uri)) {
            case FILTERED_ITEMS:
                type = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + CallBlockerTbl.Schema.TABLE_NAME;
            default:
                //TODO what needed for non-matching uri?
        }
        return type;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        SQLiteDatabase db = mCallBlockerDbHelper.getWritableDatabase();

        long rowId = 0L;

        try {
            switch(sURIMatcher.match(uri)) {
                case FILTERED_ITEMS:
                    rowId = db.insertOrThrow(CallBlockerTbl.Schema.TABLE_NAME, null, contentValues);
                    break;
                default:
                    //TODO make default
            }
        } catch(SQLiteConstraintException e) {
            Log.e(TAG, ">>>>> Exception occurred: " + e.getMessage());
            //If db.insert then the DATABASE will return -1, so ...
            rowId = -1;
        }

        //TODO what is this?
        if(rowId > 0) getContext().getContentResolver().notifyChange(uri, null);

        return Uri.parse(CallBlockerTbl.Schema.TABLE_NAME + "/" + rowId);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mCallBlockerDbHelper.getWritableDatabase();
        int rowsDeleted = 0;

        switch (sURIMatcher.match(uri)) {
            case FILTERED_ITEM_ID:
                String id = uri.getLastPathSegment();
                if(TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(CallBlockerTbl.Schema.TABLE_NAME, CallBlockerTbl.Schema._ID + "=" + id, null);
                } else {
                    rowsDeleted = db.delete(CallBlockerTbl.Schema.TABLE_NAME, CallBlockerTbl.Schema._ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
        }

        if(rowsDeleted > 0) getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        Log.d(TAG, ">>> update called: contentVaues = " + contentValues + ", selection = " + selection);
        SQLiteDatabase db = mCallBlockerDbHelper.getWritableDatabase();
        int rowsUpdated = 0;
        switch(sURIMatcher.match(uri)) {
            case FILTERED_ITEMS:
                rowsUpdated = db.update(CallBlockerTbl.Schema.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
        }

        if(rowsUpdated > 0) getContext().getContentResolver().notifyChange(uri, null);

        return rowsUpdated;
    }
}
