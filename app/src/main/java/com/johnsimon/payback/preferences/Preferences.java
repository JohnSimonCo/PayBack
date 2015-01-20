package com.johnsimon.payback.preferences;

import com.johnsimon.payback.core.UserCurrency;

import java.util.HashMap;

public class Preferences extends HashMap<String, Preference> {

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
		if(preference == null) {
			put(key, new Preference<>(null));
			return defaultValue;
		}
		T value = (T) preference.getValue();
		return value == null ? defaultValue : value;
	}

	public UserCurrency getCurrency() {
		//TODO null
		return get("currency", new UserCurrency("LinuxHacker", "i<3UNIX", true));
	}

	public String getBackground() {
		return get("background", "mountains");
	}
}