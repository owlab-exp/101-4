package com.owlab.callblocker.custom;

import android.content.Context;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

/**
 * Created by ernest on 6/28/16.
 */
public class SpinnerPreference extends DialogPreference {
    private static final String TAG = SpinnerPreference.class.getSimpleName();

    public SpinnerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
    @Override
    public void setDialogMessage(CharSequence dialogMessage) {
        super.setDialogMessage(dialogMessage);

        Log.d(TAG, ">>>>> setDialogMessage: " + dialogMessage);
    }

    @Override
    public CharSequence getDialogMessage() {
        CharSequence dialogMessage = super.getDialogMessage();
        Log.d(TAG, ">>>>> getDialogMessage: " + dialogMessage);
        return dialogMessage;
    }
    */
}
