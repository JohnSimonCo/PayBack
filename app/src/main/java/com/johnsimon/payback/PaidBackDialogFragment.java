package com.johnsimon.payback;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

public class PaidBackDialogFragment extends DialogFragment {

	public static int amount;

	public static PaidBackDialogFragment newInstance(int _amount) {
		amount = _amount;
		return new PaidBackDialogFragment();
	}

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() == null) {
            return;
        }

        getDialog().getWindow().setWindowAnimations(R.style.paid_back_anim);
    }

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Build the dialog and set up the button click handlers
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();

		View rootView = inflater.inflate(R.layout.paid_back_dialog, null);

		builder.setView(rootView);

		final AlertDialog ad = builder.create();

		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				ad.cancel();
			}
		}, 1000);


		return ad;
	}

}
