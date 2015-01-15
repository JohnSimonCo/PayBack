package com.johnsimon.payback.storage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.johnsimon.payback.core.DataActivity;
import com.johnsimon.payback.core.DataActivityInterface;
import com.johnsimon.payback.core.Debt;
import com.johnsimon.payback.core.Person;
import com.johnsimon.payback.util.AppData;
import com.johnsimon.payback.util.ColorPalette;

import java.util.Random;

public class LocalStorage extends Storage {

    private final static String SAVE_KEY_DATA = "DATA";

    public static SharedPreferences preferences;

    public LocalStorage(Context context) {
        super(context);

        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        String JSON = preferences.getString(SAVE_KEY_DATA, null);

        emit(AppData.fromJson(JSON));

		Random r = new Random();

		DataActivity dataActivity = (DataActivity) context;

		dataActivity.data = data;

		ColorPalette colorPalette = ColorPalette.getInstance(dataActivity);

		for(int i = 0; i < 10; i++) {

			data.people.add(new Person(Integer.toHexString(r.nextInt(1024)), colorPalette));
		}

		for(int i = 0; i < 100; i++) {
			data.debts.add(new Debt(data.people.get(r.nextInt(data.people.size())), r.nextFloat() * 200, null));
		}
    }

    @Override
    public void commit() {
        preferences.edit().putString(SAVE_KEY_DATA, data.save()).apply();
    }
}
