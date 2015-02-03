package com.johnsimon.payback.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;

public class PeopleOrder {

	//TODO verkar inte synka som den ska

	private ArrayList<UUID> order;
	private long touched;

	private PeopleOrder() {
	}

	public PeopleOrder(ArrayList<Person> people) {
		this.touched = System.currentTimeMillis();

        for(Person person : people) {
            order.add(person.id);
        }
	}

	public static PeopleOrder defaultPeopleOrder() {
		PeopleOrder peopleOrder = new PeopleOrder();
		peopleOrder.order = new ArrayList<>();
		peopleOrder.touched = System.currentTimeMillis();
		return peopleOrder;
	}

	protected void touch() {
		touched = System.currentTimeMillis();
	}

	public void remove(UUID id) {
		order.remove(id);
	}

	public void add(UUID id) {
		order.add(id);
	}

	public int size() {
		return order.size();
	}

	public PeopleOrder syncWith(PeopleOrder other) {
		return this.touched > other.touched ? this : other;
	}

	public ArrayList<Person> order(ArrayList<Person> people) {
		ArrayList<Person> list = new ArrayList<>(people);
        Collections.sort(list, new OrderComparator());
        return list;
	}

    public void reorder(int from, int to, boolean toLast) {
        touch();

		UUID item = order.get(from);
        order.remove(from);
        if (toLast) {
			order.add(item);
        } else {
			order.add(to, item);
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
