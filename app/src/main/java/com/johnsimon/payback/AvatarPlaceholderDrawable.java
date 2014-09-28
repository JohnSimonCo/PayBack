package com.johnsimon.payback;

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

	public Bitmap toBitmap(int widthPixels, int heightPixels) {
		Bitmap mutableBitmap = Bitmap.createBitmap(widthPixels, heightPixels, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(mutableBitmap);
		setBounds(0, 0, widthPixels, heightPixels);
		draw(canvas);

		return mutableBitmap;
	}
}
