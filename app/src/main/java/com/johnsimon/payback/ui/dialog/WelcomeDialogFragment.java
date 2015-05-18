package com.johnsimon.payback.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.johnsimon.payback.R;
import com.johnsimon.payback.core.DataDialogFragment;
import com.johnsimon.payback.ui.FeedActivity;
import com.johnsimon.payback.ui.dialog.CurrencyDialogFragment;

public class WelcomeDialogFragment extends DataDialogFragment {

	private AlertDialog alertDialog;

	@Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View rootView = inflater.inflate(R.layout.welcome_dialog, null);

        final Button welcome_continue = (Button) rootView.findViewById(R.id.welcome_continue);
		welcome_continue.setOnClickListener(clickListener);

		setCancelable(false);

		builder.setView(rootView);

		alertDialog = builder.create();
        return alertDialog;
    }

	View.OnClickListener clickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {

			CurrencyDialogFragment fragment = new CurrencyDialogFragment();
			Bundle args = new Bundle();
			args.putBoolean(CurrencyDialogFragment.CONTINUE, true);
			args.putBoolean(CurrencyDialogFragment.SHOW_INFO_TEXT, true);
			args.putBoolean(CurrencyDialogFragment.CANCELABLE, false);

            fragment.currencySelectedCallback = (FeedActivity) getActivity();

			fragment.setArguments(args);
			fragment.show(getFragmentManager(), "currenct_shit");

			alertDialog.dismiss();
		}
	};
}