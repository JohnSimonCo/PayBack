package com.johnsimon.payback.send;

import com.johnsimon.payback.data.Debt;
import com.johnsimon.payback.data.Person;

import java.util.UUID;

public class DebtSendable {
    public UUID id;
	public float amount;
	public String note;
	public long timestamp;
	public boolean isPaidBack;
	public String currency;

	public DebtSendable(Debt debt) {
		this.id = debt.id;
        this.amount = debt.getAmount();
		this.note = debt.getNote();
		this.timestamp = debt.timestamp;
		this.isPaidBack = debt.isPaidBack();
		this.currency = debt.currency;
	}

	public Debt extract(Person person) {
		//Reverse amount
		return new Debt(person, -amount, note, id, timestamp, System.currentTimeMillis(), isPaidBack, currency);
	}
}