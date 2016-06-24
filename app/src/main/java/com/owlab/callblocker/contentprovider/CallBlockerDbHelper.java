package com.owlab.callblocker.contentprovider;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ernest on 5/17/16.
 */
public class CallBlockerDbHelper extends SQLiteOpenHelper {
    private static final String TAG = CallBlockerDbHelper.class.getSimpleName();

    public static final int DATABASE_VERSION = 3;
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
        onCreate(db);
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
                CallBlockerDb.COLS_BLOCKED_NUMBER.PHONE_NUMBER + " = ? ",
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

    private static String selectionOfActiveBlockedNumber = CallBlockerDb.COLS_BLOCKED_NUMBER.PHONE_NUMBER + " = ? AND " + CallBlockerDb.COLS_BLOCKED_NUMBER.IS_ACTIVE + " > 0";

    public boolean isActiveBlockedNumber(String phoneNumber) {
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
                selectionOfActiveBlockedNumber,
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
