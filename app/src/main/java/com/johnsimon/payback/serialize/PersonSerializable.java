package com.johnsimon.payback.serialize;

import com.johnsimon.payback.core.Contact;
import com.johnsimon.payback.core.Person;

import java.util.ArrayList;
import java.util.UUID;

public class PersonSerializable {
	public String name;
	public UUID id;
	public Integer color;

	public PersonSerializable(Person person) {
		this.name = person.name;
		this.id = person.id;
		this.color = person.color;
	}

	public Person extract() {
		return new Person(name, id, color);
	}
}
