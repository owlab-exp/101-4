package com.owlab.callblocker.contentprovider;

import android.provider.BaseColumns;

import com.owlab.callblocker.CONS;

/**
 * Created by ernest on 5/17/16.
 */
public class CallQuieterDb {
    public static final String TBL_REGISTERED_NUMBER = "REGISTERED_NUMBER";
    public static final String TBL_QUIETED_CALL = "QUIETED_CALL";

    public CallQuieterDb() {}

    public static abstract class COLS_REGISTERED_NUMBER implements BaseColumns {

        public static final String PHONE_NUMBER = "phone_number";
        public static final String DISPLAY_NAME = "display_name";
        public static final String MATCH_METHOD = "match_method";
        public static final String IS_ACTIVE = "is_active";
        public static final String CREATED_AT = "created_at";
        public static final String MARK_DELETED = "mark_deleted";
    }

    private static final String TYPE_TEXT = " TEXT";
    private static final String TYPE_INTEGER = " INTEGER";
    private static final String TYPE_CURRENT_TIMESTAMP = " DATETIME DEFAULT CURRENT_TIMESTAMP";
    private static final String SEP_COMMA = ", ";

    static final String SQL_CREATE_TABLE_REGISTERED_NUMBER =
            "CREATE TABLE " + TBL_REGISTERED_NUMBER + " (" +
                    COLS_REGISTERED_NUMBER._ID + " INTEGER PRIMARY KEY" + SEP_COMMA +
                    COLS_REGISTERED_NUMBER.PHONE_NUMBER + TYPE_TEXT + " NOT NULL UNIQUE" + SEP_COMMA +
                    COLS_REGISTERED_NUMBER.DISPLAY_NAME + TYPE_TEXT + SEP_COMMA +
                    COLS_REGISTERED_NUMBER.MATCH_METHOD + TYPE_INTEGER + " DEFAULT " + CONS.MATCH_METHOD_EXACT + SEP_COMMA +
                    COLS_REGISTERED_NUMBER.IS_ACTIVE + TYPE_INTEGER + " DEFAULT 1" + SEP_COMMA +
                    COLS_REGISTERED_NUMBER.CREATED_AT +  TYPE_CURRENT_TIMESTAMP + SEP_COMMA +
                    COLS_REGISTERED_NUMBER.MARK_DELETED +  TYPE_INTEGER + " DEFAULT 0" +
                    ");" +
                    "CREATE UNIQUE INDEX " + COLS_REGISTERED_NUMBER.PHONE_NUMBER + "_idx ON " + TBL_REGISTERED_NUMBER + " (" + COLS_REGISTERED_NUMBER.PHONE_NUMBER + " );" +
                    "CREATE INDEX number_and_extra_idx ON " + TBL_REGISTERED_NUMBER + " (" + COLS_REGISTERED_NUMBER.PHONE_NUMBER +  ", " + COLS_REGISTERED_NUMBER.MATCH_METHOD + ", " + COLS_REGISTERED_NUMBER.IS_ACTIVE + ", " + COLS_REGISTERED_NUMBER.MARK_DELETED + " );"
            ;
    static final String SQL_DROP_TABLE_REGISTERED_NUMBER =
            "DROP TABLE IF EXISTS " + TBL_REGISTERED_NUMBER;

    public static class COLS_QUIETED_CALL implements BaseColumns {
        public static final String NUMBER = "phone_number"; //CallLog.Calls.NUMBER
        public static final String TYPE = "call_type"; //int - CallLog.Calls.TYPE
        public static final String DATE = "call_date"; //long type string - CallLog.Calls.DATE
        public static final String DURATION = "call_duration"; //int type string - CallLog.Calls.DURATION
        public static final String MARK_DELETED = "mark_deleted";
    }

    static final String SQL_CREATE_TABLE_QUIETED_CALL =
            "CREATE TABLE " + TBL_QUIETED_CALL + " (" +
                    COLS_QUIETED_CALL._ID + " INTEGER PRIMARY KEY" + SEP_COMMA +
                    COLS_QUIETED_CALL.NUMBER + TYPE_TEXT + SEP_COMMA +
                    COLS_QUIETED_CALL.TYPE + TYPE_INTEGER + SEP_COMMA +
                    COLS_QUIETED_CALL.DATE + TYPE_TEXT + SEP_COMMA +
                    COLS_QUIETED_CALL.DURATION + TYPE_TEXT + SEP_COMMA +
                    COLS_QUIETED_CALL.MARK_DELETED +  TYPE_INTEGER + " DEFAULT 0" +
                    ");" +
                    "CREATE INDEX compNumberDate_idx ON " + TBL_QUIETED_CALL + "(" + COLS_QUIETED_CALL.NUMBER + ", " + COLS_QUIETED_CALL.DATE + ");";

    static final String SQL_DROP_TABLE_QUIETED_CALL =
            "DROP TABLE IF EXISTS " + TBL_QUIETED_CALL;

}
