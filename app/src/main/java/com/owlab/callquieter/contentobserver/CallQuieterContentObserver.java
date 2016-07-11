package com.owlab.callquieter.contentobserver;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

/**
 * Created by ernest on 7/2/16.
 */
public class CallQuieterContentObserver extends ContentObserver {
    private static final String TAG = CallQuieterContentChangeListener.class.getSimpleName();

    CallQuieterContentChangeListener listener;

    public CallQuieterContentObserver(Handler handler, CallQuieterContentChangeListener listener) {
        super(handler);
        this.listener = listener;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange, null);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {

        ////Log.d(TAG, ">>>>> onChange: selfChange: " + selfChange + ", uri: " + uri);

        listener.onContentChanged();
    }

}
