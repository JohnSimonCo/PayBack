package com.johnsimon.payback.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.internal.widget.TintRadioButton;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.devspark.robototextview.widget.RobotoButton;
import com.devspark.robototextview.widget.RobotoTextView;
import com.johnsimon.payback.R;
import com.johnsimon.payback.core.UserCurrency;
import com.johnsimon.payback.core.DataDialogFragment;
import com.johnsimon.payback.util.Resource;
import com.johnsimon.payback.view.NDSpinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

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
			args.putBoolean(CurrencyDialogFragment.CONTINUE_TO_NFC, true);
			fragment.show(getFragmentManager(), "currenct_shit");

			alertDialog.dismiss();
		}
	};
}