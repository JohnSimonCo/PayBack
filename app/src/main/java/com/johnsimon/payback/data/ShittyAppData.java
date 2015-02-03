package com.johnsimon.payback.data;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.johnsimon.payback.currency.UserCurrency;
import com.johnsimon.payback.preferences.Preference;
import com.johnsimon.payback.preferences.Preferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

public class ShittyAppData {
	public ArrayList<ShittyPerson> people;
	public ArrayList<ShittyDebt> debts;

	public HashSet<UUID> deleted;

	public PeopleOrder peopleOrder;

	public ShittyPreferences preferences;

	public static AppData fromJson(String JSON) {
		try {
			AppData data = new Gson().fromJson(JSON, AppData.class);

			for(Debt debt : data.debts) {
				if(debt.ownerId == null) {
					throw new Exception("Data is shitty!");
				}
			}

			for(Person person : data.people) {
				if(person.name == null) {
					throw new Exception("Data is shitty!");
				}
			}

			return data;
		} catch (Exception e) {
			return new Gson().fromJson(JSON, ShittyAppData.class).clean();
		}
	}

	public AppData clean() {
		ArrayList<Person> people = new ArrayList<>();

		for(ShittyPerson shittyPerson : this.people) {
			people.add(new Person(shittyPerson.name, shittyPerson.id, shittyPerson.paletteIndex, shittyPerson.touched));
		}

		ArrayList<Debt> debts = new ArrayList<>();

		for(ShittyDebt shittyDebt : this.debts) {
			debts.add(new Debt(shittyDebt.ownerId, shittyDebt.amount, shittyDebt.note, shittyDebt.id, shittyDebt.timestamp, shittyDebt.touched, shittyDebt.paidback, shittyDebt.currencyId));
		}

		Preferences preferences = new Preferences();

		preferences.background = new Preference<>(this.preferences.background.value);
		preferences.currency = new Preference<>(this.preferences.currency.value);

		return new AppData(people, debts, deleted, peopleOrder, 0, preferences);
	}

	private static class ShittyPerson {
		@SerializedName("a")
		public String name;

		public UUID id;
		public int paletteIndex;
		public long touched;
	}

	private static class ShittyDebt {
		@SerializedName("b")
		public UUID ownerId;

		@SerializedName("c")
		public float amount;

		@SerializedName("d")
		public String note;

		@SerializedName("e")
		public boolean paidback;

		public UUID id;
		public String currencyId;
		public long timestamp;
		public long touched;
	}

	private static class ShittyPreferences {
		ShittyPreference<String> background;
		ShittyPreference<UserCurrency> currency;
	}

	private static class ShittyPreference<T> {
		@SerializedName("a")
		public T value;

		public long touched;
	}
}
