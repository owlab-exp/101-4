package com.owlab.callblocker.content;

import android.provider.BaseColumns;

/**
 * Created by ernest on 5/17/16.
 * TODO save and restore phone numbers to block
 */
public class CallBlockerDb {
    public static final String TBL_BLOCKED_NUMBER = "BLOCKED_NUMBER";
    public static final String TBL_BLOCKED_CALL = "BLOCKED_CALL";

    public CallBlockerDb() {}

    public static abstract class COLS_BLOCKED_NUMBER implements BaseColumns {

        public static final String PHONE_NUMBER = "phone_number";
        public static final String DISPLAY_NAME = "display_name";
        public static final String IS_ACTIVE = "is_active";
        public static final String CREATED_AT = "created_at";
    }

    private static final String TYPE_TEXT = " TEXT";
    private static final String TYPE_INTEGER = " INTEGER";
    private static final String TYPE_CURRENT_TIMESTAMP = " DATETIME DEFAULT CURRENT_TIMESTAMP";
    private static final String SEP_COMMA = ", ";

    static final String SQL_CREATE_TABLE_BLOCKED_NUMBER =
            "CREATE TABLE " + TBL_BLOCKED_NUMBER + " (" +
                    COLS_BLOCKED_NUMBER._ID + " INTEGER PRIMARY KEY" + SEP_COMMA +
                    COLS_BLOCKED_NUMBER.PHONE_NUMBER + TYPE_TEXT + " NOT NULL UNIQUE" + SEP_COMMA +
                    COLS_BLOCKED_NUMBER.DISPLAY_NAME + TYPE_TEXT + SEP_COMMA +
                    COLS_BLOCKED_NUMBER.IS_ACTIVE + TYPE_INTEGER + " DEFAULT 1" + SEP_COMMA +
                    COLS_BLOCKED_NUMBER.CREATED_AT +  TYPE_CURRENT_TIMESTAMP +
                    ");" +
                    "CREATE UNIQUE INDEX " + COLS_BLOCKED_NUMBER.PHONE_NUMBER + "_idx ON " + TBL_BLOCKED_NUMBER + " (" + COLS_BLOCKED_NUMBER.PHONE_NUMBER + " ASC);"
            ;
    static final String SQL_DROP_TABLE_BLOCKED_NUMBER =
            "DROP TABLE IF EXISTS " + TBL_BLOCKED_NUMBER;

    public static class COLS_BLOCKED_CALL implements BaseColumns {
        public static final String NUMBER = "phone_number"; //CallLog.Calls.NUMBER
        public static final String TYPE = "call_type"; //int - CallLog.Calls.TYPE
        public static final String DATE = "call_date"; //long type string - CallLog.Calls.DATE
        public static final String DURATION = "call_duration"; //int type string - CallLog.Calls.DURATION
    }

    static final String SQL_CREATE_TABLE_BLOCKED_CALL =
            "CREATE TABLE " + TBL_BLOCKED_CALL + " (" +
                    COLS_BLOCKED_CALL._ID + " INTEGER PRIMARY KEY" + SEP_COMMA +
                    COLS_BLOCKED_CALL.NUMBER + TYPE_TEXT + SEP_COMMA +
                    COLS_BLOCKED_CALL.TYPE + TYPE_INTEGER + SEP_COMMA +
                    COLS_BLOCKED_CALL.DATE + TYPE_TEXT + SEP_COMMA +
                    COLS_BLOCKED_CALL.DURATION + TYPE_TEXT +
                    ");";

    static final String SQL_DROP_TABLE_BLOCKED_CALL =
            "DROP TABLE IF EXISTS " + TBL_BLOCKED_CALL;

}
