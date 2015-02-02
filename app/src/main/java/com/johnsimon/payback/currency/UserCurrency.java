package com.johnsimon.payback.currency;

import com.johnsimon.payback.data.Debt;

import java.text.DecimalFormat;

public class UserCurrency {
	public final String id;
	public final String displayName;
	public final boolean before;
    public final int decimalSeparator;
    public final int thousandSeparator;

    private final transient CurrencyFormat format;

	public UserCurrency(String id, String displayName, boolean before, int decimalSeparator, int thousandSeparator) {
		this.id = id;
		this.displayName = displayName;
		this.before = before;
        this.decimalSeparator = decimalSeparator;
        this.thousandSeparator = thousandSeparator;

        format = new CurrencyFormat(decimalSeparator, thousandSeparator);
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
        return renderCurrency(amount, getDisplayName());
	}

	private String renderCurrency(String amount, String symbol) {
		return before ? symbol + " " + amount : amount + " " + symbol;
	}

}
