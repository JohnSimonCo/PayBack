package com.johnsimon.payback.serialize;

import com.johnsimon.payback.core.Debt;
import com.johnsimon.payback.core.Person;
import com.johnsimon.payback.util.AppData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

public class AppDataSerializable {
	public ArrayList<PersonSerializable> people;
	public ArrayList<DebtSerializable> debts;

    public HashSet<UUID> deleted;

	public AppDataSerializable(AppData data) {
		this.people = new ArrayList<>();
		for(Person person : data.people) {
			people.add(new PersonSerializable(person));
		}
		this.debts = new ArrayList<>();
		for(Debt debt : data.debts) {
			debts.add(new DebtSerializable(debt));
		}

        this.deleted = data.deleted;
	}

	public AppData extract() {
		ArrayList<Person> people = new ArrayList<>();
		for(PersonSerializable person : this.people) {
			people.add(person.extract());
		}

		ArrayList<Debt> debts = new ArrayList<>();
		for(DebtSerializable debt : this.debts) {
			debts.add(debt.extract(people));
		}
		return new AppData(people, debts, this.deleted);
	}
}
