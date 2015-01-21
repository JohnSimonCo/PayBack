package com.johnsimon.payback.data;

import com.johnsimon.payback.preferences.Preference;
import com.johnsimon.payback.preferences.Preferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class DataSyncer {
    public static AppData sync(AppData a, AppData b) {
        //TODO bara synka det som skiljer sig
        HashSet<UUID> deleted = new HashSet<>();
        deleted.addAll(a.deleted);
        deleted.addAll(b.deleted);

        removeDeleted(a.people, deleted);
        removeDeleted(b.people, deleted);
        removeDeleted(a.debts, deleted);
        removeDeleted(b.debts, deleted);

        ArrayList<Person> people = sync(a.people, b.people);
        ArrayList<Debt> debts = sync(a.debts, b.debts);

		PeopleOrder peopleOrder = a.peopleOrder.syncWith(b.peopleOrder);

		Preferences preferences = syncPreferences(a.preferences, b.preferences);

        return new AppData(people, debts, deleted, peopleOrder, preferences);
    }

    private static <T extends Identifiable> void removeDeleted(ArrayList<T> array, HashSet<UUID> deleted) {
        for(Iterator<T> iterator = array.iterator(); iterator.hasNext();) {
            if(deleted.contains(iterator.next().getId())) {
                iterator.remove();
            }
        }
    }

    private static <T extends Identifiable> T find(ArrayList<T> array, UUID id) {
        for(T item : array) {
            if(item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    private static <T extends SyncedData<T> & Identifiable> ArrayList<T> sync(ArrayList<T> a, ArrayList<T> b) {
        ArrayList<T> array = new ArrayList<>();

        array.addAll(a);
        for(T item : b) {
            T other = find(array, item.getId());
            if(other != null) {
                array.remove(other);
                array.add(item.syncWith(other));
            } else {
                array.add(item);
            }
        }

        return array;
    }

	private static Preferences syncPreferences(Preferences a, Preferences b) {
		Preferences preferences = new Preferences();
		for(Map.Entry<String, Preference> entry : a.entrySet()) {
			String key = entry.getKey();
			Preference aValue = entry.getValue(), bValue = b.get(key);
			preferences.put(key, aValue.equals(bValue) ? aValue : (Preference) aValue.syncWith(bValue));
		}
		return preferences;
	}
}
