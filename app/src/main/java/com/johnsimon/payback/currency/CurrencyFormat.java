package com.johnsimon.payback.currency;

import java.text.DecimalFormat;

public class CurrencyFormat {

	public final static int DECIMAL_SEPARATOR_DOT = 0;
	public final static int DECIMAL_SEPARATOR_COMMA = 1;

	public final static int THOUSAND_SEPARATOR_NONE = 0;
	public final static int THOUSAND_SEPARATOR_DOT = 1;
	public final static int THOUSAND_SEPARATOR_COMMA = 2;
	public final static int THOUSAND_SEPARATOR_SPACE = 3;

	public final int decimalSeparator;
	public final int thousandSeparator;

	public final DecimalFormat format;

	public CurrencyFormat(int decimalSeparator, int thousandSeparator) {
		this.decimalSeparator = decimalSeparator;
		this.thousandSeparator = thousandSeparator;

		format = createFormat();

		test();
	}

	void test() {
		String s1 = format.format(1000000.123123213);
		String s2 = format.format(12312.3);
		String s3 = format.format(1.0);

		int i = 0;
	}

	private DecimalFormat createFormat() {
		String formatString = "###";

		if(thousandSeparator != THOUSAND_SEPARATOR_NONE) {
			formatString += thousandSeparator() + "###";
		}

		formatString += decimalSeparator();

		formatString += ".##";

		return new DecimalFormat(formatString);
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
