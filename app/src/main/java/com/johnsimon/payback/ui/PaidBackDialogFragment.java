package com.johnsimon.payback.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.johnsimon.payback.R;
import com.johnsimon.payback.data.Debt;

public class PaidBackDialogFragment extends DialogFragment {

	public final static int PAY_BACK = 0;
	public final static int UNDO_PAY_BACK = 1;

	private static boolean payBack = false;
	private static Debt debt;

	public static PaidBackDialogFragment newInstance(int flag, Debt _debt) {
		payBack = flag == PAY_BACK;
		debt = _debt;
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

			final ImageView image = (ImageView) rootView.findViewById(R.id.paid_back_dialog_image);
			image.setBackgroundResource(R.anim.checkbox_animation);
			image.post(new Runnable() {
				@Override
				public void run() {
					AnimationDrawable frameAnimation = (AnimationDrawable) image.getBackground();
					frameAnimation.start();
				}
			});
		} else {
			rootView = inflater.inflate(R.layout.paid_back_dialog_reverse, null);

			final ImageView image = (ImageView) rootView.findViewById(R.id.paid_back_dialog_image_reverse);
			image.setBackgroundResource(R.anim.checkbox_animation_reverse);
			image.post(new Runnable() {
				@Override
				public void run() {
					AnimationDrawable frameAnimation = (AnimationDrawable) image.getBackground();
					frameAnimation.start();
				}
			});
		}

		builder.setView(rootView);

		final AlertDialog alertDialog = builder.create();

		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				completeCallback.onComplete(debt);
				alertDialog.dismiss();
			}
		}, 1000);


		return alertDialog;
	}

	public interface CompleteCallback {
		public void onComplete(Debt debt);
	}

}
