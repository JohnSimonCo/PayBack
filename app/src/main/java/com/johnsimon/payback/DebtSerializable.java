package com.johnsimon.payback;

import java.util.ArrayList;
import java.util.UUID;

public class DebtSerializable {
	public UUID ownerId;
	public float amount;
	public String note;
	public long timestamp;
	public boolean isPaidBack;

	public DebtSerializable(Debt debt) {
		this.ownerId = debt.owner.id;
		this.amount = debt.amount;
		this.note = debt.note;
		this.timestamp = debt.timestamp;
		this.isPaidBack = debt.isPaidBack;
	}

	public Debt extract(ArrayList<Person> people) {
		return new Debt(AppData.findPerson(people, ownerId), amount, note, timestamp, isPaidBack);
	}
}
