package com.johnsimon.payback.core;

public class Contact {
	public String name;
	public String number;
	public String photoURI;
	public long id;

	public Contact(String name, String number, String photoURI, long id) {
		this.name = name;
		this.number = number;
		this.photoURI = photoURI;
		this.id = id;
	}

	public boolean matchTo(User user) {
		return this.name.equals(user.name) || this.number.equals(user.number);
	}
}
