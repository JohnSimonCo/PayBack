package com.johnsimon.payback.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;

public class PeopleOrder extends ArrayList<UUID> {

	public long touched;

	public PeopleOrder() {
		this.touched = System.currentTimeMillis();
	}

	public PeopleOrder(ArrayList<Person> people) {
        this();

        for(Person person : people) {
            add(person.id);
        }
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

	private SortResult sort(ArrayList<Person> people, Comparator<Person> comparator) {
        ArrayList<Person> copy = new ArrayList<>();
		Collections.sort(copy, comparator);
		return new SortResult(copy, new PeopleOrder(people));
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
