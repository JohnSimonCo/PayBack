package com.johnsimon.payback;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by John on 2014-06-04.
 */
public class AppData {
	public ArrayList<Person> people;
	public ArrayList<Debt> debts;

	public AppData() {
		this.people = new ArrayList<Person>();
		this.debts = new ArrayList<Debt>();
	}

	public AppData(ArrayList people, ArrayList debts) {
		this.people = people;
		this.debts = debts;
	}

	public ArrayList<Debt> personalizedFeed(Person person) {
		ArrayList<Debt> result = new ArrayList<Debt>();
		for(Debt debt : debts) {
			if(debt.owner == person)
				result.add(debt);
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

	public Person findPerson(UUID id) {
		return findPerson(people, id);
	}

	public static Person findPerson(ArrayList<Person> people, UUID id) {
		for(Person p : people)
			if(p.id.equals(id)) return p;
		return null;
	}

	public Person findPerson(String name) {
		return findPerson(people, name);
	}

	public static Person findPerson(ArrayList<Person> people, String name) {
		for(Person p : people)
			if(p.name.equals(name)) return p;
		return null;
	}

    public ArrayList<String> peopleArray() {
        ArrayList<String> result = new ArrayList<String>();
        for(Person p : people) {
			result.add(p.toString());
		}
        return result;
    }
}
