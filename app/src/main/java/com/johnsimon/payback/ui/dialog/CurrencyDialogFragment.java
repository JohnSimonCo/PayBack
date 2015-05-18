package com.johnsimon.payback.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatSpinner;
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

public class CurrencyDialogFragment extends DataDialogFragment {

	public final static String CONTINUE = "CONTINUE";
	public final static String CANCELABLE = "CANCELABLE";
	public final static String SHOW_INFO_TEXT = "SHOW_INFO_TEXT";

	private AlertDialog alertDialog;

	public final static String CURRENCY_SAVE_KEY = "CURRENCY_SAVE_KEY";
	public final static String CURRENCY_DISPLAY_SAVE_KEY = "CURRENCY_BEFORE_SAVE_KEY";
	public final static String CURRENCY_CHECKBOX = "CURRENCY_CHECKBOX";
	public final static String CURRENCY_DECIMAL_SEPARATOR = "CURRENCY_DECIMAL_SEPARATOR";
	public final static String CURRENCY_TRAILING_ZEROS = "CURRENCY_TRAILING_ZEROS";
	public final static String CURRENCY_THOUSAND_SPINNER = "CURRENCY_THOUSAND_SPINNER";
	public final static String KEY_FIRST_TIME = "KEY_FIRST_TIME";

	private RobotoButton welcome_select_currency;
	private RobotoButton welcome_select_currency_display;
	private AppCompatCheckBox custom_currency_check_after;
    private AppCompatCheckBox custom_currency_decimal_separator;
    private AppCompatCheckBox custom_currency_trailing_zeros;
    private AppCompatSpinner currency_thousand_separator;
	private TextView welcome_currency_preview;

	private final static float DISPLAY_VALUE =  2400.5f;

	private String currencyDisplay = "";
	private String currencyCode = "";

	private boolean continueToNfc = false;
    private boolean usingDefaults = true;
	private boolean firstTime = false;

    public CurrencySelectedCallback currencySelectedCallback;

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();

		View rootView = inflater.inflate(R.layout.currency_dialog, null);

		Bundle args = getArguments();
		if (args != null) {
			continueToNfc = args.getBoolean(CONTINUE, false);
			firstTime = args.getBoolean(KEY_FIRST_TIME, false);

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

		custom_currency_check_after = (AppCompatCheckBox) rootView.findViewById(R.id.custom_currency_check_after);
        custom_currency_decimal_separator = (AppCompatCheckBox) rootView.findViewById(R.id.custom_currency_decimal_separator);
        custom_currency_trailing_zeros = (AppCompatCheckBox) rootView.findViewById(R.id.custom_currency_trailing_zeros);
		currency_thousand_separator = (AppCompatSpinner) rootView.findViewById(R.id.currency_thousand_separator);

        welcome_currency_preview = (TextView) rootView.findViewById(R.id.welcome_currency_preview);

		firstTime = savedInstanceState.getBoolean(KEY_FIRST_TIME, false);

		if (savedInstanceState != null) {

			String cc = savedInstanceState.getString(CURRENCY_SAVE_KEY, null);
			if (cc == null) {
                usingDefaults = false;
			} else {
				currencyCode = cc;
			}

			currencyDisplay = savedInstanceState.getString(CURRENCY_DISPLAY_SAVE_KEY, "");

            custom_currency_check_after.setChecked(savedInstanceState.getBoolean(CURRENCY_CHECKBOX, false));
            custom_currency_decimal_separator.setChecked(savedInstanceState.getBoolean(CURRENCY_DECIMAL_SEPARATOR, false));
            custom_currency_trailing_zeros.setChecked(savedInstanceState.getBoolean(CURRENCY_TRAILING_ZEROS, false));

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

		CurrencyUtils.generateAllCurrenciesWithPrioritizedAsDisplay();

		welcome_select_currency.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new MaterialDialog.Builder(getActivity())
						.title(R.string.select_currency)
						.items(CurrencyUtils.allCurrenciesWithPrioritizedAsDisplay)
						.itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog materialDialog, View view, int which, CharSequence charSequence) {
                                try {
                                    currencyCode = CurrencyUtils.getAllCurrenciesWithPrioritized()[which][0];
                                    currencyDisplay = CurrencyUtils.getAllCurrenciesWithPrioritized()[which][1];
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
						currencyDisplay = currency;
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

        custom_currency_trailing_zeros.setOnClickListener(new View.OnClickListener() {
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

            UserCurrency userCurrency = data.preferences.getCurrency();

            currencyCode = userCurrency.id;
            currencyDisplay = userCurrency.getDisplayName();
            custom_currency_check_after.setChecked(!userCurrency.before);
            custom_currency_decimal_separator.setChecked(userCurrency.decimalSeparator == UserCurrency.DECIMAL_SEPARATOR_COMMA);
            custom_currency_trailing_zeros.setChecked(userCurrency.trailingZeros);
			currency_thousand_separator.setSelection(userCurrency.thousandSeparator);
            updatePreview();
        }
        super.onDataReceived();
    }

    private void updatePreview() {
		UserCurrency cur = new UserCurrency(currencyCode, currencyDisplay, !custom_currency_check_after.isChecked(), custom_currency_decimal_separator.isChecked() ? UserCurrency.DECIMAL_SEPARATOR_COMMA : UserCurrency.DECIMAL_SEPARATOR_DOT, currency_thousand_separator.getSelectedItemPosition(), custom_currency_trailing_zeros.isChecked());

        boolean same = currencyCode.equals(currencyDisplay);
        if (same) {
            welcome_currency_preview.setText(cur.render(DISPLAY_VALUE));
        } else {
            welcome_currency_preview.setText(cur.render(DISPLAY_VALUE) + " (" + currencyCode + ")");
        }

		welcome_select_currency.setText(getString(R.string.currency) + " (" + currencyCode + ")");
		welcome_select_currency_display.setText(getString(R.string.currency_symbol) + " (" + currencyDisplay + ")");
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(CURRENCY_SAVE_KEY, currencyCode);
		outState.putString(CURRENCY_DISPLAY_SAVE_KEY, currencyDisplay);
        outState.putBoolean(CURRENCY_CHECKBOX, custom_currency_check_after.isChecked());
        outState.putBoolean(CURRENCY_DECIMAL_SEPARATOR, custom_currency_decimal_separator.isChecked());
        outState.putBoolean(CURRENCY_TRAILING_ZEROS, custom_currency_trailing_zeros.isChecked());
		outState.putBoolean(KEY_FIRST_TIME, firstTime);
		outState.putInt(CURRENCY_THOUSAND_SPINNER, currency_thousand_separator.getSelectedItemPosition());
	}

	View.OnClickListener clickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {

			UserCurrency userCurrency = new UserCurrency(currencyCode, currencyDisplay, !custom_currency_check_after.isChecked(), custom_currency_decimal_separator.isChecked() ? UserCurrency.DECIMAL_SEPARATOR_COMMA : UserCurrency.DECIMAL_SEPARATOR_DOT, currency_thousand_separator.getSelectedItemPosition(), custom_currency_trailing_zeros.isChecked());

			data.preferences.currency.setValue(userCurrency);
			storage.commit();

			currencySelectedCallback.onCurrencySelected();

			alertDialog.dismiss();

			if (continueToNfc) {
				WelcomeNfcDialogFragment welcomeNfcDialogFragment = new WelcomeNfcDialogFragment();
				welcomeNfcDialogFragment.show(getFragmentManager(), "welcome_nfc");
			} else {
				//TODO KOLLA HÄR JOHN
			}
		}
	};

    public interface CurrencySelectedCallback {
        void onCurrencySelected();
    }

}