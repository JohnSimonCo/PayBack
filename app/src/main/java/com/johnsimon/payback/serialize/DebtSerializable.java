package com.johnsimon.payback.serialize;

import com.johnsimon.payback.core.Debt;
import com.johnsimon.payback.core.Person;

import java.util.ArrayList;
import java.util.UUID;

public class DebtSerializable {
	public UUID ownerId;
    public UUID id;
	public float amount;
	public String note;
	public long timestamp;
	public boolean isPaidBack;

	public DebtSerializable(Debt debt) {
		this.ownerId = debt.owner.id;
        this.id = debt.id;
		this.amount = debt.amount;
		this.note = debt.note;
		this.timestamp = debt.timestamp;
		this.isPaidBack = debt.isPaidBack;
	}

	public Debt extract(ArrayList<Person> people) {
		return new Debt(owner(people), amount, note, timestamp, isPaidBack);
	}

	private Person owner(ArrayList<Person> people) {
		for(Person person : people) {
			if(person.id.equals(ownerId)) return person;
		}
		return null;
	}
}
