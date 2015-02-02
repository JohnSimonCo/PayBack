package com.johnsimon.payback.currency;

import com.johnsimon.payback.data.Debt;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class UserCurrency {
	public final static int DECIMAL_SEPARATOR_DOT = 0;
	public final static int DECIMAL_SEPARATOR_COMMA = 1;

	public final static int THOUSAND_SEPARATOR_NONE = 0;
	public final static int THOUSAND_SEPARATOR_DOT = 1;
	public final static int THOUSAND_SEPARATOR_COMMA = 2;
	public final static int THOUSAND_SEPARATOR_SPACE = 3;

	public final String id;
	public final String displayName;
	public final boolean before;
    public final int decimalSeparator;
    public final int thousandSeparator;

    private final transient DecimalFormat format;

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

	public String render(Debt debt) {
		return render(debt.getAmount());
	}

	public String render(float amount) {
		return render(format.format(Math.abs(amount)), id);
	}

	public String render(String amount, String currencyId) {
        //TODO efter launch: displaya rätt currency
        //return renderCurrency(amount, currencyId.equals(id) && displayName != null ? displayName : currencyId);
		//return renderCurrency(amount, getDisplayName());
		return format.format(amount);
	}

	private String renderCurrency(String amount, String symbol) {
		return before ? symbol + " " + amount : amount + " " + symbol;
	}

	private DecimalFormat createFormat() {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator(decimalSeparator());
		symbols.setCurrencySymbol(getDisplayName());

		String formatString = thousandSeparator == THOUSAND_SEPARATOR_NONE ? "###.##" : "###,###.##";

		formatString = before ? "$ " + formatString : formatString + " $";

		if(thousandSeparator != THOUSAND_SEPARATOR_NONE) {
			symbols.setGroupingSeparator(thousandSeparator());
		}

		DecimalFormat format = new DecimalFormat(formatString, symbols);

		return format;
	}

	private char decimalSeparator() {
		switch(decimalSeparator) {
			case DECIMAL_SEPARATOR_COMMA: return ',';
			default: return '.';
		}
	}

	private char thousandSeparator() {
		switch(thousandSeparator) {
			case THOUSAND_SEPARATOR_DOT: return '.';
			case THOUSAND_SEPARATOR_COMMA: return ',';
			case THOUSAND_SEPARATOR_SPACE : return ' ';
			default: return '&';
		}
	}

}
