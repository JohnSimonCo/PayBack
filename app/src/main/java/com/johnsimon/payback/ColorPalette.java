package com.johnsimon.payback;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by John on 2014-09-23.
 */
public class ColorPalette {
	private static ColorPalette instance = null;

	private int[] palette;
	public ColorPalette(Resources resources) {
		palette = new int[] {
			resources.getColor(R.color.color1),
			resources.getColor(R.color.color2),
			resources.getColor(R.color.color3)
		};
	}

	public int nextColor() {
		ArrayList<Integer> usedColors = new ArrayList<Integer>(palette.length);
		for (int i = 0; i < palette.length; i++) {
			usedColors.add(i, 0);
		}

		for (Person person : Resource.people) {
			if(person.color != null) {
				int index = usedColors.indexOf(person.color);
				usedColors.set(index, usedColors.get(index) + 1);
			}
		}

		int index = 0, smallest = usedColors.get(0);

		for (int i = 1; i < usedColors.size(); i++) {
			int count = usedColors.get(i);
			if(count < smallest) {
				smallest = count;
				index = i;
			}
		}

		return palette[index];
	}

	public static ColorPalette getInstance(Context context) {
		if(instance != null) {
			return instance;
		} else {
			return (instance = new ColorPalette(context.getResources()));
		}
	}
}
