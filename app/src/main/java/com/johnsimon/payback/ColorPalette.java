package com.johnsimon.payback;

import android.content.Context;
import android.content.res.Resources;
import android.util.SparseIntArray;

public class ColorPalette {
	private static ColorPalette instance = null;

	private int[] palette;
	public ColorPalette(Resources resources) {
		palette = new int[] {
			resources.getColor(R.color.color1),
			resources.getColor(R.color.color2),
			resources.getColor(R.color.color3),
            resources.getColor(R.color.color4),
            resources.getColor(R.color.color5)
		};
	}

	public int nextColor() {
		SparseIntArray usedColors = new SparseIntArray(palette.length);
		for(int color : palette) {
			usedColors.put(color, 0);
		}

		for (Person person : Resource.people) {
			if(person.color != null) {
				usedColors.put(person.color, usedColors.get(person.color) + 1);
			}
		}

		int color = 0, smallest = -1;
		//#perfmatters
		for(int i = 0, length = usedColors.size(); i < length; i++) {
			int value = usedColors.valueAt(i);
			if(smallest == -1 || value < smallest) {
				smallest = value;
				color = usedColors.keyAt(i);
			}
		}

		return color;
	}

	public static ColorPalette getInstance(Context context) {
		if(instance == null) {
			instance = new ColorPalette(context.getResources());
		}
		return instance;
	}
}
