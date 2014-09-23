package com.johnsimon.payback;

public class Debt {
	private final static int NEUTRAL_COLOR = R.color.gray_text_light;
	private final static int POSITIVE_COLOR = R.color.green;
	private final static int NEGATIVE_COLOR = R.color.red;

	private final static int NEUTRAL_COLOR_DISABLED = R.color.gray_text_very_light;
	private final static int POSITIVE_COLOR_DISABLED = R.color.green_disabled;
	private final static int NEGATIVE_COLOR_DISABLED = R.color.red_disabled;

	public Person owner;
	public float amount;
	public String amountAsString;
	public String note;
	public int color;
	public boolean isPaidBack;

	public Debt(Person owner, float amount, String note) {
		this.owner = owner;
		this.amount = amount;
		this.amountAsString = amountString(amount);
		this.note = note;
		this.color = getColor(amount);
	}

	public Debt(Person owner, float amount, String note, boolean isPaidBack) {
		this(owner, amount, note);
		this.isPaidBack = isPaidBack;
	}

	public static String amountString(float amount) {
		return Float.toString(Math.abs(amount))
//				.replaceAll("(\\.\\d)\\d+$", "$1")
				.replaceAll("\\.0$", "")
				+ " " + Resource.getCurrency();
	}

	public static int getColor(float amount) {
		return amount == 0 ? NEUTRAL_COLOR : amount > 0 ? POSITIVE_COLOR : NEGATIVE_COLOR;
	}

	public static int getDisabledColor(float amount) {
		return amount == 0 ? NEUTRAL_COLOR : amount > 0 ? POSITIVE_COLOR_DISABLED : NEGATIVE_COLOR_DISABLED;
	}

	public static String totalString(float amount, String even) {
		if (amount == 0) {
			return even;
		} else {
			return (amount > 0 ? "+ " : "- ") + amountString(amount);
		}
	}
}
