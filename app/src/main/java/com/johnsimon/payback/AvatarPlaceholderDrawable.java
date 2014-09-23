package com.johnsimon.payback;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;

public class AvatarPlaceholderDrawable extends Drawable {

	private Context ctx;
	private int[] palette = new int[] {get(R.color.color1)};

	public AvatarPlaceholderDrawable(Context context) {
		this.ctx = context;
	}

	@Override
	public void draw(Canvas canvas) {

	}

	@Override
	public void setAlpha(int alpha) {

	}

	@Override
	public void setColorFilter(ColorFilter cf) {

	}

	@Override
	public int getOpacity() {
		return 0;
	}

	private int get(int id) {
		return ctx.getResources().getColor(id);
	}

}
