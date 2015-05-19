package com.johnsimon.payback.send;

import com.google.gson.annotations.SerializedName;
import com.johnsimon.payback.data.Debt;
import com.johnsimon.payback.data.Person;

import java.util.UUID;

public class DebtSendable {
	@SerializedName("id")
	public UUID id;

	@SerializedName("amount")
	public float amount;

	@SerializedName("note")
	public String note;

	@SerializedName("timestamp")
	public long timestamp;

	@SerializedName("paidBack")
	public boolean paidBack;

	@SerializedName("datePaidBack")
	public Long datePaidBack;

	@SerializedName("currency")
	public String currency;

	public DebtSendable(Debt debt) {
		this.id = debt.id;
        this.amount = debt.getAmount();
		this.note = debt.getNote();
		this.timestamp = debt.timestamp;
		this.paidBack = debt.isPaidBack();
		this.datePaidBack = debt.datePaidBack;
		this.currency = debt.currencyId;
	}

	public Debt extract(Person person) {
		//Reverse amount
		return new Debt(person, -amount, note, id, timestamp, System.currentTimeMillis(), paidBack, datePaidBack, currency);
	}
}