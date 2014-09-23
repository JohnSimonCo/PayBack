package com.johnsimon.payback;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

public class WelcomeNfcDialogFragment extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();

		View rootView = inflater.inflate(R.layout.welcome_dialog_nfc, null);

		Button welcome_got_it = (Button) rootView.findViewById(R.id.welcome_got_it);
		welcome_got_it.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "robotomedium.ttf"));

		builder.setView(rootView);

		return builder.create();
	}

}