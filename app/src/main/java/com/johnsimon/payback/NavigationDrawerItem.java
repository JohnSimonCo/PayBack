package com.johnsimon.payback;

import android.graphics.Bitmap;

import java.util.UUID;

public class NavigationDrawerItem {
	public enum Type {
		All,
		Person,
		Settings
	}
	public Type type;

	public NavigationDrawerItem(Type type) {
		this.type = type;
	}
	public NavigationDrawerItem() {
	}
}
