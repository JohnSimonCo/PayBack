package com.johnsimon.payback.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by John on 2015-01-20.
 */
public class PeopleOrder extends ArrayList<UUID> {

	public long touched;

	public PeopleOrder() {
		this.touched = System.currentTimeMillis();
	}

	public PeopleOrder(PeopleOrder other) {
		super(other);
		this.touched = System.currentTimeMillis();
	}

	/*protected void touch() {
		touched = System.currentTimeMillis();
	}*/

	public PeopleOrder syncWith(PeopleOrder other) {
		return this.touched > other.touched ? this : other;
	}

	public ArrayList<Person> order(ArrayList<Person> people) {
		ArrayList<Person> list = new ArrayList<>(people);
        Collections.sort(list, new OrderComparator());
        return list;
	}

	private PeopleOrder reorder(Comparator<UUID> comparator) {
		PeopleOrder copy = new PeopleOrder(this);
		Collections.sort(copy, comparator);
		return copy;
	}

    private HashMap<UUID, Person> createMap(ArrayList<Person> people) {
        HashMap<UUID, Person> map = new HashMap<>(people.size());
        for(Person person : people) {
            map.put(person.id, person);
        }
        return map;
    }

	public PeopleOrder reorderAlphabetically(ArrayList<Person> people) {
		return reorder(new AlphabeticalComparator(createMap(people)));
	}

    private PeopleOrder order = this;

    public class OrderComparator implements Comparator<Person> {
        @Override
        public int compare(Person a, Person b) {
            return order.indexOf(a.id) - order.indexOf(b.id);
        }
    }

	public static class AlphabeticalComparator implements Comparator<UUID> {
		public HashMap<UUID, Person> peopleMap;
		public AlphabeticalComparator(HashMap<UUID, Person> peopleMap) {
			this.peopleMap = peopleMap;
		}

		@Override
		public int compare(UUID lhs, UUID rhs) {
			Person a = peopleMap.get(lhs), b = peopleMap.get(rhs);
			return a.getName().compareToIgnoreCase(b.getName());
		}
	}
}
