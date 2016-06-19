package com.owlab.callblocker.content;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ernest on 5/17/16.
 */
public class CallBlockerDbHelper extends SQLiteOpenHelper {
    private static final String TAG = CallBlockerDbHelper.class.getSimpleName();

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "CallBlocker.db";

    public CallBlockerDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CallBlockerTbl.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(CallBlockerTbl.SQL_DROP_TABLE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //onUpgrade(db);
    }


    //Several helper methods for other use
    public boolean hasPhoneNumber(String phoneNumber) {
        //Log.d(TAG, ">>> phoneNumber: " + phoneNumber);
        if(phoneNumber == null) {
            return false;
        }

        String purePhoneNumber = phoneNumber.replaceAll("[^\\d]", "");

        //Log.d(TAG, ">>> purePoneNumber: " + purePhoneNumber);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(CallBlockerTbl.Schema.TABLE_NAME,
                new String[]{CallBlockerTbl.Schema.COLUMN_NAME_PHONE_NUMBER},
                CallBlockerTbl.Schema.COLUMN_NAME_PHONE_NUMBER + " = ? ",
                new String[]{purePhoneNumber},
                null,
                null,
                null);

        boolean result = false;
        //Log.d(TAG, ">>>>> count: " + cursor.getCount());
        if(cursor != null && cursor.getCount() > 0) {
            result = true;
        }

        db.close();

        return result;
    }
}
