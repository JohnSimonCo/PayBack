package com.johnsimon.payback.currency;

import com.johnsimon.payback.data.Debt;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Currency;
import java.util.Locale;

public class UserCurrency {
	public final static int DECIMAL_SEPARATOR_DOT    = 0;
	public final static int DECIMAL_SEPARATOR_COMMA  = 1;

	public final static int THOUSAND_SEPARATOR_NONE  = 0;
	public final static int THOUSAND_SEPARATOR_DOT   = 1;
	public final static int THOUSAND_SEPARATOR_COMMA = 2;
	public final static int THOUSAND_SEPARATOR_SPACE = 3;

	public final String id;
	public final String displayName;
	public final boolean before;
    public final int decimalSeparator;
    public final int thousandSeparator;

    private transient DecimalFormat format;

	public UserCurrency(String id, String displayName, boolean before, int decimalSeparator, int thousandSeparator) {
		this.id = id;
		this.displayName = displayName;
		this.before = before;
        this.decimalSeparator = decimalSeparator;
        this.thousandSeparator = thousandSeparator;

		format = createFormat();
	}

	public String getDisplayName() {
		return displayName == null ? id : displayName;
	}

	public String render() {
		String output = this.id;
		if(displayName != null && !displayName.equals(id)) {
			output += " (" + displayName + ")";
		}
		return output;
	}

	public String render(Debt debt) {
		return render(debt.getAmount());
	}

	public String render(float amount) {
		if(format == null) format = createFormat();
        return format.format(Math.abs(amount));
	}

	private DecimalFormat createFormat() {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator(decimalSeparator());
        symbols.setMonetaryDecimalSeparator(decimalSeparator());
		symbols.setCurrencySymbol(getDisplayName());

		String formatString = thousandSeparator == THOUSAND_SEPARATOR_NONE ? "###.###" : "###,###.###";

		formatString = before ? "¤ " + formatString : formatString + " ¤";

		if(thousandSeparator != THOUSAND_SEPARATOR_NONE) {
			symbols.setGroupingSeparator(thousandSeparator());
		}

		DecimalFormat format = new DecimalFormat(formatString, symbols);
		format.setRoundingMode(RoundingMode.HALF_UP);

		return format;
	}

	private char decimalSeparator() {
		switch(decimalSeparator) {
			case DECIMAL_SEPARATOR_COMMA: return ',';
			case DECIMAL_SEPARATOR_DOT:
			default: return '.';
		}
	}

	private char thousandSeparator() {
		switch(thousandSeparator) {
			case THOUSAND_SEPARATOR_DOT: return '.';
			case THOUSAND_SEPARATOR_COMMA: return ',';
			case THOUSAND_SEPARATOR_SPACE:
			default: return ' ';
		}
	}

}
