package com.owlab.callblocker.util;

import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

/**
 * Created by ernest on 6/17/16.
 */
public class MoveUpwardBehavior extends CoordinatorLayout.Behavior<View> {
    private static final String TAG = MoveUpwardBehavior.class.getSimpleName();

    private static final boolean SNACKBAR_BEHAVIOR_ENABLED;

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        boolean result = SNACKBAR_BEHAVIOR_ENABLED && dependency instanceof Snackbar.SnackbarLayout;
        Log.d(TAG, ">>>>> layoutDependsOn: " + result);
        return result;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
        Log.d(TAG, ">>>>> translationY: " + translationY);
        child.setTranslationY(translationY);
        return true;
    }

    static {
        SNACKBAR_BEHAVIOR_ENABLED = Build.VERSION.SDK_INT >= 11;
    }
}
