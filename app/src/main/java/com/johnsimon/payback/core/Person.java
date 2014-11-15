package com.johnsimon.payback.core;

import com.johnsimon.payback.util.ColorPalette;

import java.util.UUID;

public class Person {
	public String name;
	public UUID id;
	public Contact link;
	public int color;

	//Used for deserialization
	public Person(String name, UUID id, Contact link, Integer color) {
		this.name = name;
		this.id = id;
		this.link = link;
		this.color = color;
	}
	//Used for creating a person with a contact link
	public Person(String name, Contact link, ColorPalette palette) {
		this(name, UUID.randomUUID(), link, palette.nextColor());
	}
	//Used for creating a person without a contact link
	public Person(String name, ColorPalette palette) {
		this(name, null, palette);
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
}
