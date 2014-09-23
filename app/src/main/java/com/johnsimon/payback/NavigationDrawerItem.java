package com.johnsimon.payback;

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

	public NavigationDrawerItem(String title, UUID personId, Bitmap image) {
		this.type = Type.Person;
		this.title = title;
		this.image = image;
		this.personId = personId;
	}
	public NavigationDrawerItem(Type type) {
		this.type = type;
	}
}
