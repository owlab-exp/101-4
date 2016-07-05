package com.owlab.callquieter.util;

import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;

/**
 * Created by ernest on 7/5/16.
 */
public class FabMoveOnListScroll implements AbsListView.OnScrollListener {
    private static final String TAG = FabMoveOnListScroll.class.getSimpleName();

    private final FloatingActionButton fab;

    private int oldTop;
    private int oldFistVisibleItem;

    private int fabInitY;
    private int fabScrollDistance = 200;
    private boolean fabHidden;

    public FabMoveOnListScroll(FloatingActionButton fab) {
        this.fab = fab;
        this.fabInitY = fab.getScrollY();
    }


    @Override
    public void onScrollStateChanged(AbsListView absListView, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        View view = absListView.getChildAt(0);
        int top = (view == null) ? 0 : view.getTop();

        if(firstVisibleItem == oldFistVisibleItem) {
            if(top > oldTop) {
                onUpScroll();
            } else if(top < oldTop) {
                onDownScroll();
            }
        } else {
            if(firstVisibleItem < oldFistVisibleItem) {
                onUpScroll();
            } else {
                onDownScroll();
            }
        }

        oldTop = top;
        oldFistVisibleItem = firstVisibleItem;

    }

    private void onUpScroll() {
        Log.d(TAG, ">>>>> up scrolling");
        if(fabHidden) {

            fab.animate().cancel();
            fab.animate().translationY(fabInitY);
            fabHidden = false;
        }
    }

    private void onDownScroll() {
        Log.d(TAG, ">>>>> down scrolling");
        if(!fabHidden) {
            fab.animate().cancel();
            fab.animate().translationYBy(fabScrollDistance);
            fabHidden = true;
        }
    }
}
