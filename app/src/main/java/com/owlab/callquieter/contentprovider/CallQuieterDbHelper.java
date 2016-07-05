package com.owlab.callquieter.contentprovider;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.owlab.callquieter.CONS;

import java.util.regex.Pattern;

/**
 * Created by ernest on 5/17/16.
 */
public class CallQuieterDbHelper extends SQLiteOpenHelper {
    private static final String TAG = CallQuieterDbHelper.class.getSimpleName();

    public static final int DATABASE_VERSION = 9; //20160703
    public static final String DATABASE_NAME = "CallQuieter.db";

    public CallQuieterDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CallQuieterDb.SQL_CREATE_TABLE_REGISTERED_NUMBER);
        db.execSQL(CallQuieterDb.SQL_CREATE_TABLE_QUIETED_CALL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //drop
        db.execSQL(CallQuieterDb.SQL_DROP_TABLE_REGISTERED_NUMBER);
        db.execSQL(CallQuieterDb.SQL_DROP_TABLE_QUIETED_CALL);
        //create
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
        Cursor cursor = db.query(CallQuieterDb.TBL_REGISTERED_NUMBER,
                new String[]{CallQuieterDb.COLS_REGISTERED_NUMBER.PHONE_NUMBER},
                CallQuieterDb.COLS_REGISTERED_NUMBER.PHONE_NUMBER + " = ? AND " + CallQuieterDb.COLS_REGISTERED_NUMBER.MARK_DELETED + " = 0",
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
            CallQuieterDb.COLS_REGISTERED_NUMBER.PHONE_NUMBER + " = ?" +
                    " AND " + CallQuieterDb.COLS_REGISTERED_NUMBER.IS_ACTIVE + " > 0" +
                    " AND " + CallQuieterDb.COLS_REGISTERED_NUMBER.MATCH_METHOD + " = " + CONS.MATCH_METHOD_EXACT +
                    " AND " + CallQuieterDb.COLS_REGISTERED_NUMBER.MARK_DELETED + " = 0";

    public boolean isActiveBlockedNumberExact(String phoneNumber) {
        //Log.d(TAG, ">>> phoneNumber: " + phoneNumber);
        boolean result = false;

        if(phoneNumber == null) {
            return result;
        }

        String purePhoneNumber = phoneNumber.replaceAll("[^\\d]", "");

        //Log.d(TAG, ">>> purePoneNumber: " + purePhoneNumber);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(CallQuieterDb.TBL_REGISTERED_NUMBER,
                new String[]{CallQuieterDb.COLS_REGISTERED_NUMBER.PHONE_NUMBER},
                //CallQuieterDb.COLS_REGISTERED_NUMBER.PHONE_NUMBER + " = ? AND " + CallQuieterDb.COLS_REGISTERED_NUMBER.IS_ACTIVE + " > 0",
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
            //CallQuieterDb.COLS_REGISTERED_NUMBER.PHONE_NUMBER + " = ?" +
                    CallQuieterDb.COLS_REGISTERED_NUMBER.IS_ACTIVE + " > 0" +
                    " AND " + CallQuieterDb.COLS_REGISTERED_NUMBER.MATCH_METHOD + " = " + CONS.MATCH_METHOD_STARTS_WITH +
                    " AND " + CallQuieterDb.COLS_REGISTERED_NUMBER.MARK_DELETED + " = 0";

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
        Cursor cursor = db.query(CallQuieterDb.TBL_REGISTERED_NUMBER,
                new String[]{CallQuieterDb.COLS_REGISTERED_NUMBER.PHONE_NUMBER},
                selectionOfActiveBlockedNumberStartsWith,
                null,
                null,
                null,
                null);

        if(cursor != null && cursor.moveToFirst()) {
            do {
                String startsWith = cursor.getString(cursor.getColumnIndexOrThrow(CallQuieterDb.COLS_REGISTERED_NUMBER.PHONE_NUMBER));
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

    public Pattern getMatchPattern() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                CallQuieterDb.TBL_REGISTERED_NUMBER,
                new String[]{CallQuieterDb.COLS_REGISTERED_NUMBER.PHONE_NUMBER, CallQuieterDb.COLS_REGISTERED_NUMBER.MATCH_METHOD},
                CallQuieterDb.COLS_REGISTERED_NUMBER.IS_ACTIVE + " > 0 AND " + CallQuieterDb.COLS_REGISTERED_NUMBER.MARK_DELETED + " = 0",
                null,
                null,
                null,
                null);

        StringBuilder sb = new StringBuilder();

        if(cursor != null) {
            if(cursor.moveToFirst()) {
                boolean isFirst = true;
                do {
                    if(isFirst) {
                        isFirst = !isFirst;
                    } else {
                        sb.append("|");
                    }
                    String number = cursor.getString(cursor.getColumnIndexOrThrow(CallQuieterDb.COLS_REGISTERED_NUMBER.PHONE_NUMBER));
                    sb.append(number);
                    int matchMathod = cursor.getInt(cursor.getColumnIndexOrThrow(CallQuieterDb.COLS_REGISTERED_NUMBER.MATCH_METHOD));
                    if(matchMathod == CONS.MATCH_METHOD_EXACT) {

                    } else if(matchMathod == CONS.MATCH_METHOD_STARTS_WITH) {
                        sb.append(".*");
                    }
                } while(cursor.moveToNext());
            }
        }

        db.close();

        String patternStr = sb.toString();
        if(!patternStr.isEmpty()) {
            return Pattern.compile(patternStr);
        }
        return null;
    }

    public boolean logExists(String phoneNumber, String date) {
        boolean result = false;

        if(phoneNumber == null || date == null) {
            return result;
        }

        String purePhoneNumber = phoneNumber.replaceAll("[^\\d]", "");

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(CallQuieterDb.TBL_QUIETED_CALL,
                new String[]{CallQuieterDb.COLS_QUIETED_CALL._ID},
                CallQuieterDb.COLS_QUIETED_CALL.NUMBER + " = ? AND " + CallQuieterDb.COLS_QUIETED_CALL.DATE + " = ?",
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
