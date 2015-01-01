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
import com.johnsimon.payback.util.FontCache;
import com.johnsimon.payback.util.RobotoMediumTextView;

public class ConfirmDialogFragment extends DialogFragment {

	public ConfirmCallback confirm = null;
	public final static String INFO_TEXT = "INFO_TEXT_KEY";
	public final static String CONFIRM_TEXT = "CONFIRM_TEXT_KEY";

	private AlertDialog alertDialog;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();

		View rootView = inflater.inflate(R.layout.confirm_delete_dialog, null);

		Button confirm_delete_cancel = (Button) rootView.findViewById(R.id.confirm_delete_cancel);
		Button confirm_delete_confirm = (Button) rootView.findViewById(R.id.confirm_delete_confirm);

		confirm_delete_cancel.setTypeface(FontCache.get(getActivity(), FontCache.RobotoMedium));
		confirm_delete_confirm.setTypeface(FontCache.get(getActivity(), FontCache.RobotoMedium));

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

		RobotoMediumTextView confirm_delete_dialog_text = (RobotoMediumTextView) rootView.findViewById(R.id.confirm_delete_dialog_text);
		confirm_delete_dialog_text.setTypeface(FontCache.get(getActivity(), FontCache.RobotoMedium));

		String text = getArguments().getString(INFO_TEXT);
		if (TextUtils.isEmpty(text)) {
			confirm_delete_dialog_text.setVisibility(View.GONE);
		} else {
			confirm_delete_dialog_text.setText(text);
		}

		String confirmText = getArguments().getString(CONFIRM_TEXT);
		if (TextUtils.isEmpty(confirmText)) {
			confirm_delete_confirm.setText(R.string.confirm);
		} else {
			confirm_delete_confirm.setText(confirmText);
		}

		builder.setView(rootView);

		alertDialog = builder.create();

		return alertDialog;
	}

	public interface ConfirmCallback {
		public void onConfirm();
	}

}