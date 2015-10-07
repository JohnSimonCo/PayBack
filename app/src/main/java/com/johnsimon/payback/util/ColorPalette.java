package com.johnsimon.payback.util;

import android.content.Context;
import android.content.res.Resources;

import com.johnsimon.payback.R;
import com.johnsimon.payback.core.DataContextInterface;
import com.johnsimon.payback.data.AppData;
import com.johnsimon.payback.data.Person;

public class ColorPalette {
	private static ColorPalette instance = null;

    private AppData data;

	private int[] palette;
	public ColorPalette(Context context) {
        Resources resources = context.getResources();
        palette = new int[] {
			resources.getColor(R.color.color1, null),
			resources.getColor(R.color.color2, null),
			resources.getColor(R.color.color3, null),
            resources.getColor(R.color.color4, null),
			resources.getColor(R.color.color5, null),
			resources.getColor(R.color.color6, null),
			resources.getColor(R.color.color7, null)
		};
	}

	public int nextIndex() {
		int[] usedIndices = new int[palette.length];

		for (Person person : data.people) {
			usedIndices[person.paletteIndex]++;
		}

		//Start at 0
		int index = 0, smallest = usedIndices[0];
		//Proceed at 1
		for(int i = 1, length = usedIndices.length; i < length; i++) {
			int value = usedIndices[i];
			if(value < smallest) {
				smallest = value;
				index = i;
			}
		}

		return index;
	}

	public int getColor(int index) {
		return palette[index];
	}

	public static ColorPalette getInstance(DataContextInterface dataContext) {
		if(instance == null) {
			instance = new ColorPalette(dataContext.getContext());
		}

		instance.data = dataContext.getData();

		return instance;
	}

	public static ColorPalette getInstanceWithContext(Context context) {
		if(instance == null) {
			instance = new ColorPalette(context);
		}

		instance.data = AppData.defaultAppData();

		return instance;
	}
}
