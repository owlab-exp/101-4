package com.owlab.callblocker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

/**
 * Created by ernest on 6/12/16.
 */
public class SMSReceiver extends BroadcastReceiver {
    private static final String TAG = SMSReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, ">>>>> received intent action: " + intent.getAction().toString());
        if(intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            //Bundle extra = intent.getExtras();
            //if(extra != null) {
            //    Object[] pdus = (Object[]) extra.get("pdus");
            //    SmsMessage[] msgs = new SmsMessage[pdus.length];
            //    for(int i = 0; i < msgs.length; i++) {
            //        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i], )
            //    }
            //}
            SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            for(SmsMessage message: messages) {
                String address = message.getOriginatingAddress();
                String body = message.getMessageBody();
                Log.d(TAG, ">>>>> message: from " + address + ", body: " + body);
            }
        }
    }
}
