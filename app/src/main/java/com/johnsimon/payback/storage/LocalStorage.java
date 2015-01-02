package com.johnsimon.payback.storage;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.johnsimon.payback.util.AppData;

/**
 * Created by johnrs on 2015-01-02.
 */
public class LocalStorage extends Storage {

    private final static String SAVE_KEY_DATA = "DATA";

    public static SharedPreferences preferences;

    public LocalStorage(Activity context) {
        super(context);

        long time = System.currentTimeMillis();

        preferences = context.getPreferences(Context.MODE_PRIVATE);

        String JSON = preferences.getString(SAVE_KEY_DATA, null);
        emit(new AppData(context, JSON));

        show(Long.toString(System.currentTimeMillis() - time));
    }

    @Override
    public void commit() {
        preferences.edit().putString(SAVE_KEY_DATA, data.save()).apply();
    }
}
