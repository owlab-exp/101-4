package com.owlab.callblocker.content;

import android.provider.BaseColumns;

/**
 * Created by ernest on 5/17/16.
 * TODO save and restore phone numbers to block
 */
public class CallBlockerTbl {
    public CallBlockerTbl() {}

    public static abstract class Schema implements BaseColumns {

        public static final String TABLE_NAME = "FILTERED_PHONE";

        public static final String COLUMN_NAME_PHONE_NUMBER = "phone_number";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_IS_ACTIVE = "is_active";
        public static final String COLUMN_NAME_CREATED_AT = "created_at";
    }

    private static final String TYPE_TEXT = " TEXT";
    private static final String TYPE_INTEGER = " INTEGER";
    private static final String TYPE_CURRENT_TIMESTAMP = " DATETIME DEFAULT CURRENT_TIMESTAMP";
    private static final String SEP_COMMA = ", ";

    //TODO how to add index to phone_number
    static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + Schema.TABLE_NAME + " (" +
                    Schema._ID + " INTEGER PRIMARY KEY" + SEP_COMMA +
                    Schema.COLUMN_NAME_PHONE_NUMBER + TYPE_TEXT + " NOT NULL UNIQUE" + SEP_COMMA +
                    Schema.COLUMN_NAME_DESCRIPTION + TYPE_TEXT + SEP_COMMA +
                    Schema.COLUMN_NAME_IS_ACTIVE + TYPE_INTEGER + " DEFAULT 1" + SEP_COMMA +
                    Schema.COLUMN_NAME_CREATED_AT +  TYPE_CURRENT_TIMESTAMP +
                    ");" +
                    "CREATE UNIQUE INDEX " + Schema.COLUMN_NAME_PHONE_NUMBER + "_idx ON " + Schema.TABLE_NAME + " (" + Schema.COLUMN_NAME_PHONE_NUMBER + " ASC);"
            ;
    static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + Schema.TABLE_NAME;
}
