package com.owlab.callquieter.contentobserver;

import android.Manifest;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.owlab.callquieter.CONS;
import com.owlab.callquieter.contentprovider.CallQuieterDb;
import com.owlab.callquieter.contentprovider.CallQuieterDbHelper;
import com.owlab.callquieter.contentprovider.CallQuieterContentProvider;

/**
 * Created by ernest on 6/13/16.
 */
public class CallLogObserver extends ContentObserver {
    private static final String TAG = CallLogObserver.class.getSimpleName();

    private static int numOfInstance = 0;

    private Context context;
    private ContentResolver contentResolver;
    private CallQuieterDbHelper callQuieterDbHelper;

    private Service starter;

    private String phoneNumber;
    private long timeFrom;
    private boolean delete;

    //public CallLogObserver(Handler handler, Context context, Service starter, String phoneNumber, long timeFrom, boolean delete) {
    //public CallLogObserver(Handler handler, Service starter, String phoneNumber, long timeFrom, boolean delete) {
    public CallLogObserver(Handler handler, Service starter, Bundle args) {

        super(handler);
        this.context = starter.getBaseContext();
        this.contentResolver = context.getContentResolver();
        this.callQuieterDbHelper = new CallQuieterDbHelper(context);
        this.starter = starter;

        this.phoneNumber = args.getString(CONS.INTENT_KEY_PHONE_NUMBER);
        this.timeFrom = args.getLong(CONS.INTENT_KEY_TIME_FROM);
        this.delete = args.getBoolean(CONS.INTENT_KEY_SHOULD_DELETE);
        //this.phoneNumber = phoneNumber;
        //this.timeFrom = timeFrom;
        //this.delete = delete;
        //////Log.d(TAG, ">>>>> instantiated, numOfInstance: " + ++numOfInstance);
    }

    @Override
    public boolean deliverSelfNotifications() {
        return true;
    }


    @Override
    public void onChange(boolean selfChange) {
        ////Log.d(TAG, ">>>>> call log changed");
        super.onChange(selfChange, null);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        //This causes endless repeat!
        //super.onChange(selfChange, uri);
        ////Log.d(TAG, ">>>>> onChange,  parameter = (" + selfChange + ", " + uri.toString() + ")");

        //if(!selfChange) processLog();
        processLog();
        //Stop the holding service
        starter.stopSelf();
    }


    private static String[] callLogProjection = new String[] {
            CallLog.Calls._ID
            , CallLog.Calls.NUMBER
            , CallLog.Calls.TYPE
            , CallLog.Calls.DATE
            , CallLog.Calls.DURATION
    };

    private static String callLogSelection = CallLog.Calls.NUMBER + " = ? " + " AND " + CallLog.Calls.DATE + " >= ?";
    private static String callLogSelectionOrder = CallLog.Calls.DATE + " ASC";

    private void processLog() {
        try {
            String[] callLogSelectionArgs = {phoneNumber, Long.toString(timeFrom)};
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED
                    ) {
                Toast.makeText(context, "Cannot process log: lack of permission", Toast.LENGTH_SHORT).show();
                Log.e(TAG, ">>>>> READ/WRITE_CALL_LOG permission not granted");
                return;
            }

            //Default sort order is DATE DESC
            //Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI
            Cursor cursor = contentResolver.query(CallLog.Calls.CONTENT_URI
                    , callLogProjection
                    //, CallLog.Calls.NUMBER + " = ? " + " AND " + CallLog.Calls.DATE + " >= " + timeFrom
                    , callLogSelection
                    , callLogSelectionArgs
                    , callLogSelectionOrder);
            //Cursor cursor = context.getContentResolver().query(CallLog.Calls.REGISTERED_NUMBER_URI, null, CallLog.Calls.NUMBER + " = ? ", phoneNumbers, "");
            ////Log.d(TAG, ">>>>> " + cursor.getCount() + " calls found in log");

            int deleteCount = 0;

            if(cursor.getCount() > 0 && cursor.moveToFirst()) {
                do {
                    //copy the original log to com.owlab.callquieter db
                    String number = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
                    int type = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE));
                    String duration = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION));

                    //Defensive code
                    //Occasionally, the same call logs comes here
                    if(!callQuieterDbHelper.logExists(number, date)) {
                        ContentValues values = new ContentValues();
                        values.put(CallQuieterDb.COLS_QUIETED_CALL.NUMBER, number);
                        values.put(CallQuieterDb.COLS_QUIETED_CALL.TYPE, type);
                        values.put(CallQuieterDb.COLS_QUIETED_CALL.DATE, date);
                        values.put(CallQuieterDb.COLS_QUIETED_CALL.DURATION, duration);

                        //context.getContentResolver().insert(CallQuieterContentProvider.QUIETED_CALL_URI, values);
                        contentResolver.insert(CallQuieterContentProvider.QUIETED_CALL_URI, values);
                    }

                    //delete if needed
                    if(delete) {
                        int idToDelete = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls._ID));
                        //deleteCount += context.getContentResolver().delete(CallLog.Calls.CONTENT_URI, CallLog.Calls._ID + " = ? ", new String[]{String.valueOf(idToDelete)});
                        deleteCount += contentResolver.delete(CallLog.Calls.CONTENT_URI, CallLog.Calls._ID + " = ? ", new String[]{String.valueOf(idToDelete)});
                    }
                } while(cursor.moveToNext());
            }
            cursor.close();

            ////Log.d(TAG, ">>>>> " + deleteCount + " call log(s) deleted");

        } catch(Exception e) {
            Log.e(TAG, ">>>>> Error while deleting call log: " + e.getMessage());
        }
    }
}
