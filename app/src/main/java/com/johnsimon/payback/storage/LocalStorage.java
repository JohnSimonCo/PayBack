package com.johnsimon.payback.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.johnsimon.payback.util.AppData;

/**
 * Created by johnrs on 2015-01-02.
 */
public class LocalStorage extends Storage {

    private final static String SAVE_KEY_DATA = "DATA";

    public static SharedPreferences preferences;

    public LocalStorage(Context context) {
        super(context);

        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        String JSON = preferences.getString(SAVE_KEY_DATA, null);
        emit(AppData.fromJson(JSON));
    }

    @Override
    public void commit() {
        preferences.edit().putString(SAVE_KEY_DATA, data.save()).apply();
    }
}
