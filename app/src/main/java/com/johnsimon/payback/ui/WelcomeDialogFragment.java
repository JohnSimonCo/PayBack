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
import java.util.Locale;
import java.util.Set;

public class WelcomeDialogFragment extends DataDialogFragment {

	private AlertDialog alertDialog;
	private boolean hasNfc = false;
	private String currency;
	private boolean currencyOnly;
	private boolean currencyBefore = true;

    private final String CURRENCY_SAVE_KEY = "CURRENCY_SAVE_KEY";
    private final String CURRENCY_BEFORE_SAVE_KEY = "CURRENCY_BEFORE_SAVE_KEY";

	private RobotoButton welcome_select_currency;
	private RobotoButton welcome_select_currency_display;

	private TintRadioButton custom_currency_radio_before;
	private TintRadioButton custom_currency_radio_after;

	private RobotoTextView welcome_currency_preview;

	private String displayCurrency;
	private Currency selectedCurrency;


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

        final Button welcome_continue = (Button) rootView.findViewById(R.id.welcome_continue);

		hasNfc = NfcAdapter.getDefaultAdapter(getActivity()) != null;

		welcome_continue.setOnClickListener(clickListener);

		if (!hasNfc) {
			welcome_continue.setText(R.string.welcome_got_it);
		} else {
			welcome_continue.setText(R.string.welcome_continue);
		}

        if (currencyOnly) {
            welcome_continue.setText(R.string.done);
        }

		if (currencyOnly) {
			RelativeLayout rl = (RelativeLayout) rootView.findViewById(R.id.welcome_header);
			rl.setVisibility(View.GONE);

			TextView welcome_information_text = (TextView) rootView.findViewById(R.id.welcome_information_text);
			welcome_information_text.setText(R.string.welcome_currency);
		}

		if (currencyOnly) {
			setCancelable(true);
		} else {
            setCancelable(false);
        }

		welcome_select_currency = (RobotoButton) rootView.findViewById(R.id.welcome_select_currency);
		welcome_select_currency_display = (RobotoButton) rootView.findViewById(R.id.welcome_select_currency_display);

		custom_currency_radio_before = (TintRadioButton) rootView.findViewById(R.id.custom_currency_radio_before);
		custom_currency_radio_after = (TintRadioButton) rootView.findViewById(R.id.custom_currency_radio_after);

		welcome_currency_preview = (RobotoTextView) rootView.findViewById(R.id.welcome_currency_preview);

		Set<Currency> currencySet = getAllCurrencies();

		final Currency[] currencyList = currencySet.toArray(new Currency[currencySet.size()]);
		final String[] currencyNameList = new String[currencyList.length];
		for (int i = 0; i < currencyList.length; i++) {
			currencyNameList[i] = currencyList[i].getSymbol();
		}

		Arrays.sort(currencyList, new Resource.AlphabeticalCurrencyComparator());
		Arrays.sort(currencyNameList, new Resource.AlphabeticalStringComparator());

		welcome_select_currency.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new MaterialDialog.Builder(getActivity())
						.title(R.string.select_currency)
						.items(currencyNameList)
						.itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallback() {
							@Override
							public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
								selectedCurrency = currencyList[which];
								displayCurrency = text.toString();
								updatePreview();
							}
						})
						.positiveText(R.string.select)
						.show();
			}
		});

		welcome_select_currency_display.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CustomCurrencyDialogFragment fragment = new CustomCurrencyDialogFragment();
				fragment.completeCallback = new CustomCurrencyDialogFragment.CustomCurrencySelectedCallback() {
					@Override
					public void onSelected(String currency) {
						displayCurrency = currency;
						updatePreview();
					}
				};

				fragment.show(getFragmentManager(), "custom_currency");
			}
		});

		builder.setView(rootView);

		alertDialog = builder.create();
        return alertDialog;
    }

	private void updatePreview() {

	}

	private static Set<Currency> getAllCurrencies()
	{
		Set<Currency> toret = new HashSet<>();
		Locale[] locs = Locale.getAvailableLocales();

		for(Locale loc : locs) {
			try {
				toret.add( Currency.getInstance( loc ) );
			} catch(Exception exc)
			{
				// Locale not found
			}
		}

		return toret;
	}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            currencyBefore = savedInstanceState.getBoolean(CURRENCY_BEFORE_SAVE_KEY, true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CURRENCY_SAVE_KEY, currency);
        outState.putBoolean(CURRENCY_BEFORE_SAVE_KEY, currencyBefore);
    }

	View.OnClickListener clickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			//Button was disabled when no currencyId so we're free
			//to continue since the user was able to press the button

			if (hasNfc && !currencyOnly) {
				WelcomeNfcDialogFragment welcomeNfcDialogFragment = new WelcomeNfcDialogFragment();
				welcomeNfcDialogFragment.show(getFragmentManager(), "welcome_nfc");
			}

			data.preferences.set("currency", new UserCurrency(selectedCurrency.getSymbol(), displayCurrency, custom_currency_radio_before.isChecked()));
			storage.commit();

            FeedFragment.adapter.notifyDataSetChanged();

			/*if (getActivity() != null && getResources() != null) {
				FeedFragment.displayTotalDebt(getResources());
			}*/

			NavigationDrawerFragment.updateBalance(data);
			if (SettingsActivity.pref_currency != null) {
				SettingsActivity.pref_currency.setSummary(currency);
			}

			alertDialog.dismiss();
		}
	};
}