package com.owlab.callblocker.util;

import android.content.Context;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;

/**
 * Created by ernest on 6/19/16.
 */
public class FilteredSimpleCursorAdapter extends SimpleCursorAdapter {
    protected Cursor mmCursor;
    protected Context mmContext;
    protected int mmLayout;

    public FilteredSimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        this.mmContext = context;
        this.mmLayout = layout;
    }

    //@Override
    //public View newView(Context context, Cursor cursor, ViewGroup parent) {
    //    Cursor local
    //}
}
