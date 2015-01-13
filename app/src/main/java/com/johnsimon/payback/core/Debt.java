package com.johnsimon.payback.core;

import android.content.Context;
import android.text.TextUtils;

import com.johnsimon.payback.R;
import com.johnsimon.payback.util.Resource;

import java.util.UUID;

public class Debt implements Syncable<Debt> {
	private final static int POSITIVE_COLOR = R.color.green;
	private final static int NEGATIVE_COLOR = R.color.red;

	private final static int POSITIVE_COLOR_DISABLED = R.color.green_disabled;
	private final static int NEGATIVE_COLOR_DISABLED = R.color.red_disabled;

	public Person owner;
    public UUID id;
	public float amount;
	public String amountAsString;
	public String note;
	public long timestamp;
	public int color;
	public boolean isPaidBack;

    public Debt(Person owner, float amount, String note, UUID id, long timestamp, boolean isPaidBack) {
        this.owner = owner;
        this.id = id;
        this.amount = amount;
        this.amountAsString = amountString(amount);
        this.note = note;
        this.timestamp = timestamp;
        this.color = getColor(amount);
        this.isPaidBack = isPaidBack;
    }

	//Used when creating new
	public Debt(Person owner, float amount, String note) {
        this(owner, amount, note, UUID.randomUUID(), System.currentTimeMillis(), false);
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

	public static String totalString(float amount, String even, boolean isAll, String allEvenString) {
		if (amount == 0) {
            return isAll ? allEvenString : even;
		} else {
			return (amount > 0 ? "+ " : "- ") + amountString(amount);
		}
	}

	public void edit(Person owner, float amount, String note) {
		this.owner = owner;
		this.amount = amount;
		this.amountAsString = amountString(amount);
		this.note = note;
	}

	//Method to get a string usable for sharing.
	public String getShareString(Context ctx) {
		String shareText;

		if (this.amount < 0) {
			shareText = ctx.getString(R.string.ioweyou);
		} else {
			shareText = ctx.getString(R.string.youoweme);
		}

		shareText = shareText + " " + this.amountAsString;
		if (!TextUtils.isEmpty(shareText)) {
			shareText = shareText + " " + ctx.getString(R.string.debt_for) + " " + this.note;
		}

		return shareText;
	}

    @Override
    public UUID getId() {
        return id;
    }


    @Override
    public Debt syncWith(Debt other) {
        return Debt.sync(this, other);
    }

    public static Debt sync(Debt a, Debt b) {
		//TODO implement
        return a;
    }

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (!(o instanceof Debt))return false;
		Debt other = (Debt) o;

		return id.equals(other.id)
			&& owner.id.equals(other.owner.id)
			&& amount == other.amount
			&& note == null ? other.note == null : note.equals(other.note)
			&& isPaidBack == other.isPaidBack;
	}
}
