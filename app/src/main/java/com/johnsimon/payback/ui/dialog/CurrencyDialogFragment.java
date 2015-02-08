package com.johnsimon.payback.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v7.internal.widget.TintCheckBox;
import android.support.v7.internal.widget.TintSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.devspark.robototextview.widget.RobotoButton;
import com.johnsimon.payback.R;
import com.johnsimon.payback.core.DataDialogFragment;
import com.johnsimon.payback.currency.UserCurrency;
import com.johnsimon.payback.currency.CurrencyUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.Set;

public class CurrencyDialogFragment extends DataDialogFragment {

	public final static String CONTINUE = "CONTINUE";
	public final static String CANCELABLE = "CANCELABLE";
	public final static String SHOW_INFO_TEXT = "SHOW_INFO_TEXT";

	private AlertDialog alertDialog;

	public final static String CURRENCY_SAVE_KEY = "CURRENCY_SAVE_KEY";
	public final static String CURRENCY_DISPLAY_SAVE_KEY = "CURRENCY_BEFORE_SAVE_KEY";
	public final static String CURRENCY_CHECKBOX = "CURRENCY_CHECKBOX";
	public final static String CURRENCY_DECIMAL_SEPARATOR = "CURRENCY_DECIMAL_SEPARATOR";
	public final static String CURRENCY_THOUSAND_SPINNER = "CURRENCY_THOUSAND_SPINNER";

	private RobotoButton welcome_select_currency;
	private RobotoButton welcome_select_currency_display;
	private TintCheckBox custom_currency_check_after;
    private TintCheckBox custom_currency_decimal_separator;
    private TintSpinner currency_thousand_separator;
	private TextView welcome_currency_preview;

	private String displayCurrency = "";
	private String selectedCurrency = "";

	private boolean continueToNfc = false;
    private boolean usingDefaults = true;

    public CurrencySelectedCallback currencySelectedCallback;

    /*TODO simmes feautre
        Vi borde ha med USD och alla de valutorna och fortfarande ha
        dollartecken och eurotecken i början. Det känns fel att
        USD och EUR helt saknas i listan. De möjliga scenariorna är:

        1.  Person använder dollar, hittar det i början <- Påverkas inte

        2.  Person använder dollar, scrollar ned och
            hittar det för han letar alltid efter USD   <- Påverkas positivt

        3.  Person använder inte dollar, använder dock
            USD och liknande för att orientera sig för
            hans valuta kanske börjar på U också.       <- Påverkas positivt
     */

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
        custom_currency_decimal_separator = (TintCheckBox) rootView.findViewById(R.id.custom_currency_decimal_separator);
		currency_thousand_separator = (TintSpinner) rootView.findViewById(R.id.currency_thousand_separator);

        welcome_currency_preview = (TextView) rootView.findViewById(R.id.welcome_currency_preview);

		final List<String> order = Arrays.asList("$", "€", "£", "₪", "₫", "₩", "¥", "฿");

		Set<Currency> currencySet = CurrencyUtils.getAllCurrencies();

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

			String cc = savedInstanceState.getString(CURRENCY_SAVE_KEY, null);
			if (cc == null) {
                usingDefaults = false;
			} else {
				selectedCurrency = cc;
			}

			displayCurrency = savedInstanceState.getString(CURRENCY_DISPLAY_SAVE_KEY, "$");

            custom_currency_check_after.setChecked(savedInstanceState.getBoolean(CURRENCY_CHECKBOX, false));
            custom_currency_decimal_separator.setChecked(savedInstanceState.getBoolean(CURRENCY_DECIMAL_SEPARATOR, false));

			currency_thousand_separator.setSelection(savedInstanceState.getInt(CURRENCY_THOUSAND_SPINNER, UserCurrency.THOUSAND_SEPARATOR_NONE));
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
								try {
                                    selectedCurrency = currencyNames.get(which);
                                    displayCurrency = text.toString();
                                    updatePreview();
                                } catch (Exception e) {
                                    //TODO mer avancerad crash prevention
                                }
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

        custom_currency_decimal_separator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePreview();
            }
        });

		currency_thousand_separator.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				updatePreview();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

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
            custom_currency_decimal_separator.setChecked(data.preferences.getCurrency().decimalSeparator == UserCurrency.DECIMAL_SEPARATOR_COMMA);
			currency_thousand_separator.setSelection(data.preferences.getCurrency().thousandSeparator);
            updatePreview();
        }
        super.onDataReceived();
    }

    private void updatePreview() {
		UserCurrency cur = new UserCurrency(selectedCurrency, displayCurrency, !custom_currency_check_after.isChecked(), custom_currency_decimal_separator.isChecked() ? UserCurrency.DECIMAL_SEPARATOR_COMMA : UserCurrency.DECIMAL_SEPARATOR_DOT, currency_thousand_separator.getSelectedItemPosition());
		welcome_currency_preview.setText(cur.render(2400.5f) + (displayCurrency.equals(selectedCurrency) ? "" : " (" + selectedCurrency + ")"));

		welcome_select_currency.setText(getString(R.string.currency) + " (" + selectedCurrency + ")");
		welcome_select_currency_display.setText(getString(R.string.currency_symbol) + " (" + displayCurrency + ")");
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(CURRENCY_SAVE_KEY, selectedCurrency);
		outState.putString(CURRENCY_DISPLAY_SAVE_KEY, displayCurrency);
        outState.putBoolean(CURRENCY_CHECKBOX, custom_currency_check_after.isChecked());
        outState.putBoolean(CURRENCY_DECIMAL_SEPARATOR, custom_currency_decimal_separator.isChecked());
		outState.putInt(CURRENCY_THOUSAND_SPINNER, currency_thousand_separator.getSelectedItemPosition());
	}

	View.OnClickListener clickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {

			if (continueToNfc) {
				WelcomeNfcDialogFragment welcomeNfcDialogFragment = new WelcomeNfcDialogFragment();
				welcomeNfcDialogFragment.show(getFragmentManager(), "welcome_nfc");
			}

			UserCurrency userCurrency = new UserCurrency(selectedCurrency, displayCurrency, !custom_currency_check_after.isChecked(), custom_currency_decimal_separator.isChecked() ? UserCurrency.DECIMAL_SEPARATOR_COMMA : UserCurrency.DECIMAL_SEPARATOR_DOT, currency_thousand_separator.getSelectedItemPosition());

			data.preferences.currency.setValue(userCurrency);
			storage.commit();

            currencySelectedCallback.onCurrencySelected();

			alertDialog.dismiss();
		}
	};

    public interface CurrencySelectedCallback {
        public void onCurrencySelected();
    }

}