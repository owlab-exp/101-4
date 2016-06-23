package com.owlab.callblocker;

/**
 * Created by ernest on 6/8/16.
 */
public final class CONS {
    public static final int STATUSBAR_NOTIFICATION_ID = 111;

    public static final int REQUEST_CODE_ASK_PERMISSION_FOR_BLOCKING = 0;
    public static final int REQUEST_CODE_ASK_PERMISSION_FOR_SUPPRESS_RINGING = 1;
    public static final int REQUEST_CODE_ASK_PERMISSION_FOR_DISMISS_CALL = 2;
    public static final int REQUEST_CODE_ASK_PERMISSION_FOR_DELETE_CALL_LOG = 3;
    public static final int REQUEST_CODE_ASK_PERMISSION_FOR_READ_CALL_LOG = 4;
    public static final int REQUEST_CODE_ASK_PERMISSION_FOR_READ_SMS_LOG = 5;
    public static final int REQUEST_CODE_ASK_PERMISSION_FOR_READ_CONTACTS = 6;
    public static final int REQUEST_CODE_ASK_PERMISSION_FOR_READ_BLOCKED_CALLS = 7;

    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_FAIL = -1;
    public static final int RESULT_OTHER = -1;

    public static final String INTENT_KEY_TARGET_FRAGMENT = "TARGET_FRAGMENT";
    public static final String INTENT_KEY_TRANSITION_SOURCE = "TRANSITION_SOURCE";

    public static final int REQUEST_CODE_ADD_SOURCE_SELECTION = 11;

    public static final String ROW_COLOR_ALREADY_BLOCKED = "#D3D3D3";
    public static final String ROW_COLOR_UNSELECTED = "#ffffff";
    public static final String ROW_COLOR_SELECTED = "#ab82ff";

    public static final String ARG_KEY_BLOCKED_NUMBER_ID = "_id";
    public static final String ARG_KEY_BLOCKED_NUMBER = "phoneNumber";
    public static final String ARG_KEY_DISPLAY_NAME = "displayName";
}
