package com.owlab.callblocker.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import com.owlab.callblocker.CONS;
import com.owlab.callblocker.R;
import com.owlab.callblocker.contentprovider.CallQuieterDb;
import com.owlab.callblocker.util.Utils;
import com.owlab.callblocker.contentprovider.CallQuieterContentProvider;

/**
 * Created by ernest on 5/15/16.
 */
public class DeleteDialogFragment extends DialogFragment {
    public static final String TAG = DeleteDialogFragment.class.getSimpleName();

    AlertDialog deleteFilteredItemDialog;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Log.d(TAG, ">>> target fragment: " + getTargetFragment());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        //final EditTextAddNumberDialogFragment input = new EditText(getActivity());
        Bundle arguments = getArguments();
        final int _id = arguments.getInt(CONS.ARG_KEY_REGISTERED_NUMBER_ID);
        final String phoneNumber = Utils.formatPhoneNumber(getContext(), arguments.getString(CONS.ARG_KEY_REGISTERED_NUMBER));
        final String displayName = arguments.getString(CONS.ARG_KEY_DISPLAY_NAME);

        View diagView = inflater.inflate(R.layout.delete_dialog_layout, null);
        //TextView phoneNumberTextView = (TextView) diagView.findViewById(R.id.deleteDialog_textView_phoneNumber);
        //TextView descriptionTextView = (TextView) diagView.findViewById(R.id.deleteDialog_textView_description);

        //phoneNumberTextView.setText(Utils.formatPhoneNumber(phoneNumber));
        //phoneNumberTextView.setEnabled(false);
        //descriptionTextView.setText(description);
        //descriptionTextView.setEnabled(false);

        deleteFilteredItemDialog = builder
                //.setView(diagView)
                //.setParentView(input)
                //.setIcon(R.drawable.ic_warning_48)
                .setTitle("Confirm")
                .setMessage(phoneNumber + (displayName.isEmpty() ? "" : "(" + displayName + ")") + " will be deleted")
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //mDeleteItemDialogListener.onDeleteItemConirmClick(_id);
                        dialog.dismiss();
                        //TODO not need uri encoding of _id?
                        //Uri deleteUri = ContentUris.withAppendedId(CallQuieterContentProvider.REGISTERED_NUMBER_URI, _id);
                        //int deleteCount = getTargetFragment().getActivity().getContentResolver().delete(deleteUri, null, null);
                        final ContentValues values = new ContentValues();
                        values.put(CallQuieterDb.COLS_REGISTERED_NUMBER.MARK_DELETED, 1);
                        int updateCount = getTargetFragment().getActivity().getContentResolver().update(CallQuieterContentProvider.REGISTERED_NUMBER_URI, values, CallQuieterDb.COLS_REGISTERED_NUMBER._ID + " = " + _id, null);
                        values.clear();

                        if (updateCount > 0) {
                            Snackbar snackbar = Snackbar.make(getTargetFragment().getView(), phoneNumber + " is deleted", Snackbar.LENGTH_LONG);
                            snackbar.setCallback(new Snackbar.Callback() {
                                @Override
                                public void onDismissed(Snackbar snackbar, int event) {
                                    super.onDismissed(snackbar, event);
                                    if(event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                                        int deleteCount = getTargetFragment().getActivity().getContentResolver().delete(CallQuieterContentProvider.REGISTERED_NUMBER_URI, CallQuieterDb.COLS_REGISTERED_NUMBER.MARK_DELETED + " > 0", null);
                                        if(deleteCount > 0) {
                                        }

                                    }
                                }
                            });
                            snackbar.setAction("Undo", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    values.put(CallQuieterDb.COLS_REGISTERED_NUMBER.MARK_DELETED, 0);
                                    int updateCount = getTargetFragment().getActivity().getContentResolver().update(CallQuieterContentProvider.REGISTERED_NUMBER_URI, values, CallQuieterDb.COLS_REGISTERED_NUMBER._ID + " = " + _id, null);
                                    if(updateCount > 0) {
                                    }
                                }
                            });
                            snackbar.show();
                        }
                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //mDeleteItemDialogListener.onDeleteItemCancelClick();
                        dialog.dismiss();
                    }
                }).create();

        return deleteFilteredItemDialog;
    }
}
