package com.johnsimon.payback.core;

public class Contact {
	public String name;
	public String number;
	public String photoURI;
	public long id;

	public Contact(String name, String photoURI, long id) {
		this.name = name;
		this.photoURI = photoURI;
		this.id = id;
	}

    public void setNumber(String number) {
        this.number = number;
    }

	private boolean hasNumber() {
		return number != null;
	}

	public boolean matchTo(User user) {
		return this.name.equals(user.name) || (hasNumber() && this.number.equals(user.number));
	}
}
