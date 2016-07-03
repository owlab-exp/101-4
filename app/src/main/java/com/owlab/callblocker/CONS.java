package com.owlab.callblocker;

/**
 * Created by ernest on 6/8/16.
 */
public final class CONS {
    public static final int STATUSBAR_NOTIFICATION_ID = 111;

    public static final String PREF_KEY_BLOCKING_ON = "BLOCKING_ON";

    public static final int MATCH_METHOD_EXACT = 0;
    public static final int MATCH_METHOD_STARTS_WITH = 1;

    public static final String ACTION_UPDATE_MATCH_PATTERN = "com.owlab.callblocker.UPDATE_MATCH_PATTERN";

    public static final int REQUEST_CODE_ASK_PERMISSION_FOR_BLOCKING = 0;
    public static final int REQUEST_CODE_ASK_PERMISSION_FOR_BLOCK_HIDDEN_NUMBER = 5;
    public static final int REQUEST_CODE_ASK_PERMISSION_FOR_BLOCK_UNKNOWN_NUMBER = 7;
    public static final int REQUEST_CODE_ASK_PERMISSION_FOR_SUPPRESS_RINGING = 10;
    public static final int REQUEST_CODE_ASK_PERMISSION_FOR_DISMISS_CALL = 13;
    public static final int REQUEST_CODE_ASK_PERMISSION_FOR_DELETE_CALL_LOG = 15;
    public static final int REQUEST_CODE_ASK_PERMISSION_FOR_READ_CALL_LOG = 20;
    public static final int REQUEST_CODE_ASK_PERMISSION_FOR_READ_SMS_LOG = 25;
    public static final int REQUEST_CODE_ASK_PERMISSION_FOR_READ_CONTACTS = 30;
    public static final int REQUEST_CODE_ASK_PERMISSION_FOR_READ_QUIETED_CALLS = 35;

    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_FAIL = -1;
    public static final int RESULT_OTHER = -1;

    public static final String INTENT_KEY_TARGET_FRAGMENT = "TARGET_FRAGMENT";
    public static final String INTENT_KEY_TRANSITION_SOURCE = "TRANSITION_SOURCE";

    public static final String INTENT_KEY_PHONE_NUMBER = "PHONE_NUMBER";
    public static final String INTENT_KEY_TIME_FROM = "TIME_FROM";
    public static final String INTENT_KEY_SHOULD_DELETE = "SHOULD_DELETE";

    public static final int REQUEST_CODE_ADD_SOURCE_SELECTION = 11;

    public static final String ROW_COLOR_ALREADY_BLOCKED = "#D3D3D3";
    public static final String ROW_COLOR_UNSELECTED = "#ffffff";
    public static final String ROW_COLOR_SELECTED = "#ab82ff";

    public static final String ARG_KEY_REGISTERED_NUMBER_ID = "_id";
    public static final String ARG_KEY_REGISTERED_NUMBER = "phoneNumber";
    public static final String ARG_KEY_DISPLAY_NAME = "displayName";
}
