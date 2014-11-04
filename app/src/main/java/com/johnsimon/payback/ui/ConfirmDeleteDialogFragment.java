package com.johnsimon.payback.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.johnsimon.payback.R;
import com.johnsimon.payback.util.RobotoMediumTextView;

public class ConfirmDeleteDialogFragment extends DialogFragment {

	//public ConfirmDeleteCallback confirmDelete = null;
	public final static String DELTE_TEXT = "DELETE_TEXT_KEY";

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();

		View rootView = inflater.inflate(R.layout.confirm_delete_dialog, null);

		RobotoMediumTextView confirm_delete_dialog_text = (RobotoMediumTextView) rootView.findViewById(R.id.confirm_delete_dialog_text);

		String text = getArguments().getString(DELTE_TEXT);
		if (TextUtils.isEmpty(text)) {
			confirm_delete_dialog_text.setVisibility(View.GONE);
		} else {
			confirm_delete_dialog_text.setText(text);
		}



		builder.setView(rootView);

		final AlertDialog ad = builder.create();

		return ad;
	}

}