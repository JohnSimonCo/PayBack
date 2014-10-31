package com.johnsimon.payback.core;

import com.johnsimon.payback.util.ColorPalette;

import java.util.UUID;

public class Person {
	public String name;
	public UUID id;
	public String photoURI;
	public Integer color;

	public Person(String name, String photoURI, Integer color, UUID id) {
		this.name = name;
		this.id = id;
		this.photoURI = photoURI;
		this.color = color;
	}
	private Person(String name, String photoURI, Integer color) {
		this(name, photoURI, color, UUID.randomUUID());
	}
	public Person(String name, String photoURI) {
		this(name, photoURI, null);
	}
	public Person(String name, ColorPalette palette) {
		this(name, null, palette.nextColor());
	}

	@Override
	public String toString() {
		return name;
	}
	public String getAvatarLetter() {
		return name.substring(0, 1).toUpperCase();
	}
}
