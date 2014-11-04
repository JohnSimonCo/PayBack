package com.johnsimon.payback.serialize;

import com.johnsimon.payback.core.Contact;
import com.johnsimon.payback.core.Person;
import com.johnsimon.payback.util.ColorPalette;
import com.johnsimon.payback.util.Resource;

import java.util.ArrayList;
import java.util.UUID;

public class PersonSerializable {
	public String name;
	public UUID id;
	public long link;
	public Integer color;

	public PersonSerializable(Person person) {
		this.name = person.name;
		this.id = person.id;
		if(person.link != null) {
			this.link = person.link.id;
		}
		this.color = person.color;
	}

	public Person extract(ArrayList<Contact> contacts) {
		return new Person(name, id, link(contacts), color);
	}

	private Contact link(ArrayList<Contact> contacts) {
		for(Contact contact : contacts) {
			if(contact.id == link) return contact;
		}
		return null;
	}
}
