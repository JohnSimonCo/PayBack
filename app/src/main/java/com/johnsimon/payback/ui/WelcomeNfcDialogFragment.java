package com.johnsimon.payback.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.johnsimon.payback.R;

public class WelcomeNfcDialogFragment extends DialogFragment {

	private AlertDialog alertDialog;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();

		View rootView = inflater.inflate(R.layout.welcome_dialog_nfc, null);

		Button welcome_got_it = (Button) rootView.findViewById(R.id.welcome_got_it);

		welcome_got_it.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.dismiss();
			}
		});

		builder.setView(rootView);

		alertDialog = builder.create();

		return alertDialog;
	}

}