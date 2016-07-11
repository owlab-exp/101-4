package com.owlab.callquieter.util;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

/**
 * Created by ernest on 6/17/16.
 */
@CoordinatorLayout.DefaultBehavior(MoveUpwardBehavior.class)
public class SnackbarCompatLinearLayout extends LinearLayout {
    private static final String TAG = SnackbarCompatLinearLayout.class.getSimpleName();

    public SnackbarCompatLinearLayout(Context context) {
        super(context);
        ////Log.d(TAG, ">>>>> call with context");
    }

    public SnackbarCompatLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        ////Log.d(TAG, ">>>>> call with context and attrs");
    }

    public SnackbarCompatLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs,  defStyleAttr);
        ////Log.d(TAG, ">>>>> call with context, attrs, and defstyleattr");
    }
}
