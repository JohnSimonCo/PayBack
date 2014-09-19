package com.johnsimon.payback;

import java.util.UUID;

public class Person {
	public String name;
	public UUID id;

	public Person(String name, UUID id) {
		this.name = name;
		this.id = id;
	}

	@Override
	public String toString() {
		return name;
	}
}
