package com.johnsimon.payback.currency;

/**
 * Created by John on 2015-02-02.
 */
public class CurrencyFormat {

	public final static int DECIMAL_SEPARATOR_DOT = 0;
	public final static int DECIMAL_SEPARATOR_COMMA = 1;

	public final static int THOUSAND_SEPARATOR_NONE = 0;
	public final static int THOUSAND_SEPARATOR_DOT = 1;
	public final static int THOUSAND_SEPARATOR_COMMA = 2;
	public final static int THOUSAND_SEPARATOR_SPACE = 3;

	public final int decimalSeparator;
	public final int thousandSeparator;

	public CurrencyFormat(int decimalSeparator, int thousandSeparator) {
		this.decimalSeparator = decimalSeparator;
		this.thousandSeparator = thousandSeparator;
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
