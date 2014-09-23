package com.johnsimon.payback;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;

public class PaidBackDialogFragment extends DialogFragment {

	public final static int PAY_BACK = 0;
	public final static int UNDO_PAY_BACK = 1;

	private static boolean payBack = false;

	public static PaidBackDialogFragment newInstance(int flag) {
		payBack = flag == PAY_BACK;
		return new PaidBackDialogFragment();
	}

	public CompleteCallback completeCallback;

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

		View rootView;
		if (payBack) {
			rootView = inflater.inflate(R.layout.paid_back_dialog, null);
		} else {
			rootView = inflater.inflate(R.layout.paid_back_dialog_reverse, null);
		}

		builder.setView(rootView);

		final AlertDialog alertDialog = builder.create();

		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				completeCallback.onComplete();
				alertDialog.cancel();
			}
		}, 1000);


		return alertDialog;
	}

	public interface CompleteCallback {
		public void onComplete();
	}

}
