package com.johnsimon.payback.util;

import android.content.res.Resources;
import android.util.SparseIntArray;

import com.johnsimon.payback.R;
import com.johnsimon.payback.core.DataActivity;
import com.johnsimon.payback.core.DataActivityInterface;
import com.johnsimon.payback.core.Person;

import java.util.ArrayList;

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
		SparseIntArray usedIndices = new SparseIntArray(palette.length);

		for (Person person : dataAcitivity.getData().people) {
			usedIndices.put(person.paletteIndex, usedIndices.get(person.paletteIndex) + 1);
		}

		//Start at 0
		int index = usedIndices.keyAt(0), smallest = usedIndices.valueAt(0);
		//Proceed at 1
		for(int i = 1, length = usedIndices.size(); i < length; i++) {
			int value = usedIndices.valueAt(i);
			if(value < smallest) {
				smallest = value;
				index = usedIndices.keyAt(i);
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
