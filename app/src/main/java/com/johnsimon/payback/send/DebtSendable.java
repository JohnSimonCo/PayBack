package com.johnsimon.payback.send;

import com.google.gson.annotations.SerializedName;
import com.johnsimon.payback.data.Debt;
import com.johnsimon.payback.data.Person;
import com.johnsimon.payback.data.Payment;

import java.util.ArrayList;
import java.util.UUID;

public class DebtSendable {
	@SerializedName("amount")
	public double amount;

	@SerializedName("note")
	public String note;

	@SerializedName("timestamp")
	public long timestamp;

	@SerializedName("paidBack")
	public boolean paidBack;

	@SerializedName("payments")
	public ArrayList<Payment> payments;

	@SerializedName("currency")
	public String currency;

	public DebtSendable(Debt debt) {
        this.amount = debt.getAmount();
		this.note = debt.getNote();
		this.timestamp = debt.timestamp;
		this.paidBack = debt.isPaidBack();
		this.payments = debt.payments;
		this.currency = debt.currencyId;
	}

	public Debt extract(Person person) {
		//Reverse amount
		return new Debt(person, -amount, note, UUID.randomUUID(), timestamp, System.currentTimeMillis(), paidBack, payments, currency);
	}
}