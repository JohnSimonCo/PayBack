package com.johnsimon.payback.core;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.johnsimon.payback.R;
import com.johnsimon.payback.util.Resource;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.UUID;

public class Debt extends SyncedData<Debt> {
	private final static int POSITIVE_COLOR = R.color.green;
	private final static int NEGATIVE_COLOR = R.color.red;

	private final static int POSITIVE_COLOR_DISABLED = R.color.green_disabled;
	private final static int NEGATIVE_COLOR_DISABLED = R.color.red_disabled;


	private transient Person owner;
	//Used for (de)serialization
	private UUID ownerId;
	private float amount;
	private String note;
	public final long timestamp;
	private boolean paidBack;
	public String currency;

    public Debt(Person owner, float amount, String note, UUID id, long timestamp, long touched, boolean paidBack) {
		super(id, touched);

        this.owner = owner;
		this.ownerId = owner.id;
        this.amount = amount;
        this.note = note;
        this.timestamp = timestamp;
        this.paidBack = paidBack;
    }

	private Debt(Person owner, float amount, String note, long time) {
        this(owner, amount, note, UUID.randomUUID(), time, time, false);
	}

	//Used when creating new
	public Debt(Person owner, float amount, String note) {
		this(owner, amount, note, System.currentTimeMillis());
	}

	public void linkOwner(ArrayList<Person> people) {
		for(Person person : people) {
			if(person.id.equals(ownerId)) {
				this.owner = person;
				return;
			}
		}
	}

	public Person getOwner() {
		return owner;
	}

	public void setOwner(Person owner) {
		touch();
		this.owner = owner;
		this.ownerId = owner.id;
	}

	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		touch();
		this.amount = amount;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		touch();
		this.note = note;
	}

	public boolean isPaidBack() {
		return paidBack;
	}

	public void setPaidBack(boolean isPaidBack) {
		touch();
		this.paidBack = isPaidBack;
	}

	public void edit(Person owner, float amount, String note) {
		this.owner = owner;
		this.amount = amount;
		this.note = note;
		touch();
	}

	public String amountString() {
		return Debt.amountString(amount);
	}

	public static String amountString(float amount) {
		return Float.toString(Math.abs(amount))
				.replaceAll("\\.0$", "")
				+ " " + Resource.getCurrency();
	}

	public int getColor() {
		return amount > 0 ? POSITIVE_COLOR : NEGATIVE_COLOR;
	}

	public int getDisabledColor() {
		return amount > 0 ? POSITIVE_COLOR_DISABLED : NEGATIVE_COLOR_DISABLED;
	}
	/*
	public static int getColor(float amount) {
		return amount > 0 ? POSITIVE_COLOR : NEGATIVE_COLOR;
	}

	public static int getDisabledColor(float amount) {
		return amount > 0 ? POSITIVE_COLOR_DISABLED : NEGATIVE_COLOR_DISABLED;
	}
	*/

	public static String totalString(float amount, String even, boolean isAll, String allEvenString) {
		if (amount == 0) {
			return isAll ? allEvenString : even;
		} else {
			return (amount > 0 ? "+ " : "- ") + amountString(amount);
		}
	}

	//Method to get a string usable for sharing.
	public String getShareString(Context ctx) {
		String shareText =
				this.amount < 0 ?
				ctx.getString(R.string.ioweyou) :
				ctx.getString(R.string.youoweme);

		shareText += " " + this.amountString();
		if (!TextUtils.isEmpty(this.note)) {
			shareText += " " + ctx.getString(R.string.debt_for) + " " +  this.note;
		}

		return shareText;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (!(o instanceof Debt))return false;
		Debt other = (Debt) o;

		return id.equals(other.id)
			&& touched == other.touched
			&& owner.id.equals(other.owner.id)
			&& amount == other.amount
			&& (note == null ? other.note == null : note.equals(other.note))
			&& paidBack == other.paidBack;
	}

	@Override
	public String toString() {
		return amount + " for " + note;
	}

}
