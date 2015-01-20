package com.johnsimon.payback.core;

import com.johnsimon.payback.data.Person;
import com.johnsimon.payback.data.User;

public class Contact {
	public String name;
	public String[] numbers;
	public String photoURI;
	public long id;

	public Contact(String name, String photoURI, long id) {
		this.name = name;
		this.photoURI = photoURI;
		this.id = id;
	}

    public void setNumbers(String[] numbers) {
        this.numbers = numbers;
    }

	public boolean hasNumbers() {
		return numbers != null;
	}

	public boolean matchTo(User user) {
		return this.name.equals(user.name)
			|| (hasNumbers() && user.hasNumbers() && matchNumbers(user.numbers));
	}

	public boolean matchTo(Person person) {
		return this.name.equals(person.getName());
	}

	private boolean matchNumbers(String[] numbers) {
		for(String a : this.numbers) {
			for(String b : numbers) {
				if(a.equals(b)) return true;
			}
		}
		return false;
	}
}
