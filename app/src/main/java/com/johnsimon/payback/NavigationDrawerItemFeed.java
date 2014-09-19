package com.johnsimon.payback;

import android.graphics.Bitmap;

import java.util.UUID;

/**
 * Created by John on 2014-09-20.
 */
public class NavigationDrawerItemFeed extends NavigationDrawerItem {
	public String title;
	public UUID personId;
	public Bitmap image;

	public NavigationDrawerItemFeed(String title, UUID personId, Bitmap image) {
		this.type = Type.Person;
		this.title = title;
		this.image = image;
		this.personId = personId;
	}

	public NavigationDrawerItemFeed() {
		this.type = Type.All;
	}
}
