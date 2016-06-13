package com.owlab.callblocker.contentobserver;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

/**
 * Created by ernest on 6/13/16.
 */
public class CallLogObserver extends ContentObserver {
    private static final String TAG = CallLogObserver.class.getSimpleName();

    private static int numOfInstance = 0;

    //private String phoneNumber;
    private Context context;
    private long startTime;

    public CallLogObserver(Handler handler, Context context) {
        super(handler);
        this.context = context;
        this.startTime = System.currentTimeMillis();
        Log.d(TAG, ">>>>> instantiated, numOfInstance: " + ++numOfInstance);
    }

    //public void setPhoneNumber(String phoneNumber) {
    //    this.phoneNumber = phoneNumber;
    //}

    @Override
    public boolean deliverSelfNotifications() {
        return true;
    }


    @Override
    public void onChange(boolean selfChange) {
        Log.d(TAG, ">>>>> call log changed");
        super.onChange(selfChange);

        //doDeleteIfNeeded();
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        Log.d(TAG, ">>>>> call log changed with uri");
        super.onChange(selfChange);

        doDeleteIfNeeded();
    }

    private void doDeleteIfNeeded() {
        try {
            String[] phoneNumbers = {"777"};
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED
                    ) {
                Log.e(TAG, ">>>>> READ/WRITE_CALL_LOG permission not granted");
                return;
            }

            //Default sort order is DATE DESC
            Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, CallLog.Calls.NUMBER + " = ? ", phoneNumbers, "");

            if(cursor.moveToFirst()) {
                do {
                    int idToDelete = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls._ID));
                    int deleteCount = context.getContentResolver().delete(CallLog.Calls.CONTENT_URI, CallLog.Calls._ID + " = ? " + " AND " + CallLog.Calls.DATE + " >= " + startTime, new String[]{String.valueOf(idToDelete)});
                    Log.d(TAG, "deleted: " + deleteCount + " call log(s)");
                } while(cursor.moveToNext());
            }

        } catch(Exception e) {
            Log.e(TAG, ">>>>> Error while deleting call log: " + e.getMessage());
        }
    }
}
