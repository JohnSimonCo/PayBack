package com.johnsimon.payback.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import com.johnsimon.payback.util.ColorPalette;

public class AvatarPlaceholderDrawable extends Drawable {
	private int color;
	public AvatarPlaceholderDrawable(ColorPalette colorPalette, int paletteIndex) {
		this.color = colorPalette.getColor(paletteIndex);
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
	}

}
