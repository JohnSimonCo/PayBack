package com.johnsimon.payback.util;

import android.content.res.Resources;
import android.util.SparseIntArray;

import com.johnsimon.payback.R;
import com.johnsimon.payback.core.DataActivity;
import com.johnsimon.payback.core.Person;

public class ColorPalette {
	private static ColorPalette instance = null;

    private DataActivity context;

	private int[] palette;
	public ColorPalette(DataActivity context) {
        this.context = context;

        Resources resources = context.getResources();
        palette = new int[] {
			resources.getColor(R.color.color1),
			resources.getColor(R.color.color2),
			resources.getColor(R.color.color3),
            resources.getColor(R.color.color4),
			resources.getColor(R.color.color5),
			resources.getColor(R.color.color6),
			resources.getColor(R.color.color7)
		};
	}

	public int nextColor() {
		SparseIntArray usedColors = new SparseIntArray(palette.length);
		for(int color : palette) {
			usedColors.put(color, 0);
		}

		for (Person person : context.data.people) {
			usedColors.put(person.color, usedColors.get(person.color) + 1);
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

	public static ColorPalette getInstance(DataActivity context) {
		if(instance == null) {
			instance = new ColorPalette(context);
		}
		return instance;
	}
}
