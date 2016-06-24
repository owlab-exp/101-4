package com.owlab.callblocker.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.owlab.callblocker.R;
import com.owlab.callblocker.contentprovider.CallBlockerProvider;
import com.owlab.callblocker.contentprovider.CallBlockerDb;

/**
 * Created by ernest on 5/15/16.
 */
public class AddByManualDialogFragment extends DialogFragment {
    public static final String TAG = AddByManualDialogFragment.class.getSimpleName();

    //public interface AddItemDialogListener {
    //    public void onAddItemDialogAddClick(DialogFragment dialog);
    //    public void onAddItemDialogCancelClick(DialogFragment dialog);
    //}

    //AddItemDialogListener mAddItemDialogListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        //try {
        //    mAddItemDialogListener = (AddItemDialogListener) getTargetFragment();
        //} catch(ClassCastException e) {
        //    throw new ClassCastException(activity.toString() + " must implement AddItemDialogListener");
        //}
    }

    //AlertDialog addFilteredItemDialog;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Log.d(TAG, ">>> target fragment: " + getTargetFragment() );

        //AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AddDialog);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        //final EditText input = new EditText(getActivity());
        View diagView = inflater.inflate(R.layout.add_by_manual_dialog_layout, null);
        AlertDialog addPhoneNumberDialog = builder
                .setView(diagView)
                //.setParentView(input)
                .setTitle("Add new phone number")
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogI, int id) {
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //AddItemDialogFragment.this.getDialog().cancel();
                        //mAddItemDialogListener.onAddItemDialogCancelClick(AddItemDialogFragment.this);
                        dialog.dismiss();
                    }

                }).create();

        addPhoneNumberDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                final AlertDialog alertDialog = (AlertDialog)dialog;
                EditText phoneNumberEditText = (EditText) alertDialog.findViewById(R.id.add_phone_dialog_phone_number);
                phoneNumberEditText.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Log.d(TAG, ">>> clicked");
                        EditText phoneNumberText = (EditText)alertDialog.findViewById(R.id.add_phone_dialog_phone_number);
                        EditText displayNameET = (EditText)alertDialog.findViewById(R.id.add_phone_dialog_description);
                        if(phoneNumberText.getText().toString().trim().isEmpty()) {
                            Snackbar.make(getView(), "Empty phone number", Snackbar.LENGTH_SHORT).show();
                        } else {
                            alertDialog.dismiss();
                            //mAddItemDialogListener.onAddItemDialogAddClick(AddItemDialogFragment.this);
                            ContentValues values = new ContentValues();
                            String compactPhoneNumber = phoneNumberText.getText().toString().replaceAll("[^\\d]", "");
                            values.put(CallBlockerDb.COLS_BLOCKED_NUMBER.PHONE_NUMBER, compactPhoneNumber);
                            values.put(CallBlockerDb.COLS_BLOCKED_NUMBER.DISPLAY_NAME, displayNameET.getText().toString());
                            Uri newUri = getTargetFragment().getActivity().getContentResolver().insert(CallBlockerProvider.BLOCKED_NUMBER_URI, values);
                            Log.d(TAG, ">>> newUri: " + newUri.toString());
                            Log.d(TAG, ">>> newUri.getLastPathSegment: " + newUri.getLastPathSegment());
                            if(Long.parseLong(newUri.getLastPathSegment()) > 0)
                                Snackbar.make(getTargetFragment().getView(), compactPhoneNumber + " added", Snackbar.LENGTH_SHORT).show();
                            else
                                Snackbar.make(getTargetFragment().getView(), "Add failed, duplicate?", Snackbar.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        });

        return addPhoneNumberDialog;
    }
}
