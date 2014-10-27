package com.johnsimon.payback;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;

public class DebtDetailDialogFragment extends DialogFragment implements PaidBackDialogFragment.CompleteCallback {

	public static Debt debt;
	public PaidBackCallback paidBackCallback = null;
	public EditCallback editCallback = null;
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

		if (debt.isPaidBack) {
			dialog_custom_confirm.setText(R.string.undo_pay_back);
			dialog_custom_confirm.setTextColor(getResources().getColor(R.color.red));
		}

        //This is the share button
        dialog_custom_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, debt.getShareString(getActivity()));
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getResources().getString(R.string.share)));
            }
        });

		final DebtDetailDialogFragment self = this;
		dialog_custom_confirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				PaidBackDialogFragment paidBackDialogFragment;

				if (debt.isPaidBack) {
					paidBackDialogFragment = PaidBackDialogFragment.newInstance(PaidBackDialogFragment.UNDO_PAY_BACK);
				} else {
					paidBackDialogFragment = PaidBackDialogFragment.newInstance(PaidBackDialogFragment.PAY_BACK);
				}
				paidBackDialogFragment.show(getFragmentManager().beginTransaction(), "paid_back_dialog");
				paidBackDialogFragment.completeCallback = self;

				alertDialog.cancel();
			}
		});

		RobotoMediumTextView dialog_custom_amount = (RobotoMediumTextView) rootView.findViewById(R.id.dialog_custom_amount);
		if (debt.amount < 0) {
			//negative
			dialog_custom_amount.setText(debt.amountAsString);
			dialog_custom_amount.setTextColor(getResources().getColor(R.color.red));
		} else {
			dialog_custom_amount.setText(debt.amountAsString);
			dialog_custom_amount.setTextColor(getResources().getColor(R.color.green_strong));
		}


        RobotoMediumTextView dialog_custom_title = (RobotoMediumTextView) rootView.findViewById(R.id.dialog_custom_title);
        RobotoMediumTextView dialog_custom_content = (RobotoMediumTextView) rootView.findViewById(R.id.dialog_custom_content);

        dialog_custom_title.setText(debt.owner.name);

		if (debt.note == null) {
			dialog_custom_content.setText(R.string.cash);
		} else {
			dialog_custom_content.setText(debt.note);
		}

		ImageButton detailDialogOverflow = (ImageButton) rootView.findViewById(R.id.detail_dialog_overflow);
		detailDialogOverflow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PopupMenu popupMenu = new PopupMenu(getActivity(), v);
				popupMenu.inflate(R.menu.detail_dialog_popup);
				popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {

						switch (item.getItemId()) {
							case R.id.detail_dialog_edit:
								if(editCallback != null) {
									editCallback.onEdit(debt);
								}
								alertDialog.cancel();

								return true;
							case R.id.detail_dialog_delete:
								if(editCallback != null) {
									editCallback.onDelete(debt);
								}
								alertDialog.cancel();

								return true;
							default:
								return false;
						}
					}
				});
				popupMenu.show();
			}
		});

        builder.setView(rootView);

		alertDialog = builder.create();
        return alertDialog;
    }

	@Override
	public void onComplete() {
		if(paidBackCallback != null) {
			paidBackCallback.onPaidBack(debt);
		}
	}

	public interface PaidBackCallback {
		public void onPaidBack(Debt debt);
	}

	public interface EditCallback {
		public void onDelete(Debt debt);
		public void onEdit(Debt debt);
	}
}