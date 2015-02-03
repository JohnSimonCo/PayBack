package com.johnsimon.payback.data;

import com.johnsimon.payback.core.Contact;
import com.johnsimon.payback.util.ColorPalette;

import java.util.UUID;

public class Person extends SyncedData<Person> implements Identifiable {
    public final UUID id;
	public String name;
	public final int paletteIndex;
    public transient Contact link = null;

	public Person(String name, UUID id, int paletteIndex, long touched) {
		super(touched);

        this.id = id;
		this.name = name;
		this.paletteIndex = paletteIndex;
	}
	//Used for creating a person
	public Person(String name, ColorPalette palette) {
		this(name, UUID.randomUUID(), palette.nextIndex(), System.currentTimeMillis());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		touch();
		this.name = name;
	}

	public void linkTo(Contact link) {
		this.link = link;
	}

	public boolean isLinked() {
		return link != null;
	}
	public boolean hasImage() {
		return isLinked() && link.photoURI != null;
	}
	public boolean hasNumbers() {
		return isLinked() && link.hasNumbers();
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
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (!(o instanceof Person)) return false;
		Person other = (Person) o;

		return id.equals(other.id)
			&& name.equals(other.name);
	}

	@Override
	public String toString() {
		return name;
	}
}
