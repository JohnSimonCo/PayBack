package com.johnsimon.payback.core;

import android.graphics.Bitmap;

import java.util.UUID;

public class NavigationDrawerItem {
	public enum Type {
		All,
		Person
	}
	public Type type;

	public String title;
	public UUID personId;
	public Bitmap image;
	public Person owner;

	public NavigationDrawerItem(String title, UUID personId, Bitmap image, Person person) {
		this.type = Type.Person;
		this.title = title;
		this.image = image;
		this.personId = personId;
		this.owner = person;
	}
	public NavigationDrawerItem(Type type) {
		this.type = type;
	}
}
