package com.johnsimon.payback.serialize;

import com.johnsimon.payback.core.Debt;
import com.johnsimon.payback.core.Person;

import java.util.ArrayList;
import java.util.UUID;

public class DebtSerializable extends SyncedDataSerializable {
	public UUID ownerId;
	public float amount;
	public String note;
	public long timestamp;
	public boolean isPaidBack;

	public DebtSerializable(Debt debt) {
		super(debt.id, debt.touched);
		this.ownerId = debt.getOwner().id;
		this.amount = debt.getAmount();
		this.note = debt.getNote();
		this.timestamp = debt.timestamp;
		this.isPaidBack = debt.isPaidBack();
	}

	public Debt extract(ArrayList<Person> people) {
		return new Debt(owner(people), amount, note, id, timestamp, touched, isPaidBack);
	}

	private Person owner(ArrayList<Person> people) {
		for(Person person : people) {
			if(person.id.equals(ownerId)) return person;
		}
		return null;
	}
}
