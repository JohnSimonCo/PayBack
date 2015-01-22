package com.johnsimon.payback.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v7.internal.widget.TintCheckBox;
import android.support.v7.internal.widget.TintSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.devspark.robototextview.widget.RobotoButton;
import com.johnsimon.payback.R;
import com.johnsimon.payback.core.DataDialogFragment;
import com.johnsimon.payback.core.UserCurrency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CurrencyDialogFragment extends DataDialogFragment {

	public final static String CONTINUE = "CONTINUE";
	public final static String CANCELABLE = "CANCELABLE";
	public final static String SHOW_INFO_TEXT = "SHOW_INFO_TEXT";

	private AlertDialog alertDialog;

	public final static String CURRENCY_SAVE_KEY = "CURRENCY_SAVE_KEY";
	public final static String CURRENCY_DISPLAY_SAVE_KEY = "CURRENCY_BEFORE_SAVE_KEY";
	public final static String CURRENCY_CHECKBOX = "CURRENCY_CHECKBOX";

	private RobotoButton welcome_select_currency;
	private RobotoButton welcome_select_currency_display;

	private TintCheckBox custom_currency_check_after;

	private TextView welcome_currency_preview;

	private String displayCurrency = "";
	private String selectedCurrency = "";

	private boolean continueToNfc = false;
    private boolean usingDefaults = true;

	//TODO WHEN STARTED USE OLD CURRENCY

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();

		View rootView = inflater.inflate(R.layout.currency_dialog, null);

		Bundle args = getArguments();
		if (args != null) {
			continueToNfc = args.getBoolean(CONTINUE, false);

			if (args.getBoolean(SHOW_INFO_TEXT, false)) {
				rootView.findViewById(R.id.currency_info_text).setVisibility(View.VISIBLE);
			} else {
				rootView.findViewById(R.id.currency_info_text_pusher).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.welcome_cancel).setVisibility(View.VISIBLE);
			}
			setCancelable(args.getBoolean(CANCELABLE));
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

		welcome_select_currency = (RobotoButton) rootView.findViewById(R.id.welcome_select_currency);
		welcome_select_currency_display = (RobotoButton) rootView.findViewById(R.id.welcome_select_currency_display);

		custom_currency_check_after = (TintCheckBox) rootView.findViewById(R.id.custom_currency_check_after);

		welcome_currency_preview = (TextView) rootView.findViewById(R.id.welcome_currency_preview);

		final List<String> order = Arrays.asList("$", "€", "£", "₪", "₫", "₩", "¥", "฿");

		Set<Currency> currencySet = getAllCurrencies();

		final ArrayList<String> currencyNames = new ArrayList<>(currencySet.size());
		for(Currency currency : currencySet) {
			currencyNames.add(currency.getSymbol());
		}

		Collections.sort(currencyNames, new Comparator<String>() {
			@Override
			public int compare(String a, String b) {
				if(a.length() == 1) {
					if(b.length() == 1) {
						if(order.contains(a)) {
							return order.contains(b) ? order.indexOf(a) - order.indexOf(b) : -1;
						} else return 1;
					} else return -1;
				} else if(b.length() == 1) {
					return 1;
				} else {
					return a.compareToIgnoreCase(b);
				}
			}
		});

		if (savedInstanceState != null) {

			String cc = savedInstanceState.getString(CURRENCY_SAVE_KEY, "FAILED");
			if (cc.equals("FAILED")) {
                usingDefaults = false;
			} else {
				selectedCurrency = cc;
			}

			displayCurrency = savedInstanceState.getString(CURRENCY_DISPLAY_SAVE_KEY, "$");

            custom_currency_check_after.setChecked(savedInstanceState.getBoolean(CURRENCY_CHECKBOX, false));
		} else {
            usingDefaults = false;
        }

        rootView.findViewById(R.id.welcome_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

		welcome_select_currency.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new MaterialDialog.Builder(getActivity())
						.title(R.string.select_currency)
						.items(currencyNames.toArray(new String[currencyNames.size()]))
						.itemsCallbackSingleChoice(currencyNames.indexOf(selectedCurrency), new MaterialDialog.ListCallback() {
							@Override
							public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
								selectedCurrency = currencyNames.get(which);
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

    @Override
    protected void onDataReceived() {
        if (!usingDefaults) {
            selectedCurrency = data.preferences.getCurrency().id;
            displayCurrency = data.preferences.getCurrency().getDisplayName();
            custom_currency_check_after.setChecked(!data.preferences.getCurrency().before);
            updatePreview();
        }
        super.onDataReceived();
    }

    private void updatePreview() {
		UserCurrency cur = new UserCurrency(selectedCurrency, displayCurrency, !custom_currency_check_after.isChecked());
		welcome_currency_preview.setText(cur.render(20) + (displayCurrency.equals(selectedCurrency) ? "" : " (" + selectedCurrency + ")"));

		welcome_select_currency.setText(getString(R.string.currency) + " (" + selectedCurrency + ")");
		welcome_select_currency_display.setText(getString(R.string.change_currency_symbol) + " (" + displayCurrency + ")");
	}

	private static Set<Currency> getAllCurrencies() {
		Set<Currency> toret = new HashSet<>();
		Locale[] locs = Locale.getAvailableLocales();

		for(Locale loc : locs) {
			try {
				toret.add( Currency.getInstance( loc ) );
			} catch(Exception exc){
			}
		}

		return toret;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(CURRENCY_SAVE_KEY, selectedCurrency);
		outState.putString(CURRENCY_DISPLAY_SAVE_KEY, displayCurrency);
        outState.putBoolean(CURRENCY_CHECKBOX, custom_currency_check_after.isChecked());
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

			data.preferences.currency.setValue(new UserCurrency(selectedCurrency, displayCurrency, !custom_currency_check_after.isChecked()));
			storage.commit();

			FeedFragment.adapter.notifyDataSetChanged();

			NavigationDrawerFragment.updateBalance(data);
			if (SettingsActivity.pref_currency != null) {
				SettingsActivity.pref_currency.setSummary(displayCurrency);
			}

			alertDialog.dismiss();

			FeedFragment.displayTotalDebt(getResources(), new UserCurrency(selectedCurrency, displayCurrency, !custom_currency_check_after.isChecked()));
		}
	};

}
