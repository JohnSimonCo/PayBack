package com.johnsimon.payback.data;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.johnsimon.payback.currency.UserCurrency;
import com.johnsimon.payback.preferences.Preference;
import com.johnsimon.payback.preferences.Preferences;
import com.johnsimon.payback.util.Resource;

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
			AppData data = Resource.gson().fromJson(JSON, AppData.class);

			if(data.debts.size() > 0) {
				if(data.debts.get(0).ownerId == null) {
					throw new Exception("Data is shitty!");
				}
			}

			if(data.people.size() > 0) {
				if(data.people.get(0).name == null) {
					throw new Exception("Data is shitty!");
				}
			}

			return data;
		} catch (Exception e) {
			try {
				return Resource.gson().fromJson(JSON, ShittyAppData.class).cleanse();
			} catch (Exception e1) {
				return AppData.defaultAppData();
			}
		}
	}

	public AppData cleanse() {
		ArrayList<Person> people = new ArrayList<>();

		for(ShittyPerson shittyPerson : this.people) {
			people.add(new Person(shittyPerson.name, shittyPerson.id, shittyPerson.paletteIndex, shittyPerson.touched));
		}

		ArrayList<Debt> debts = new ArrayList<>();

		for(ShittyDebt shittyDebt : this.debts) {
			debts.add(new Debt(shittyDebt.ownerId, shittyDebt.amount, shittyDebt.note, shittyDebt.id, shittyDebt.timestamp, shittyDebt.touched, shittyDebt.paidback, null, shittyDebt.currencyId));
		}


		Preferences preferences = new Preferences();

		preferences.background = new Preference<>(this.preferences.background == null ? null : this.preferences.background.value);
		preferences.currency = new Preference<>(this.preferences.currency == null ? null : this.preferences.currency.value);

		return new AppData(people, debts, deleted, peopleOrder, 0, preferences);
	}

	private static class ShittyPerson {
		@SerializedName("a")
		public String name;

		@SerializedName("id")
		public UUID id;

		@SerializedName("palletteIndex")
		public int paletteIndex;

		@SerializedName("touched")
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

		@SerializedName("id")
		public UUID id;

		@SerializedName("currencyId")
		public String currencyId;

		@SerializedName("timestamp")
		public long timestamp;

		@SerializedName("touched")
		public long touched;
	}

	private static class ShittyPreferences {
		@SerializedName("background")
		ShittyPreference<String> background;

		@SerializedName("currency")
		ShittyPreference<UserCurrency> currency;
	}

	private static class ShittyPreference<T> {
		@SerializedName("a")
		public T value;

		@SerializedName("touched")
		public long touched;
	}
}
