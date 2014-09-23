package com.johnsimon.payback;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

public class DebtDetailDialogFragment extends DialogFragment implements PaidBackDialogFragment.CompleteCallback {

	public static Debt debt;
	public PaidBackCallback paidBackCallback = null;
	public AlertDialog alertDialog;

	public static DebtDetailDialogFragment newInstance(Debt debt) {
		DebtDetailDialogFragment.debt = debt;
        return new DebtDetailDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Build the dialog and set up the button click handlers
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View rootView = inflater.inflate(R.layout.detail_dialog, null);

        Button dialog_custom_confirm = (Button) rootView.findViewById(R.id.dialog_custom_confirm);
        Button dialog_custom_cancel = (Button) rootView.findViewById(R.id.dialog_custom_cancel);

        dialog_custom_confirm.setTypeface(FontCache.get(getActivity(), "robotomedium.ttf"));
        dialog_custom_cancel.setTypeface(FontCache.get(getActivity(), "robotomedium.ttf"));

        //This is the share button
        dialog_custom_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "BITCH YOU OWE ME MONEY");
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getResources().getString(R.string.share)));
            }
        });

		final DebtDetailDialogFragment self = this;
		dialog_custom_confirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				debt.isPaidBack = !debt.isPaidBack;

				PaidBackDialogFragment paidBackDialogFragment = new PaidBackDialogFragment();
				paidBackDialogFragment.show(getFragmentManager().beginTransaction(), "paid_back_dialog");
				paidBackDialogFragment.completeCallback = self;

				alertDialog.cancel();
			}
		});

        RobotoMediumTextView dialog_custom_title = (RobotoMediumTextView) rootView.findViewById(R.id.dialog_custom_title);
        RobotoMediumTextView dialog_custom_content = (RobotoMediumTextView) rootView.findViewById(R.id.dialog_custom_content);

        dialog_custom_title.setText(debt.owner.name);
        dialog_custom_content.setText(debt.note);


        builder.setView(rootView);

		alertDialog = builder.create();
        return alertDialog;
    }

	@Override
	public void onComplete() {
		if(paidBackCallback != null) {
			paidBackCallback.onPaidBack();
		}
	}

	public interface PaidBackCallback {
		public void onPaidBack();
	}
}