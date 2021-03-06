package com.johnsimon.payback.data;

import com.johnsimon.payback.preferences.Preferences;
import com.johnsimon.payback.util.Resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;

public class DataSyncer {
    public static boolean sync(AppData a, AppData b, AppData out) {
        boolean changed = false;

        sortDebts(a.debts);
        sortDebts(b.debts);

        ArrayList<Person> people = a.people;
        ArrayList<Debt> debts = a.debts;
        HashSet<UUID> deleted = a.deleted;
		PeopleOrder peopleOrder = a.peopleOrder;
		long peopleOrderTouched = a.peopleOrderTouched;
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
            sortDebts(debts);

            for(Debt debt : debts) {
                debt.linkOwner(people);
            }

			peopleOrder = mergePeopleOrder(a, b);

        } else if(!a.peopleOrder.equals(b.peopleOrder)) {
            changed = true;

			peopleOrder = a.peopleOrderTouched > b.peopleOrderTouched ? a.peopleOrder : b.peopleOrder;
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
			out.peopleOrderTouched = peopleOrderTouched;
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

    public static <T extends Identifiable> ArrayList<T> union(ArrayList<T> a, ArrayList<T> b) {
        ArrayList<T> array = new ArrayList<>();

        array.addAll(a);
        for(T item : b) {
            T other = find(array, item.getId());
            if(other == null) {
                array.add(item);
            }
        }

        return array;
    }

	private static PeopleOrder mergePeopleOrder(AppData a, AppData b) {
		//lord has priority over peasant
		PeopleOrder lord, peasant;

		//lord is the the most recently changed
		if(a.peopleOrderTouched > b.peopleOrderTouched) {
			lord = a.peopleOrder;
			peasant = b.peopleOrder;
		} else {
			lord = b.peopleOrder;
			peasant = a.peopleOrder;
		}

		for(UUID id : peasant) {
			if(!lord.contains(id)) {
				lord.add(id);
			}
		}

        HashSet<UUID> existingPeople = new HashSet<>();
        for(Person p : a.people) {
            existingPeople.add(p.id);
        }
        for (Person p: b.people) {
            existingPeople.add(p.id);
        }

        Iterator<UUID> iterator = lord.iterator();
        while (iterator.hasNext()) {
            UUID id = iterator.next();
            if(!existingPeople.contains(id)) {
                iterator.remove();
            }
        }

		return lord;
	}

    private static void sortDebts(ArrayList<Debt> debts) {
        Collections.sort(debts, new Resource.TimeComparator());
    }
}
