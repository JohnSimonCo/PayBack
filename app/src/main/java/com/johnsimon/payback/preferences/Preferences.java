package com.johnsimon.payback.preferences;

import com.google.gson.annotations.SerializedName;
import com.johnsimon.payback.currency.CurrencyUtils;
import com.johnsimon.payback.currency.UserCurrency;

import java.util.Currency;
import java.util.Locale;

public class Preferences {

	private final static String DEFAULT_BACKGROUND = "mountains";

	@SerializedName("background")
	public Preference<String> background;

	@SerializedName("currency")
	public Preference<UserCurrency> currency;


	public static Preferences defaultPreferences() {
		Preferences preferences = new Preferences();

		preferences.background = new Preference<>();
		preferences.currency = new Preference<>();

		return preferences;
	}

	private <T> T getWithDefault(Preference<T> preference, T defaultValue) {
		T value = preference.getValue();
		return value == null ? defaultValue : value;
	}

	public UserCurrency getCurrency() {
		UserCurrency value = currency.getValue();

		if(value == null) {
			return CurrencyUtils.guessUserCurrency();
		}

		return value;
	}

	public String getBackground() {
		return getWithDefault(background, DEFAULT_BACKGROUND);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (!(o instanceof Preferences))return false;
		Preferences other = (Preferences) o;

		return other.background.equals(background)
			&& other.currency.equals(currency);
	}
}