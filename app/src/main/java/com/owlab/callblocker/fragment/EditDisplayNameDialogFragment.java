package com.owlab.callblocker.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.owlab.callblocker.CONS;
import com.owlab.callblocker.R;
import com.owlab.callblocker.contentprovider.CallQuieterDb;
import com.owlab.callblocker.contentprovider.CallQuieterContentProvider;

/**
 * Created by ernest on 5/15/16.
 */
public class EditDisplayNameDialogFragment extends DialogFragment {
    public static final String TAG = EditDisplayNameDialogFragment.class.getSimpleName();

    //public interface UpdateDescriptionDialogListener {
    //    public void onUpdateDescriptionCancelClick();
    //    public void onUpdateDescriptionUpdateClick(int _id, String description);
    //}

    //UpdateDescriptionDialogListener mUpdateDescriptionDialogListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //try {
        //    mUpdateDescriptionDialogListener = (UpdateDescriptionDialogListener) getTargetFragment();
        //} catch (ClassCastException e) {
        //    throw new ClassCastException(activity.toString() + " must implement UpdateDescriptionDialogListener");
        //}
    }

    AlertDialog updateDescriptionDialog;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG, ">>> target fragment: " + getTargetFragment());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        //final EditTextAddNumberDialogFragment input = new EditText(getActivity());
        Bundle arguments = getArguments();
        final int _id = arguments.getInt(CONS.ARG_KEY_REGISTERED_NUMBER_ID);
        final String phoneNumber = arguments.getString(CONS.ARG_KEY_REGISTERED_NUMBER);
        final String displayName = arguments.getString(CONS.ARG_KEY_DISPLAY_NAME);

        View diagView = inflater.inflate(R.layout.edit_display_name_dialog_layout, null);
        //TextView phoneNumberTextView = (TextView) diagView.findViewById(R.id.updateDialog_textView_phoneNumber);
        final EditText displayNameET = (EditText) diagView.findViewById(R.id.edit_description_dialog_description);

        //phoneNumberTextView.setText(phoneNumber);
        //phoneNumberTextView.setEnabled(false);

        // To send cursor to the end
        displayNameET.setText("");
        displayNameET.append(displayName);

        updateDescriptionDialog = builder
                .setView(diagView)
                //.setParentView(input)
                //.setIcon(R.drawable.ic_edit_48)
                .setTitle("Edit Display Name")
                //.setMessage(Utils.formatPhoneNumber(phoneNumber))
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //mUpdateDescriptionDialogListener.onUpdateDescriptionUpdateClick(_id, displayNameET.getText().toString());
                        dialog.dismiss();
                        ContentValues values = new ContentValues();
                        values.put(CallQuieterDb.COLS_REGISTERED_NUMBER.DISPLAY_NAME, displayNameET.getText().toString());
                        int updateCount = getTargetFragment().getActivity().getContentResolver().update(CallQuieterContentProvider.REGISTERED_NUMBER_URI, values, CallQuieterDb.COLS_REGISTERED_NUMBER._ID + "=" + _id, null);
                        if(updateCount > 0) {
                            Snackbar.make(getTargetFragment().getView(), "Display name changed", Snackbar.LENGTH_SHORT).show();
                            getTargetFragment().getActivity().getContentResolver().notifyChange(CallQuieterContentProvider.REGISTERED_NUMBER_URI, null);
                        }
                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //mUpdateDescriptionDialogListener.onUpdateDescriptionCancelClick();
                        dialog.dismiss();
                    }
                }).create();

        return updateDescriptionDialog;
    }
}
