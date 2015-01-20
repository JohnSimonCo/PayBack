package com.johnsimon.payback.preferences;

import java.util.HashMap;

/**
 * Created by johnrs on 2015-01-17.
 */
public class Preferences extends HashMap<String, Preference> {

	private final static String DEFAULT_CURRENCY = "$";

	private final static String[] KEYS = {
		"background", "currency"
	};

	public static Preferences defaultPreferences() {
		Preferences preferences = new Preferences();
		for(String key : KEYS) {
			preferences.put(key, new Preference<>(null));
		}
		return preferences;
	}

	private <T> Preference<T> getTyped(String key) {
		return (Preference<T>) get(key);
	}

	public <T> void set(String key, T value) {
		getTyped(key).setValue(value);
	}

	public <T> T get(String key, T defaultValue) {
		Preference preference = get(key);
		T value = (T) preference.getValue();
		return value == null ? defaultValue : value;
	}


	public String getCurrency() {
		return get("currency", DEFAULT_CURRENCY);
	}

	public int getBackground() {
		return get("background", 0);
	}
}