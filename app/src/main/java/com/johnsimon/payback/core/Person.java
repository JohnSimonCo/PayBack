package com.johnsimon.payback.core;

import com.johnsimon.payback.util.ColorPalette;

import java.util.UUID;

public class Person implements Syncable<Person> {
	public String name;
	public UUID id;
	public int color;
    public Contact link = null;

	//Used for deserialization
	public Person(String name, UUID id, Integer color) {
		this.name = name;
		this.id = id;
		this.color = color;
	}
	//Used for creating a person
	public Person(String name, ColorPalette palette) {
		this(name, UUID.randomUUID(), palette.nextColor());
	}

	public boolean isLinked() {
		return link != null;
	}
	public boolean hasImage() {
		return isLinked() && link.photoURI != null;
	}

	@Override
	public String toString() {
		return name;
	}
	public String getAvatarLetter() {
		return name.substring(0, 1).toUpperCase();
	}

	public boolean matchTo(User user) {
		return this.name.equals(user.name);
	}

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public Person syncWith(Person other) {
        return Person.sync(this, other);
    }

    public static Person sync(Person a, Person b) {
        return a;
    }
}
