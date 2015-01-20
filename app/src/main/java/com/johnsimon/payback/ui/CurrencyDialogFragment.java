package com.johnsimon.payback.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v7.internal.widget.TintCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.devspark.robototextview.widget.RobotoButton;
import com.devspark.robototextview.widget.RobotoTextView;
import com.johnsimon.payback.R;
import com.johnsimon.payback.core.DataDialogFragment;
import com.johnsimon.payback.core.UserCurrency;
import com.johnsimon.payback.util.Resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

public class CurrencyDialogFragment extends DataDialogFragment {

	public final static String CONTINUE_TO_NFC = "CONTINUE_TO_NFC";

	private AlertDialog alertDialog;

	private final String CURRENCY_SAVE_KEY = "CURRENCY_SAVE_KEY";
	private final String CURRENCY_DISPLAY_SAVE_KEY = "CURRENCY_BEFORE_SAVE_KEY";

	private RobotoButton welcome_select_currency;
	private RobotoButton welcome_select_currency_display;

	private TintCheckBox custom_currency_check_after;

	private RobotoTextView welcome_currency_preview;

	private String displayCurrency = "$";
	private Currency selectedCurrency = Currency.getInstance(Locale.getDefault());

	private boolean continueToNfc = false;

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();

		View rootView = inflater.inflate(R.layout.currency_dialog, null);

		Bundle args = getArguments();
		if (args != null) {
			continueToNfc = getArguments().getBoolean(CONTINUE_TO_NFC, false);
		}

		final Button welcome_continue = (Button) rootView.findViewById(R.id.welcome_continue);

		boolean hasNfc = NfcAdapter.getDefaultAdapter(getActivity()) != null;

		welcome_continue.setOnClickListener(clickListener);

		if (hasNfc) {
			welcome_continue.setText(R.string.welcome_continue);
		} else {
			welcome_continue.setText(R.string.welcome_got_it);
			continueToNfc = false;
		}

		if (!continueToNfc) {
			welcome_continue.setText(R.string.done);
		}

		if (continueToNfc) {
			setCancelable(false);
		} else {
			setCancelable(true);
		}

		welcome_select_currency = (RobotoButton) rootView.findViewById(R.id.welcome_select_currency);
		welcome_select_currency_display = (RobotoButton) rootView.findViewById(R.id.welcome_select_currency_display);

		custom_currency_check_after = (TintCheckBox) rootView.findViewById(R.id.custom_currency_check_after);

		welcome_currency_preview = (RobotoTextView) rootView.findViewById(R.id.welcome_currency_preview);

		Set<Currency> currencySet = getAllCurrencies();

		final ArrayList<Currency> currencyListUnsorted = new ArrayList<>(currencySet);
		final ArrayList<Currency> currencyList = new ArrayList<>();

		for (int i = 0; i < currencyListUnsorted.size(); i++) {
			if (currencyListUnsorted.get(i).getSymbol().length() == 1) {
				currencyList.add(currencyListUnsorted.get(i));
			}
		}

		for(Iterator<Currency> iterator = currencyListUnsorted.iterator(); iterator.hasNext();) {
			if (iterator.next().getSymbol().length() == 1) {
				iterator.remove();
			}
		}

		Collections.sort(currencyListUnsorted, new Resource.AlphabeticalCurrencyComparator());

		for (int i = 0; i < currencyListUnsorted.size(); i++) {
			currencyList.add(currencyListUnsorted.get(i));
		}

		final String[] currencyNameList = new String[currencyList.size()];

		for (int i = 0; i < currencyList.size(); i++) {
			currencyNameList[i] = currencyList.get(i).getSymbol();
		}

		if (savedInstanceState != null) {
			selectedCurrency = currencyList.get(3);
			displayCurrency = currencyNameList[3];
		}

		welcome_select_currency.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new MaterialDialog.Builder(getActivity())
						.title(R.string.select_currency)
						.items(currencyNameList)
						.itemsCallbackSingleChoice(3, new MaterialDialog.ListCallback() {
							@Override
							public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
								selectedCurrency = currencyList.get(which);
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

		custom_currency_check_after.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updatePreview();
			}
		});

		updatePreview();

		builder.setView(rootView);

		alertDialog = builder.create();
		return alertDialog;
	}

	private void updatePreview() {
		UserCurrency cur = new UserCurrency(selectedCurrency.getSymbol(), displayCurrency, !custom_currency_check_after.isChecked());
		welcome_currency_preview.setText(cur.render(20) + (displayCurrency.equals(selectedCurrency.getSymbol()) ? "" : " (" + selectedCurrency.getSymbol() + ")"));
	}

	private static Set<Currency> getAllCurrencies() {
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

			Currency.getInstance(Locale.getDefault()).getCurrencyCode();

			String cc = savedInstanceState.getString(CURRENCY_SAVE_KEY, "FAILED");
			if (cc.equals("FAILED")) {
				selectedCurrency = Currency.getInstance(Locale.getDefault());
			} else {
				selectedCurrency = Currency.getInstance(cc);
			}

			displayCurrency = savedInstanceState.getString(CURRENCY_DISPLAY_SAVE_KEY, "$");
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(CURRENCY_SAVE_KEY, selectedCurrency.getCurrencyCode());
		outState.putString(CURRENCY_DISPLAY_SAVE_KEY, displayCurrency);
	}

	View.OnClickListener clickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			//Button was disabled when no currencyId so we're free
			//to continue since the user was able to press the button

			if (continueToNfc) {
				WelcomeNfcDialogFragment welcomeNfcDialogFragment = new WelcomeNfcDialogFragment();
				welcomeNfcDialogFragment.show(getFragmentManager(), "welcome_nfc");
			}

			data.preferences.set("currency", new UserCurrency(selectedCurrency.getSymbol(), displayCurrency, !custom_currency_check_after.isChecked()));
			storage.commit();

			FeedFragment.adapter.notifyDataSetChanged();

			NavigationDrawerFragment.updateBalance(data);
			if (SettingsActivity.pref_currency != null) {
				SettingsActivity.pref_currency.setSummary(displayCurrency);
			}

			alertDialog.dismiss();
		}
	};

}
