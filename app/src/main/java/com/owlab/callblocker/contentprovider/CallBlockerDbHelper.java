package com.owlab.callblocker.contentprovider;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.owlab.callblocker.CONS;

/**
 * Created by ernest on 5/17/16.
 */
public class CallBlockerDbHelper extends SQLiteOpenHelper {
    private static final String TAG = CallBlockerDbHelper.class.getSimpleName();

    public static final int DATABASE_VERSION = 6; //20160629
    public static final String DATABASE_NAME = "CallBlocker.db";

    public CallBlockerDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CallBlockerDb.SQL_CREATE_TABLE_BLOCKED_NUMBER);
        db.execSQL(CallBlockerDb.SQL_CREATE_TABLE_BLOCKED_CALL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(CallBlockerDb.SQL_DROP_TABLE_BLOCKED_NUMBER);
        db.execSQL(CallBlockerDb.SQL_DROP_TABLE_BLOCKED_CALL);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //onUpgrade(db);
    }


    //Several helper methods for other use
    public boolean isBlockedNumber(String phoneNumber) {
        //Log.d(TAG, ">>> phoneNumber: " + phoneNumber);
        boolean result = false;

        if(phoneNumber == null) {
            return result;
        }

        String purePhoneNumber = phoneNumber.replaceAll("[^\\d]", "");

        //Log.d(TAG, ">>> purePoneNumber: " + purePhoneNumber);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(CallBlockerDb.TBL_BLOCKED_NUMBER,
                new String[]{CallBlockerDb.COLS_BLOCKED_NUMBER.PHONE_NUMBER},
                CallBlockerDb.COLS_BLOCKED_NUMBER.PHONE_NUMBER + " = ? AND " + CallBlockerDb.COLS_BLOCKED_NUMBER.MARK_DELETED + " = 0",
                new String[]{purePhoneNumber},
                null,
                null,
                null);

        //Log.d(TAG, ">>>>> count: " + cursor.getCount());
        if(cursor != null && cursor.getCount() > 0) {
            result = true;
        }

        db.close();

        return result;
    }

    private static String selectionOfActiveBlockedNumberExact =
            CallBlockerDb.COLS_BLOCKED_NUMBER.PHONE_NUMBER + " = ?" +
                    " AND " + CallBlockerDb.COLS_BLOCKED_NUMBER.IS_ACTIVE + " > 0" +
                    " AND " + CallBlockerDb.COLS_BLOCKED_NUMBER.MATCH_METHOD + " = " + CONS.MATCH_METHOD_EXACT +
                    " AND " + CallBlockerDb.COLS_BLOCKED_NUMBER.MARK_DELETED + " = 0";

    public boolean isActiveBlockedNumberExact(String phoneNumber) {
        //Log.d(TAG, ">>> phoneNumber: " + phoneNumber);
        boolean result = false;

        if(phoneNumber == null) {
            return result;
        }

        String purePhoneNumber = phoneNumber.replaceAll("[^\\d]", "");

        //Log.d(TAG, ">>> purePoneNumber: " + purePhoneNumber);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(CallBlockerDb.TBL_BLOCKED_NUMBER,
                new String[]{CallBlockerDb.COLS_BLOCKED_NUMBER.PHONE_NUMBER},
                //CallBlockerDb.COLS_BLOCKED_NUMBER.PHONE_NUMBER + " = ? AND " + CallBlockerDb.COLS_BLOCKED_NUMBER.IS_ACTIVE + " > 0",
                selectionOfActiveBlockedNumberExact,
                new String[]{purePhoneNumber},
                null,
                null,
                null);

        //Log.d(TAG, ">>>>> count: " + cursor.getCount());
        if(cursor != null && cursor.getCount() > 0) {
            result = true;
        }

        db.close();

        Log.d(TAG, ">>>>> result: " + result);
        return result;
    }

    private static String selectionOfActiveBlockedNumberStartsWith =
            //CallBlockerDb.COLS_BLOCKED_NUMBER.PHONE_NUMBER + " = ?" +
                    CallBlockerDb.COLS_BLOCKED_NUMBER.IS_ACTIVE + " > 0" +
                    " AND " + CallBlockerDb.COLS_BLOCKED_NUMBER.MATCH_METHOD + " = " + CONS.MATCH_METHOD_STARTS_WITH +
                    " AND " + CallBlockerDb.COLS_BLOCKED_NUMBER.MARK_DELETED + " = 0";

    public boolean isActiveBlockedNumberStartsWith(String phoneNumber) {

        boolean result = false;

        //Weird code
        if(phoneNumber == null ) {
            return result;
        }

        String pureNumber = phoneNumber.replaceAll("[^\\d]", "");

        if(pureNumber.isEmpty()) {
            return result;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(CallBlockerDb.TBL_BLOCKED_NUMBER,
                new String[]{CallBlockerDb.COLS_BLOCKED_NUMBER.PHONE_NUMBER},
                selectionOfActiveBlockedNumberStartsWith,
                null,
                null,
                null,
                null);

        if(cursor != null && cursor.moveToFirst()) {
            do {
                String startsWith = cursor.getString(cursor.getColumnIndexOrThrow(CallBlockerDb.COLS_BLOCKED_NUMBER.PHONE_NUMBER));
                Log.d(TAG, ">>>>> startsWith: " + startsWith);
                if(phoneNumber.startsWith(startsWith)) {
                    result = true;
                    break;
                }
            } while(cursor.moveToNext());
        }

        db.close();
        return result;
    }

    public boolean logExists(String phoneNumber, String date) {
        boolean result = false;

        if(phoneNumber == null || date == null) {
            return result;
        }

        String purePhoneNumber = phoneNumber.replaceAll("[^\\d]", "");

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(CallBlockerDb.TBL_BLOCKED_CALL,
                new String[]{CallBlockerDb.COLS_BLOCKED_CALL._ID},
                CallBlockerDb.COLS_BLOCKED_CALL.NUMBER + " = ? AND " + CallBlockerDb.COLS_BLOCKED_CALL.DATE + " = ?",
                new String[]{purePhoneNumber, date},
                null,
                null,
                null);
        if(cursor != null && cursor.getCount() > 0) {
            result = true;
        }

        db.close();

        return result;
    }
}
