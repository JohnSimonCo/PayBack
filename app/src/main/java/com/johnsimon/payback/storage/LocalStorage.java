package com.johnsimon.payback.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.johnsimon.payback.core.DataActivity;
import com.johnsimon.payback.data.Debt;
import com.johnsimon.payback.data.Person;
import com.johnsimon.payback.data.AppData;
import com.johnsimon.payback.util.ColorPalette;

import java.util.Random;

public class LocalStorage extends Storage {

    private final static String SAVE_KEY_DATA = "DATA";

    private SharedPreferences preferences;

    public LocalStorage(Context context) {
        super(context);

        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        String JSON = preferences.getString(SAVE_KEY_DATA, null);

        emit(AppData.fromJson(JSON));

		//test();
    }

	@Override
	public SharedPreferences getPreferences() {
		return preferences;
	}

	public void test() {
		Random r = new Random();

		DataActivity dataActivity = (DataActivity) context;

		dataActivity.data = data;

		ColorPalette colorPalette = ColorPalette.getInstance(dataActivity);

		for(int i = 0; i < 25; i++) {

			data.people.add(new Person(Integer.toHexString(r.nextInt()), colorPalette));
		}

		for(int i = 0; i < 100; i++) {
			data.debts.add(new Debt(data.people.get(r.nextInt(data.people.size())), r.nextFloat() * 200, null, data.preferences.getCurrency()));
		}
	}

    @Override
    public void commit() {
        preferences.edit().putString(SAVE_KEY_DATA, data.save()).apply();
    }
}
