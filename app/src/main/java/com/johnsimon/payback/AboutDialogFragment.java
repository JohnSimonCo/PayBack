package com.johnsimon.payback;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class AboutDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Build the dialog and set up the button click handlers
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View rootView = inflater.inflate(R.layout.about_dialog_fragment, null);

		TextView versionText = (TextView) rootView.findViewById(R.id.about_dialog_version);
		try {
			String version = getActivity().getPackageManager()
					.getPackageInfo(getActivity().getPackageName(), 0).versionName;
			versionText.setText("v" + version);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			versionText.setText("");
		};


        builder.setView(rootView);

        final AlertDialog ad = builder.create();

        return ad;
    }

}