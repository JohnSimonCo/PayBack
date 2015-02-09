package com.johnsimon.payback.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.johnsimon.payback.core.DataActivity;
import com.johnsimon.payback.data.AppData;
import com.johnsimon.payback.data.Debt;
import com.johnsimon.payback.data.Person;
import com.johnsimon.payback.util.ColorPalette;

import java.util.Random;

public class LocalStorage extends Storage {

    private final static String SAVE_KEY_DATA = "DATA";

    private SharedPreferences preferences;

    public LocalStorage(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        String JSON = preferences.getString(SAVE_KEY_DATA, null);

        emit(AppData.fromJson(JSON));
    }

	@Override
	public SharedPreferences getPreferences() {
		return preferences;
	}

	//TODO testa
	public static void test(DataActivity dataActivity, AppData appData) {
		Random r = new Random();

		ColorPalette colorPalette = ColorPalette.getInstance(dataActivity);

		for(int i = 0; i < 25; i++) {

            appData.add(new Person(Integer.toHexString(r.nextInt()), colorPalette));
		}

		for(int i = 0; i < 100; i++) {
            appData.add(new Debt(appData.people.get(r.nextInt(appData.people.size())), r.nextFloat() * 200, null, appData.preferences.getCurrency().id));
		}
	}

	@Override
	protected void commit(String JSON) {
		preferences.edit().putString(SAVE_KEY_DATA, JSON).apply();
	}
}
