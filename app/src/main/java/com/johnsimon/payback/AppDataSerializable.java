package com.johnsimon.payback;

import java.util.ArrayList;

/**
 * Created by John on 2014-09-02.
 */
public class AppDataSerializable {
	public ArrayList<Person> people;
	public ArrayList<DebtSerializable> debts;

	public AppDataSerializable(AppData data) {
		people = data.people;
		this.debts = new ArrayList<DebtSerializable>();
		for(Debt debt : data.debts) {
			debts.add(new DebtSerializable(debt));
		}
	}

	public AppData extract() {
		ArrayList<Debt> debts = new ArrayList<Debt>();
		for(DebtSerializable debt : this.debts) {
			debts.add(debt.extract(people));
		}
		return new AppData(people, debts);
	}
}
