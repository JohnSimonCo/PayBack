package com.johnsimon.payback.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.johnsimon.payback.R;

public class RequestRateDialogFragment extends DialogFragment {

    public RateCallback rateCallback = null;

    private AlertDialog alertDialog;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Build the dialog and set up the button click handlers
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View rootView = inflater.inflate(R.layout.request_rate_dialog, null);

        Button rate_now = (Button) rootView.findViewById(R.id.request_rate_dialog_rate);
        Button rate_later = (Button) rootView.findViewById(R.id.request_rate_dialog_later);
        Button rate_never = (Button) rootView.findViewById(R.id.request_rate_dialog_never);

        rate_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String appPackageName = getActivity().getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
                }

				rateCallback.onNeverAgain();
				alertDialog.cancel();
            }
        });

        rate_later.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rateCallback.onLater();
                alertDialog.cancel();
            }
        });

        rate_never.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rateCallback.onNeverAgain();
                alertDialog.cancel();
            }
        });

        builder.setView(rootView);

        alertDialog = builder.create();

		setCancelable(false);

        return alertDialog;
    }


    public interface RateCallback {
        public void onNeverAgain();
        public void onLater();
    }
}
