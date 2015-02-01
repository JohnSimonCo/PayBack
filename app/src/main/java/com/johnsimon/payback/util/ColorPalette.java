package com.johnsimon.payback.util;

import android.content.res.Resources;
import android.util.SparseIntArray;

import com.johnsimon.payback.R;
import com.johnsimon.payback.core.DataActivityInterface;
import com.johnsimon.payback.data.Person;

public class ColorPalette {
	private static ColorPalette instance = null;

    private DataActivityInterface dataAcitivity;

	private int[] palette;
	public ColorPalette(DataActivityInterface dataAcitivity) {
        this.dataAcitivity = dataAcitivity;

        Resources resources = dataAcitivity.getContext().getResources();
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

	public int nextIndex() {
		int[] usedIndices = new int[palette.length];

		for (Person person : dataAcitivity.getData().people) {
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

	public static ColorPalette getInstance(DataActivityInterface dataAcitivity) {
		if(instance == null) {
			instance = new ColorPalette(dataAcitivity);
		}

		instance.dataAcitivity = dataAcitivity;

		return instance;
	}
}
