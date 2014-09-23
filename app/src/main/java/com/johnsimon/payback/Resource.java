package com.johnsimon.payback;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.Locale;
import java.util.UUID;

public class Resource {
	private final static String SAVE_KEY_FIRST_RUN = "FIRST_RUN";
	private final static String SAVE_KEY_APP_DATA = "APP_DATA";

	private final static String PACKAGE_NAME = "se.jrp.deptapp";
	private final static String ARG_PREFIX = PACKAGE_NAME + ".ARG_";

	public static AppData data = null;
	public static ArrayList<Person> people;
	public static ArrayList<Debt> debts;
	public static boolean isFirstRun = false;
	private static SharedPreferences preferences;

	private final static String currencySymbol = Currency.getInstance(Locale.getDefault()).getSymbol();

	public static void fetchData(SharedPreferences preferences) {
		if(data != null) return;
		Resource.preferences = preferences;

		String JSON = preferences.getString(SAVE_KEY_APP_DATA, null);
		data = JSON == null ? new AppData() : new Gson().fromJson(JSON, AppDataSerializable.class).extract();
		people = data.people;
		debts = data.debts;

		isFirstRun = isFirstRun();
		if(isFirstRun) {
			Person druggie = new Person("Random druggie", UUID.randomUUID());
			Person dealer = new Person("Ma hagsätra dealer", UUID.randomUUID());
			Person ica = new Person("killen på ica", UUID.randomUUID());
			people.add(druggie);
			people.add(dealer);
			people.add(ica);

			debts.add(new Debt(druggie, -2000, "weed an sum white shit"));
			debts.add(new Debt(dealer, -20000, "100 l moskovskaya, ingen grossistrabbat :/"));
			debts.add(new Debt(ica, 350, "sög av mig"));
			debts.add(new Debt(ica, -10, "snabbt knull"));
			debts.add(new Debt(ica, 500, "hand job på plattan"));

			commit();
		}

		/* Censored Version
		if(isFirstRun(preferences)) {
			Person john = new Person("John Rapp", UUID.randomUUID());
			Person simon = new Person("Simon Halvdansson", UUID.randomUUID());
			Person agge = new Person("Agge Eklöf", UUID.randomUUID());
			people.add(john);
			people.add(simon);
			people.add(agge);

			debts.add(new Debt(john, 100, "Dyr kebab"));
			debts.add(new Debt(simon, -200, "Pokemonkort"));
			debts.add(new Debt(simon, -1000, "Glömde kortet på ICA"));
			debts.add(new Debt(agge, 40, null));
			debts.add(new Debt(john, 200, "Lampor till dator"));
			debts.add(new Debt(agge, 2.5f, "Äpple delat på 2"));
			commit();
		}
		*/


		/*
		Person druggie = new Person("Random druggie", UUID.randomUUID());
		Person dealer = new Person("Ma hagsätra dealer", UUID.randomUUID());
		Person ica = new Person("killen på ica", UUID.randomUUID());
		people.add(druggie);
		people.add(dealer);
		people.add(ica);

		debts.add(new Debt(druggie, -2000, "weed an sum white shit"));
		debts.add(new Debt(dealer, -20000, "100 l moskovskaya, ingen grossistrabbat :/"));
		debts.add(new Debt(ica, 350, "sög av mig"));
		debts.add(new Debt(ica, -10, "snabbt knull"));
		debts.add(new Debt(ica, 500, "hand job på plattan"));
		*/
	}

	public static void commit() {
		String JSON = new Gson().toJson(new AppDataSerializable(data), AppDataSerializable.class);
		preferences.edit().putString(SAVE_KEY_APP_DATA, JSON).apply();
	}

    /*  Method to detect if it's the first time the user uses the app.
        Will return true if a preference with the key "FIRST_TIME"
        already exists.  */
    public static boolean isFirstRun() {
        if (preferences.getBoolean(SAVE_KEY_FIRST_RUN, true)) {
            preferences.edit().putBoolean(SAVE_KEY_FIRST_RUN, false).apply();

            return true;
        } else {
            return false;
        }
    }

	public static String prefix(String prefix) {
		return prefix + "_";
	}
	public static String arg(String prefix, String arg) {
			return ARG_PREFIX + prefix + "_" + arg;
	}

	public static String getCurrency() {
		return currencySymbol;
	}

	public static void toast(Context context, String text) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

	public static void toast(Context context, Boolean bool) {
		toast(context, Boolean.toString(bool));
	}

	public static void toast(Context context, int i) {
		toast(context, Integer.toString(i));
	}

    public static ArrayList<String> getAllAvailableCurrencies() {
        //Yeah i wrote this myself, thanks for asking! :D

        ArrayList<String> currencys = new ArrayList<String>();
        Locale[] locs = Locale.getAvailableLocales();

        for(Locale loc : locs) {
            try {
                String val = Currency.getInstance(loc).getCurrencyCode() + " (" + Currency.getInstance(loc).getSymbol() + ")";
                if(!currencys.contains(val))
                    currencys.add(val);
            } catch(Exception exc)
            {
                // Locale not found
            }
            Collections.sort(currencys);
        }

        return currencys;
    }

	public static void expand(final View v) {
		expand(v, 3);
	}

	public static void expand(final View v, int msPerDp) {
		v.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		final int targtetHeight = v.getMeasuredHeight();

		v.getLayoutParams().height = 0;
		v.setVisibility(View.VISIBLE);
		Animation a = new Animation()
		{
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				v.getLayoutParams().height = interpolatedTime == 1
						? LinearLayout.LayoutParams.WRAP_CONTENT
						: (int)(targtetHeight * interpolatedTime);
				v.requestLayout();
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};

		// 0.333dp/ms
		a.setDuration((int)(targtetHeight / v.getContext().getResources().getDisplayMetrics().density) * msPerDp);
		v.startAnimation(a);
	}
	public static void collapse(final View v) {
		collapse(v, 3);
	}

	public static void collapse(final View v, int msPerDp) {
		final int initialHeight = v.getMeasuredHeight();

		Animation a = new Animation()
		{
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				if(interpolatedTime == 1){
					v.setVisibility(View.GONE);
				}else{
					v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
					v.requestLayout();
				}
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};

		// 0.333dp/ms
		a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density) * msPerDp);
		v.startAnimation(a);
	}
}