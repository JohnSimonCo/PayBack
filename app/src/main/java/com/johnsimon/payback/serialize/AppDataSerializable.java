package com.johnsimon.payback.serialize;

import com.johnsimon.payback.core.Contact;
import com.johnsimon.payback.core.Debt;
import com.johnsimon.payback.core.Person;
import com.johnsimon.payback.util.AppData;

import java.util.ArrayList;

public class AppDataSerializable {
	public ArrayList<PersonSerializable> people;
	public ArrayList<DebtSerializable> debts;

	public AppDataSerializable(AppData data) {
		this.people = new ArrayList<PersonSerializable>();
		for(Person person : data.people) {
			people.add(new PersonSerializable(person));
		}
		this.debts = new ArrayList<DebtSerializable>();
		for(Debt debt : data.debts) {
			debts.add(new DebtSerializable(debt));
		}
	}

	public AppData extract(ArrayList<Contact> contacts) {
		ArrayList<Person> people = new ArrayList<Person>();
		for(PersonSerializable person : this.people) {
			people.add(person.extract(contacts));
		}

		ArrayList<Debt> debts = new ArrayList<Debt>();
		for(DebtSerializable debt : this.debts) {
			debts.add(debt.extract(people));
		}
		return new AppData(people, debts);
	}
}
