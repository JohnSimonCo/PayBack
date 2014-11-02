package com.johnsimon.payback.drawable;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;

public class AvatarPlaceholderDrawable extends Drawable {
	private int color;
	public AvatarPlaceholderDrawable(int color) {
		this.color = color;
	}
	@Override
	public void draw(Canvas canvas) {
		canvas.drawColor(color);
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
		//return PixelFormat.OPAQUE;
	}

}
