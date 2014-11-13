package com.johnsimon.payback.util;

import com.johnsimon.payback.core.Contact;
import com.johnsimon.payback.core.Debt;
import com.johnsimon.payback.core.Person;

import java.util.ArrayList;
import java.util.UUID;

public class AppData {
	public ArrayList<Person> people;
	public ArrayList<Debt> debts;

	public AppData() {
		this.people = new ArrayList<Person>();
		this.debts = new ArrayList<Debt>();
	}

	public AppData(ArrayList<Person> people, ArrayList<Debt> debts) {
		this.people = people;
		this.debts = debts;
	}

	public ArrayList<Debt> personalizedFeed(Person person) {
		ArrayList<Debt> result = new ArrayList<Debt>();
		for(Debt debt : debts) {
			if(debt.owner == person) {
				result.add(debt);
			}
		}
		return result;
	}

	public static int totalDebt(ArrayList<Debt> debts) {
		int total = 0;
		for(Debt debt : debts) {
			if(!debt.isPaidBack) {
				total += debt.amount;
			}
		}
		return total;
	}

	public int calculateTotalPlus() {
		int sum = 0;
		for (int i = 0; i < debts.size(); i++) {
			if (debts.get(i).amount > 0) {
				sum += debts.get(i).amount;
			}
		}
		return sum;
	}

	public int calculateTotalMinus() {
		int sum = 0;
		for (int i = 0; i < debts.size(); i++) {
			if (debts.get(i).amount < 0) {
				sum += debts.get(i).amount;
			}
		}
		return sum;
	}

	public Person findPerson(UUID id) {
		return findPerson(people, id);
	}
	public Person findPerson(String id) {
		return findPerson(people, UUID.fromString(id));
	}

	public static Person findPerson(ArrayList<Person> people, UUID id) {
		for(Person p : people) {
			if(p.id.equals(id)) return p;
		}
		return null;
	}

	public Person findPersonByName(String name) {
		for(Person p : people) {
			if(p.name.equals(name)) return p;
		}
		return null;
	}

	public Debt findDebt(long timestamp) {
		for (Debt debt : debts) {
			if(debt.timestamp == timestamp) return debt;
		}

		return null;
	}

	public void merge(Person from, Person to) {
		for(Debt debt : debts) {
			if(debt.owner == from) {
				debt.owner = to;
			}
		}
		people.remove(from);
	}

	public void unmerge(Person restore, ArrayList<Debt> debts, int index) {
		for(Debt debt : debts) {
			debt.owner = restore;
		}
		people.add(index, restore);
	}
	public void delete(Person person) {
		//#perfmatters
		int push = -1;
		Debt[] remove = new Debt[debts.size()];
		for(Debt debt : debts) {
			if(debt.owner == person) {
				remove[++push] = debt;
			}
		}

		for(int i = 0; i < push; i++) {
			debts.remove(remove[i]);
		}

		//Tell GC to clean #memorymanagement
		remove = null;

		people.remove(person);
	}

	public void move(Debt debt, Person person) {
		debt.owner = person;
	}

	public void rename(Person person, String name) {
		person.name = name;
	}

}
