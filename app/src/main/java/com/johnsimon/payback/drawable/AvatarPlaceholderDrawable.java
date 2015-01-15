package com.johnsimon.payback.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;

import com.johnsimon.payback.core.DataActivityInterface;
import com.johnsimon.payback.util.ColorPalette;

public class AvatarPlaceholderDrawable extends Drawable {
	private int color;
	public AvatarPlaceholderDrawable(DataActivityInterface dataActivity, int paletteIndex) {
		this.color = ColorPalette.getInstance(dataActivity).getColor(paletteIndex);
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
