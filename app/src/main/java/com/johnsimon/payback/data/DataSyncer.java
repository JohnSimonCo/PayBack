package com.johnsimon.payback.data;

import com.johnsimon.payback.preferences.Preference;
import com.johnsimon.payback.preferences.Preferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class DataSyncer {
    public static boolean sync(AppData a, AppData b, AppData out) {
        boolean changed = false;

        ArrayList<Person> people = a.people;
        ArrayList<Debt> debts = a.debts;
        HashSet<UUID> deleted = a.deleted;
        PeopleOrder peopleOrder = a.peopleOrder;
        Preferences preferences = a.preferences;

        if(!a.people.equals(b.people) || !a.debts.equals(b.debts) || !a.deleted.equals(b.deleted)) {
            changed = true;

            deleted.addAll(b.deleted);

            removeDeleted(a.people, deleted);
            removeDeleted(b.people, deleted);
            removeDeleted(a.debts, deleted);
            removeDeleted(b.debts, deleted);

            people = sync(a.people, b.people);
            debts = sync(a.debts, b.debts);
        }

        if(!a.peopleOrder.equals(b.peopleOrder)) {
            changed = true;

            peopleOrder = a.peopleOrder.syncWith(b.peopleOrder);
        }

        if(!a.preferences.equals(b.preferences)) {
            changed = true;

			preferences.background = a.preferences.background.syncWith(b.preferences.background);
			preferences.currency = a.preferences.currency.syncWith(b.preferences.currency);
        }

		if(changed) {
			out.people = people;
			out.debts = debts;
			out.deleted = deleted;
			out.peopleOrder = peopleOrder;
			out.preferences = preferences;
		}
        return changed;
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

}
