package com.johnsimon.payback;

import android.graphics.Bitmap;

import java.util.UUID;

public class NavigationDrawerItem {
	public boolean all = false;
	public String title;
	public UUID personId;
    public Bitmap image;

	public NavigationDrawerItem(String title, UUID personId, Bitmap image) {
		this.title = title;
		this.personId = personId;
        this.image = image;
	}

	protected NavigationDrawerItem() {

	}
}
