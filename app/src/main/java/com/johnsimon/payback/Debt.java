package com.johnsimon.payback;

public class Debt {
	private final static int NEUTRAL_COLOR = R.color.gray_text_light;
	private final static int POSITIVE_COLOR = R.color.green;
	private final static int NEGATIVE_COLOR = R.color.red;

	public Person owner;
	public float amount;
	public String amountAsString;
	public String note;
	public int color;

	public Debt(Person owner, float amount, String note) {
		this.owner = owner;
		this.amount = amount;
		this.amountAsString = amountString(amount);
		this.note = note;
		this.color = getColor(amount);
	}

	public static String amountString(float amount) {
		return Float.toString(Math.abs(amount))
				.replaceAll("(\\.\\d)\\d+$", "$1")
				.replaceAll("\\.0$", "")
				+ " " + Resource.getCurrency();
	}
	public static int getColor(float amount) {
		return amount == 0 ? NEUTRAL_COLOR : amount > 0 ? POSITIVE_COLOR : NEGATIVE_COLOR;
	}

	public static String totalString(float amount, String even) {
		if (amount == 0) {
			return even;
		} else {
			return (amount > 0 ? "+ " : "- ") + amountString(amount);
		}
	}
}
