package com.johnsimon.payback.currency;

import com.johnsimon.payback.data.Debt;

import java.text.DecimalFormat;

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

    private final DecimalFormat format;

	public UserCurrency(String id, String displayName, boolean before, int decimalSeparator, int thousandSeparator) {
		this.id = id;
		this.displayName = displayName;
		this.before = before;
        this.decimalSeparator = decimalSeparator;
        this.thousandSeparator = thousandSeparator;

        format = new DecimalFormat("###" + thousandSeparator() + "###" + decimalSeparator() + "###");
	}

	public String getDisplayName() {
		return displayName == null ? id : displayName;
	}

	public String render() {
		String output = this.id;
		if(displayName != null) {
			output += " (" + displayName + ")";
		}
		return output;
	}

	public String render(float amount) {
		return render(Float.toString(Math.abs(amount)).replaceAll("\\.0$", ""));
	}

	public String render(String amount) {
		return renderCurrency(amount, getDisplayName());
	}

	public String render(Debt debt) {
		return render(debt.getAmount(), debt.currencyId);
	}

	public String render(float amount, String currencyId) {
		return render(Float.toString(Math.abs(amount)).replaceAll("\\.0$", ""), currencyId);
	}

	public String render(String amount, String currencyId) {
        //TODO efter launch: displaya rätt currency
        //return renderCurrency(amount, currencyId.equals(id) && displayName != null ? displayName : currencyId);
        return renderCurrency(amount, getDisplayName());
	}

	private String renderCurrency(String amount, String symbol) {
		return before ? symbol + " " + amount : amount + " " + symbol;
	}

    private String decimalSeparator() {
        switch(decimalSeparator) {
            case DECIMAL_SEPARATOR_COMMA: return ",";
            default: return ",";
        }
    }

    private String thousandSeparator() {
        switch(thousandSeparator) {
			case THOUSAND_SEPARATOR_DOT: return ".";
			case THOUSAND_SEPARATOR_COMMA: return ",";
			case THOUSAND_SEPARATOR_SPACE : return " ";
			default: return "";
		}
    }
}
