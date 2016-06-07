package com.owlab.callblocker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.owlab.callblocker.content.CallBlockerContentProvider;
import com.owlab.callblocker.content.CallBlockerTbl;

/**
 * Created by ernest on 5/15/16.
 */
public class ChangeDescriptionDialogFragment extends DialogFragment {
    private static final String TAG = ChangeDescriptionDialogFragment.class.getSimpleName();

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

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.ChangeDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        //final EditTextAddNumberDialogFragment input = new EditText(getActivity());
        Bundle arguments = getArguments();
        final int _id = arguments.getInt("_id");
        final String phoneNumber = arguments.getString("phoneNumber");
        final String description = arguments.getString("description");

        View diagView = inflater.inflate(R.layout.change_description_dialog_layout, null);
        //TextView phoneNumberTextView = (TextView) diagView.findViewById(R.id.updateDialog_textView_phoneNumber);
        final EditText descriptionText = (EditText) diagView.findViewById(R.id.change_description_dialog_description);

        //phoneNumberTextView.setText(phoneNumber);
        //phoneNumberTextView.setEnabled(false);

        // To send cursor to the end
        descriptionText.setText("");
        descriptionText.append(description);

        updateDescriptionDialog = builder
                .setView(diagView)
                //.setParentView(input)
                .setTitle("Change description")
                .setMessage(Utils.formatPhoneNumber(phoneNumber))
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //mUpdateDescriptionDialogListener.onUpdateDescriptionUpdateClick(_id, descriptionText.getText().toString());
                        dialog.dismiss();
                        ContentValues values = new ContentValues();
                        values.put(CallBlockerTbl.Schema.COLUMN_NAME_DESCRIPTION, descriptionText.getText().toString());
                        int updateCount = getTargetFragment().getActivity().getContentResolver().update(CallBlockerContentProvider.CONTENT_URI, values, CallBlockerTbl.Schema._ID + "=" + _id, null);
                        if(updateCount > 0) {
                            Snackbar.make(getTargetFragment().getView(), "Description changed", Snackbar.LENGTH_SHORT).show();
                            getTargetFragment().getActivity().getContentResolver().notifyChange(CallBlockerContentProvider.CONTENT_URI, null);
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
