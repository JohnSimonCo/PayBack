package com.johnsimon.payback;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by John on 2014-09-02.
 */
public class DebtSerializable {
	public UUID ownerId;
	public float amount;
	public String note;
	public boolean isPaidBack;

	public DebtSerializable(Debt debt) {
		this.ownerId = debt.owner.id;
		this.amount = debt.amount;
		this.note = debt.note;
		this.isPaidBack = debt.isPaidBack;
	}

	public Debt extract(ArrayList<Person> people) {
		return new Debt(AppData.findPerson(people, ownerId), amount, note, isPaidBack);
	}
}
