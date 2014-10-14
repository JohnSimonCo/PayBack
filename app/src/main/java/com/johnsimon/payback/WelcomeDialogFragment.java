package com.johnsimon.payback;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Typeface;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class WelcomeDialogFragment extends DialogFragment implements CustomCurrencyDialogFragment.CustomCurrencySelectedCallback {

	private AlertDialog alertDialog;
	private boolean hasNfc = false;
	private String currency, lastSpinnerValue;

	private NDSpinner currencySpinner;
	private TextView welcomeCirrencyPreview;

	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View rootView = inflater.inflate(R.layout.welcome_dialog, null);

		welcomeCirrencyPreview = (TextView) rootView.findViewById(R.id.welcome_currency_preview);

        final Button welcome_continue = (Button) rootView.findViewById(R.id.welcome_continue);
        welcome_continue.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "robotomedium.ttf"));

		currencySpinner = (NDSpinner) rootView.findViewById(R.id.welcome_currency_spinner);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
				R.array.currencies, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		currencySpinner.setAdapter(adapter);

		hasNfc = NfcAdapter.getDefaultAdapter(getActivity()) != null;

		welcome_continue.setOnClickListener(clickListener);

		if (hasNfc) {
			welcome_continue.setText(R.string.welcome_continue);
		} else {
			welcome_continue.setText(R.string.welcome_got_it);
		}
		final WelcomeDialogFragment self = this;
		currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				if (pos == parent.getCount() - 1) {
					// "Custom"
					CustomCurrencyDialogFragment fragment = new CustomCurrencyDialogFragment();
					fragment.completeCallback = self;
					fragment.show(getFragmentManager(), "CustomCurrencyDialogFragment");
				} else {
					Object item = parent.getItemAtPosition(pos);
					lastSpinnerValue = item.toString();
					setCurrency(lastSpinnerValue);
				}
			}
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		builder.setView(rootView);

		alertDialog = builder.create();
		alertDialog.setCancelable(false);
        return alertDialog;
    }

	private void setCurrency(String currency) {
		this.currency = currency;
		welcomeCirrencyPreview.setText(this.currency);
	}

	View.OnClickListener clickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			//Button was disabled when no currency so we're free
			//to continue since the user was able to press the button
			if (hasNfc) {
				WelcomeNfcDialogFragment welcomeNfcDialogFragment = new WelcomeNfcDialogFragment();
				welcomeNfcDialogFragment.show(getFragmentManager(), "welcome_nfc");
			}

			Resource.setCurrency(currency);

			alertDialog.cancel();
		}
	};

	@Override
	public void onSelected(String currency) {
		setCurrency(currency);
	}
}