package com.johnsimon.payback.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.johnsimon.payback.R;

public class ConfirmDialogFragment extends DialogFragment {

	public ConfirmCallback confirm = null;
	public final static String INFO_TEXT = "INFO_TEXT_KEY";
    public final static String CONFIRM_TEXT = "CONFIRM_TEXT_KEY";
    public final static String DECLINE_TEXT = "DECLINE_TEXT_KEY";
    public final static String TITLE_TEXT = "CONFIRM_TEXT_KEY";

	private AlertDialog alertDialog;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();

		View rootView = inflater.inflate(R.layout.confirm_delete_dialog, null);

		Button confirm_delete_cancel = (Button) rootView.findViewById(R.id.confirm_delete_cancel);
		Button confirm_delete_confirm = (Button) rootView.findViewById(R.id.confirm_delete_confirm);
        TextView confirm_delete_dialog_text = (TextView) rootView.findViewById(R.id.confirm_delete_dialog_text);
        TextView confirm_delete_dialog_title = (TextView) rootView.findViewById(R.id.confirm_delete_dialog_title);


		confirm_delete_cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.cancel();
			}
		});

		confirm_delete_confirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				confirm.onConfirm();
				alertDialog.cancel();
			}
		});

        String titleText = getArguments().getString(TITLE_TEXT);
        String text = getArguments().getString(INFO_TEXT);
        String confirmText = getArguments().getString(CONFIRM_TEXT);
        String declineText = getArguments().getString(DECLINE_TEXT);

        if (TextUtils.isEmpty(text)) {
            confirm_delete_dialog_text.setVisibility(View.GONE);
        } else {
            confirm_delete_dialog_text.setText(text);
        }

        if (TextUtils.isEmpty(confirmText)) {
            confirm_delete_confirm.setText(R.string.confirm);
        } else {
            confirm_delete_confirm.setText(confirmText);
        }

        if (TextUtils.isEmpty(declineText)) {
            confirm_delete_cancel.setText(R.string.cancel);
        } else {
            confirm_delete_cancel.setText(declineText);
        }

        if (TextUtils.isEmpty(titleText)) {
            confirm_delete_dialog_title.setVisibility(View.GONE);
        } else {
            confirm_delete_dialog_title.setText(titleText);
        }

		builder.setView(rootView);

		alertDialog = builder.create();

		return alertDialog;
	}

	public interface ConfirmCallback {
		public void onConfirm();
	}

}