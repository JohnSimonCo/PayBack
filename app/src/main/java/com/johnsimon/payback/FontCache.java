package com.johnsimon.payback;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;

public class FontCache {
	private static HashMap<String, Typeface> cache = new HashMap<String, Typeface>();

	public static Typeface get(Context ctx, String fontName) {
		if(cache.containsKey(fontName)) {
			return cache.get(fontName);
		} else {
			Typeface face = Typeface.createFromAsset(ctx.getAssets(), fontName);
			cache.put(fontName, face);
			return face;
		}
	}
}
