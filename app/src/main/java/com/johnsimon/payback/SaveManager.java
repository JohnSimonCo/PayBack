package com.johnsimon.payback;

import android.content.SharedPreferences;

import java.util.ArrayList;

/**
 * Created by John on 2014-06-04.
 */
public class SaveManager {

	/*private SharedPreferences preferences;

	private final static String PERSON = "PERSON";

	private final static String AMOUNT_ENTRIES = "AMOUNT_ENTRIES";
	private final static String AMOUNT_PEOPLE = "AMOUNT_PEOPLE";

	private final static String ENTRY_PERSON = "ENTRY_PERSON";
	private final static String ENTRY_AMOUNT = "ENTRY_AMOUNT";
	private final static String ENTRY_NOTE = "ENTRY_NOTE";

	SaveManager(SharedPreferences preferences) {
		this.preferences = preferences;
	}

	public AppData extract() {
		ArrayList<Debt> entries = new ArrayList<Debt>();
		ArrayList<Person> people = new ArrayList<Person>();
		int entryAmount = preferences.getInt(AMOUNT_ENTRIES, 0);
		int peopleAmount = preferences.getInt(AMOUNT_PEOPLE, 0);

		if(entryAmount > 0) {
			for(int i = 0; i < entryAmount; i++) {
				String person = preferences.getString(ENTRY_PERSON + i, null);
				int amount = preferences.getInt(ENTRY_AMOUNT + i, 0);
				String note = preferences.getString(ENTRY_NOTE + i, null);
				entries.add(new Debt(person, amount, note));
			}
		}

		if(peopleAmount > 0) {
			for(int i = 0; i < peopleAmount; i++) {
				people.add(new Person(preferences.getString(PERSON + i, null)));
			}
		}

		return new AppData(people, entries);
	}

	public void commit(AppData data) {
		int entryAmount = data.entries.size(), peopleAmount = data.people.size();

		SharedPreferences.Editor editor = preferences.edit()
			.clear()
			.putInt(AMOUNT_ENTRIES, entryAmount)
			.putInt(AMOUNT_PEOPLE, peopleAmount);

		for(int i = 0; i < entryAmount; i++) {
			Debt entry = data.entries.get(i);

			editor.putString(ENTRY_PERSON + i, entry.person);
			editor.putInt(ENTRY_AMOUNT + i, entry.amount);
			editor.putString(ENTRY_NOTE + i, entry.note);
		}

		for(int i = 0; i < peopleAmount; i++) {
			Person person = data.people.get(i);
			editor.putString(PERSON + i, person.name);
		}

		editor.commit();
	}*/

}