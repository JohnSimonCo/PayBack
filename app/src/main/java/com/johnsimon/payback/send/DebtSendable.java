package com.johnsimon.payback.send;

import com.johnsimon.payback.core.Debt;
import com.johnsimon.payback.core.Person;

import java.util.UUID;

/**
 * Created by John on 2014-11-13.
 */
public class DebtSendable {
    public UUID id;
	public float amount;
	public String note;
	public long timestamp;
	public boolean isPaidBack;

	public DebtSendable(Debt debt) {
		this.id = debt.id;
        this.amount = debt.amount;
		this.note = debt.note;
		this.timestamp = debt.timestamp;
		this.isPaidBack = debt.isPaidBack;
	}

	public Debt extract(Person person) {
		//Reverse amount
		return new Debt(person, -amount, note, id, timestamp, isPaidBack);
	}
}