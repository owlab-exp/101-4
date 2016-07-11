package com.owlab.callquieter.custom;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.owlab.callquieter.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by ernest on 6/28/16.
 */
public class SpinnerPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {
    private static final String TAG = SpinnerPreferenceDialogFragmentCompat.class.getSimpleName();

    Map<String, String> countryNameCodeMap = new HashMap<>();
    ArrayList<String> countryNameList = new ArrayList<>();

    String selectedCountryNameNative;
    String selectedCountryNameComposite;
    String selectedCountryCode;

    public static SpinnerPreferenceDialogFragmentCompat newInstance(String key) {
        final SpinnerPreferenceDialogFragmentCompat fragment = new SpinnerPreferenceDialogFragmentCompat();
        final Bundle bundle = new Bundle(1);
        bundle.putString(PreferenceDialogFragmentCompat.ARG_KEY, key);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    protected View onCreateDialogView(Context context) {
        String key = getArguments().getString(PreferenceDialogFragmentCompat.ARG_KEY);
        String savedCountryAndCode = PreferenceManager.getDefaultSharedPreferences(context).getString(key, "");
        String savedCountryName = "";
        if(!savedCountryAndCode.isEmpty()) {
            savedCountryName = savedCountryAndCode.split(":")[0];
        }

        View view = LayoutInflater.from(context).inflate(R.layout.country_select_spinner_layout, null);

        //////Log.d(TAG, ">>>>> default locale's country: " + Locale.getDefault().getCountry());

        //Locales
        Locale[] locales = Locale.getAvailableLocales();

        for(Locale locale : locales) {
            //String countryCode = locale.getCountry();
            //String country = locale.getDisplayCountry(locale);
            String countryCode = locale.getCountry();
            //////Log.d(TAG, ">>>>> country & code: " + country + ", " + countryCode);
            if(!TextUtils.isEmpty(countryCode) && !countryNameCodeMap.containsValue(countryCode)) {
                String countryName = locale.getDisplayCountry();
                String countryNameNative = locale.getDisplayCountry(locale);
                String countryNameComposite = countryName + "(" + countryNameNative + ")";
                //countryNameCodeMap.put(countryNameNative, countryCode);
                //countryNameList.add(countryNameNative);
                countryNameCodeMap.put(countryNameComposite, countryCode);
                countryNameList.add(countryNameComposite);
            }
        }
        ////Log.d(TAG, ">>>>> number of country map " + countryNameCodeMap.size());
        ////Log.d(TAG, ">>>>> number of country names: " + countryNameList.size());

        Collections.sort(countryNameList, String.CASE_INSENSITIVE_ORDER);

        Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
        //ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, countryNameList);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(context, R.layout.spinner_custom_item, countryNameList);

        spinner.setAdapter(spinnerAdapter);

        if(!savedCountryName.isEmpty()) {
            int savedCountryPosition = spinnerAdapter.getPosition(savedCountryName);
            spinner.setSelection(savedCountryPosition);
        }


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                //selectedCountryNameNative = adapterView.getItemAtPosition(position).toString();
                //selectedCountryCode = countryNameCodeMap.get(selectedCountryNameNative);
                selectedCountryNameComposite = adapterView.getItemAtPosition(position).toString();
                selectedCountryCode = countryNameCodeMap.get(selectedCountryNameComposite);
                //////Log.d(TAG, ">>>>> selected: " + selectedCountryNameNative + ", " + selectedCountryCode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        return view;
    }

    private SpinnerPreference getSpinnerPreference() {
        return (SpinnerPreference) getPreference();
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        ////Log.d(TAG, ">>>>> onDialogClosed with: positiveResult = " + positiveResult);
        if(positiveResult) {

            SpinnerPreference spinnerPreference = getSpinnerPreference();
            //String countryAndCode = selectedCountryNameNative + ":" +selectedCountryCode;
            String countryAndCode = selectedCountryNameComposite + ":" +selectedCountryCode;
            if(spinnerPreference.callChangeListener(countryAndCode)) {
                //spinnerPreference.setSummary(selectedCountryNameNative);
                spinnerPreference.setText(countryAndCode);
            }

        }

    }
}
