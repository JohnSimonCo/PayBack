package com.johnsimon.payback.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;

public class PeopleOrder extends ArrayList<UUID> {

	private PeopleOrder() {
	}

	public PeopleOrder(ArrayList<Person> people) {
        for(Person person : people) {
            add(person.id);
        }
	}

	public static PeopleOrder defaultPeopleOrder() {
		return new PeopleOrder();
	}

	public ArrayList<Person> order(ArrayList<Person> people) {
		ArrayList<Person> list = new ArrayList<>(people);
        Collections.sort(list, new OrderComparator());
        return list;
	}

    public void reorder(int from, int to, boolean toLast) {
        UUID item = get(from);
        remove(from);
        if (toLast) {
            add(item);
        } else {
            add(to, item);
        }
    }
	private SortResult sort(ArrayList<Person> people, Comparator<Person> comparator) {
        ArrayList<Person> copy = new ArrayList<>(people);
		Collections.sort(copy, comparator);
		return new SortResult(copy, new PeopleOrder(copy));
	}

	public SortResult sortAlphabetically(ArrayList<Person> people) {
		return sort(people, new AlphabeticalComparator());
	}

    private PeopleOrder order = this;

    public class OrderComparator implements Comparator<Person> {
        @Override
        public int compare(Person a, Person b) {
            return order.indexOf(a.id) - order.indexOf(b.id);
        }
    }

	public static class AlphabeticalComparator implements Comparator<Person> {
		@Override
		public int compare(Person a, Person b) {
			return a.getName().compareToIgnoreCase(b.getName());
		}
	}

    public static class SortResult {
        public ArrayList<Person> people;
        public PeopleOrder order;

        public SortResult(ArrayList<Person> people, PeopleOrder order) {
            this.people = people;
            this.order = order;
        }
    }
}
