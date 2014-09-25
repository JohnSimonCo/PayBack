package com.johnsimon.payback;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;

public class AvatarPlaceholderDrawable extends Drawable {
	private String text;
	private int color;
	public AvatarPlaceholderDrawable(String text, int color) {
		this.text = text;
		this.color = color;
	}
	@Override
	public void draw(Canvas canvas) {
		canvas.drawColor(color);

		Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.FILL);
		paint.setTextSize(20);

		canvas.drawText(text, 10, 25, paint);
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
