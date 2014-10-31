package com.johnsimon.payback.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Typeface;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.johnsimon.payback.view.NDSpinner;
import com.johnsimon.payback.R;
import com.johnsimon.payback.core.Debt;
import com.johnsimon.payback.util.Resource;

public class WelcomeDialogFragment extends DialogFragment implements CustomCurrencyDialogFragment.CustomCurrencySelectedCallback {

	private AlertDialog alertDialog;
	private boolean hasNfc = false;
	private String currency, lastSpinnerValue;
    private boolean listenForSpinnerSelect = false;
	private boolean currencyOnly;

    private final String CURRENCY_SAVE_KEY = "CURRENCY_SAVE_KEY";

	private NDSpinner currencySpinner;
	private TextView welcomeCurrencyPreview;

	@Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View rootView = inflater.inflate(R.layout.welcome_dialog, null);

		currencyOnly = false;
		Bundle args = getArguments();
		if (args != null) {
			currencyOnly = getArguments().getBoolean("SETTINGS", false);
		}

		welcomeCurrencyPreview = (TextView) rootView.findViewById(R.id.welcome_currency_preview);

        final Button welcome_continue = (Button) rootView.findViewById(R.id.welcome_continue);
        welcome_continue.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "robotomedium.ttf"));

        Handler handler = new Handler();
        Runnable r = new Runnable() {
            public void run() {
                listenForSpinnerSelect = true;
            }
        };
        handler.postDelayed(r, 300);

		currencySpinner = (NDSpinner) rootView.findViewById(R.id.welcome_currency_spinner);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
				R.array.currencies, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		currencySpinner.setAdapter(adapter);

		hasNfc = NfcAdapter.getDefaultAdapter(getActivity()) != null;

		welcome_continue.setOnClickListener(clickListener);

		if (!hasNfc || currencyOnly) {
			welcome_continue.setText(R.string.welcome_got_it);
		} else {
			welcome_continue.setText(R.string.welcome_continue);
		}

		final WelcomeDialogFragment self = this;
		currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                if (listenForSpinnerSelect || savedInstanceState == null) {
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

			}
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		if (currencyOnly) {
			RelativeLayout rl = (RelativeLayout) rootView.findViewById(R.id.welcome_header);
			rl.setVisibility(View.GONE);

			TextView welcome_information_text = (TextView) rootView.findViewById(R.id.welcome_information_text);
			welcome_information_text.setText(R.string.welcome_currency);
		}

		if (!currencyOnly) {
			setCancelable(false);
		}

		builder.setView(rootView);

		alertDialog = builder.create();
        return alertDialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            setCurrency(savedInstanceState.getString(CURRENCY_SAVE_KEY));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CURRENCY_SAVE_KEY, currency);
    }

	private void setCurrency(String currency) {
		this.currency = currency;
		welcomeCurrencyPreview.setText(this.currency);
	}

	View.OnClickListener clickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			//Button was disabled when no currency so we're free
			//to continue since the user was able to press the button

			if (!(!hasNfc || currencyOnly)) {
				WelcomeNfcDialogFragment welcomeNfcDialogFragment = new WelcomeNfcDialogFragment();
				welcomeNfcDialogFragment.show(getFragmentManager(), "welcome_nfc");
			}

			Resource.setCurrency(currency);

            int length = FeedFragment.debts.size();
            for (int i = 0; i < length; i++) {
                FeedFragment.debts.get(i).amountAsString = Debt.amountString(FeedFragment.debts.get(i).amount);
            }

            FeedFragment.adapter.notifyDataSetChanged();
            FeedFragment.displayTotalDebt(getActivity());
			if (SettingsActivity.pref_currency != null) {
				SettingsActivity.pref_currency.setSummary(currency);
			}

			alertDialog.cancel();
		}
	};

	@Override
	public void onSelected(String currency) {
		setCurrency(currency);
	}
}