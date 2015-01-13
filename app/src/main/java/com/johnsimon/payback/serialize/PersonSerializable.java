package com.johnsimon.payback.serialize;

import com.johnsimon.payback.core.Person;

public class PersonSerializable extends SyncedDataSerializable {
	public String name;
	public Integer color;

	public PersonSerializable(Person person) {
		super(person.id, person.touched);

		this.name = person.getName();
		this.color = person.color;
	}

	public Person extract() {
		return new Person(name, id, color, touched);
	}
}
