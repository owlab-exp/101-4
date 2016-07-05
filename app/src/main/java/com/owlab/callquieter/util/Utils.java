package com.owlab.callquieter.util;

import android.content.Context;
import android.preference.PreferenceManager;
import android.telephony.PhoneNumberUtils;
import android.util.TypedValue;

import com.owlab.callquieter.R;

import java.util.Locale;

/**
 * Created by ernest on 5/29/16.
 */
public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    public static String formatPhoneNumber(Context context, String phoneNumber) {
        String formattedPhoneNumber = null;
        //To support different builds
        //if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        //`Log.d(TAG, ">>>>> defafultCountryCode: " + Locale.getDefault().getCountry());
        String countryAndCode = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.settings_key_country_and_code), "");
        if(countryAndCode.isEmpty()) {

        formattedPhoneNumber = PhoneNumberUtils.formatNumber(phoneNumber, Locale.getDefault().getCountry());
        } else {
            formattedPhoneNumber = PhoneNumberUtils.formatNumber(phoneNumber, countryAndCode.split(":")[1]);
        }
        return formattedPhoneNumber;
    }

    public static int convertDip2Pixels(Context context, int dip) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, context.getResources().getDisplayMetrics());
    }
}
