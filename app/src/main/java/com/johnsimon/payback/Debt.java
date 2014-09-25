package com.johnsimon.payback;

public class Debt {
	private final static int POSITIVE_COLOR = R.color.green;
	private final static int NEGATIVE_COLOR = R.color.red;

	private final static int POSITIVE_COLOR_DISABLED = R.color.green_disabled;
	private final static int NEGATIVE_COLOR_DISABLED = R.color.red_disabled;

	public Person owner;
	public float amount;
	public String amountAsString;
	public String note;
	//Also works as an id (since two different debts can't be created at the exact same time)
	public long timestamp;
	public int color;
	public boolean isPaidBack;

	//Used when creating new
	public Debt(Person owner, float amount, String note) {
		this.owner = owner;
		this.amount = amount;
		this.amountAsString = amountString(amount);
		this.note = note;
		this.color = getColor(amount);

		this.timestamp = System.currentTimeMillis();
		this.isPaidBack = false;
	}

	//Used for creating multiple
	public Debt(Person owner, float amount, String note, long timestamp) {
		this.owner = owner;
		this.amount = amount;
		this.amountAsString = amountString(amount);
		this.note = note;
		this.color = getColor(amount);

		this.timestamp = timestamp;
		this.isPaidBack = false;
	}


	//Used when extracting from serializable form
	public Debt(Person owner, float amount, String note, long timestamp, boolean isPaidBack) {
		this(owner, amount, note);

		this.timestamp = timestamp;
		this.isPaidBack = isPaidBack;
	}

	public static String amountString(float amount) {
		return Float.toString(Math.abs(amount))
//				.replaceAll("(\\.\\d)\\d+$", "$1")
				.replaceAll("\\.0$", "")
				+ " " + Resource.getCurrency();
	}

	public static int getColor(float amount) {
		return amount > 0 ? POSITIVE_COLOR : NEGATIVE_COLOR;
	}

	public static int getDisabledColor(float amount) {
		return amount > 0 ? POSITIVE_COLOR_DISABLED : NEGATIVE_COLOR_DISABLED;
	}

	public static String totalString(float amount, String even) {
		if (amount == 0) {
			return even;
		} else {
			return (amount > 0 ? "+ " : "- ") + amountString(amount);
		}
	}

	public void edit(Person owner, float amount, String note) {
		this.owner = owner;
		this.amount = amount;
		this.note = note;
	}
}
