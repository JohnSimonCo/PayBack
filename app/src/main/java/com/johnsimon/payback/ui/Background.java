package com.johnsimon.payback.ui;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.johnsimon.payback.R;

public class Background {
	private static int[] ids = {R.drawable.art, R.drawable.art_old};
	public Drawable getDrawable(Resources resources, int background) {
		return resources.getDrawable(ids[background]);
	}
	String getName(Resources resources, int background) {
		return resources.getStringArray(R.array.bg_entries)[background];
	}
}
