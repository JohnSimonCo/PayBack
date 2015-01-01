package com.johnsimon.payback.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.johnsimon.payback.R;

public class UpgradeDialogFragment extends DialogFragment {

    private AlertDialog alertDialog;
    public static BillingProcessor bp;

    public static UpgradeDialogFragment create(BillingProcessor bp) {
        UpgradeDialogFragment.bp = bp;
        return new UpgradeDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View rootView = inflater.inflate(R.layout.upgrade_dialog, null);

        Button cancelButton = (Button) rootView.findViewById(R.id.upgrade_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });


        Button upgradeButton = (Button) rootView.findViewById(R.id.upgrade_button);
        upgradeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bp.purchase(getActivity(), "full_version");
                alertDialog.cancel();
            }
        });

        builder.setView(rootView);
        alertDialog = builder.create();

        return alertDialog;
    }

}