package com.johnsimon.payback.data;

import android.provider.Contacts;

import com.johnsimon.payback.util.Resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
		ArrayList<Person> list = new ArrayList<>();

		for(UUID id : this) {
			for(Person person : people) {
				if(person.id.equals(id)) {
					list.add(person);
					break;
				}
			}
		}
		return list;
	}

	private PeopleOrder reorder(Comparator<UUID> comparator) {
		PeopleOrder copy = new PeopleOrder(this);
		Collections.sort(copy, comparator);
		return copy;
	}

	public PeopleOrder reorderAlphabetically(ArrayList<Person> people) {
		return reorder(new AlphabeticalComparator(people));
	}

	private static Person find(ArrayList<Person> people, UUID id) {
		for(Person person : people) {
			if(person.id.equals(id)) {
				return person;
			}
		}
		return null;
	}

	public static class AlphabeticalComparator implements Comparator<UUID> {
		public ArrayList<Person> people;
		public AlphabeticalComparator(ArrayList<Person> people) {
			this.people = people;
		}

		@Override
		public int compare(UUID lhs, UUID rhs) {
			Person a = find(people, lhs), b = find(people, rhs);
			return a.getName().compareToIgnoreCase(b.getName());
		}
	}
}
