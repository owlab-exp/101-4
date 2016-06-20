package com.owlab.callblocker;

import android.telephony.PhoneNumberUtils;

import java.util.Locale;

/**
 * Created by ernest on 5/29/16.
 */
public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    public static String formatPhoneNumber(String phoneNumber) {
        String formattedPhoneNumber = null;
        //To support different builds
        //if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        //`Log.d(TAG, ">>>>> defafultCountryCode: " + Locale.getDefault().getCountry());
        formattedPhoneNumber = PhoneNumberUtils.formatNumber(phoneNumber, Locale.getDefault().getCountry());
        //} else {
        //    formattedPhoneNumber = PhoneNumberUtils.formatNumber(phoneNumber);
        //}
        return formattedPhoneNumber;
    }
}
