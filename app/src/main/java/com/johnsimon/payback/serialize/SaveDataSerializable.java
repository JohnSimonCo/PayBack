package com.johnsimon.payback.serialize;

import com.johnsimon.payback.core.Debt;
import com.johnsimon.payback.core.Person;
import com.johnsimon.payback.util.SaveData;

import java.util.ArrayList;

public class SaveDataSerializable {
	public ArrayList<PersonSerializable> people;
	public ArrayList<DebtSerializable> debts;

	public SaveDataSerializable(SaveData data) {
		this.people = new ArrayList<PersonSerializable>();
		for(Person person : data.people) {
			people.add(new PersonSerializable(person));
		}
		this.debts = new ArrayList<DebtSerializable>();
		for(Debt debt : data.debts) {
			debts.add(new DebtSerializable(debt));
		}
	}

	public SaveData extract() {
		ArrayList<Person> people = new ArrayList<Person>();
		for(PersonSerializable person : this.people) {
			people.add(person.extract());
		}

		ArrayList<Debt> debts = new ArrayList<Debt>();
		for(DebtSerializable debt : this.debts) {
			debts.add(debt.extract(people));
		}
		return new SaveData(people, debts);
	}
}
