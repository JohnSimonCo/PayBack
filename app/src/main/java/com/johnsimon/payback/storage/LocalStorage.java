package com.johnsimon.payback.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.johnsimon.payback.async.Background;
import com.johnsimon.payback.async.BackgroundBlock;
import com.johnsimon.payback.async.Callback;
import com.johnsimon.payback.core.DataActivity;
import com.johnsimon.payback.data.AppData;
import com.johnsimon.payback.data.Debt;
import com.johnsimon.payback.data.Person;
import com.johnsimon.payback.data.backup.AutoBackuper;
import com.johnsimon.payback.util.ColorPalette;

import java.util.Random;

public class LocalStorage extends Storage {

    private final static String SAVE_KEY_DATA = "DATA";

    private SharedPreferences preferences;

    public LocalStorage(final Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);

		Background.run(context, new BackgroundBlock<AppData>() {
			@Override
			public AppData run() {
				String JSON = preferences.getString(SAVE_KEY_DATA, null);
				return AppData.fromJson(context, JSON);
			}
		}).then(new Callback<AppData>() {
			@Override
			public void onCalled(AppData data) {
				emit(data);
			}
		});

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
	protected void sheduleBackup(Context context, String JSON) {
		AutoBackuper.scheduleBackup(context, preferences, JSON);
	}

	@Override
	protected void commit(String JSON) {
		preferences.edit().putString(SAVE_KEY_DATA, JSON).apply();
	}
}
