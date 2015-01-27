package com.johnsimon.payback.storage;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.johnsimon.payback.async.Subscription;

public class StorageManager {
	public final static String PREFERENCE_STORAGE_TYPE = "STORAGE_TYPE";
	public final static int STORAGE_TYPE_LOCAL = 0;
	public final static int STORAGE_TYPE_DRIVE = 1;

	private static LocalStorage localStorage = null;
	private static Storage storage = null;

	public static Subscription<Storage> storageChangedSubscription = new Subscription<>();

	public static LocalStorage getLocalStorage(Context context) {
		if(localStorage == null) {
			localStorage = new LocalStorage(context);
		}
		return localStorage;
	}

	public static SharedPreferences getPreferences(Context context) {
		return getLocalStorage(context).getPreferences();
	}

    public static Storage getStorage(Activity context) {
		if(storage == null) {
			LocalStorage localStorage = getLocalStorage(context);
			switch (localStorage.getPreferences().getInt(PREFERENCE_STORAGE_TYPE, STORAGE_TYPE_LOCAL)) {
				case STORAGE_TYPE_LOCAL:
					storage = localStorage;
					break;
				case STORAGE_TYPE_DRIVE:
					storage = new DriveStorage(context, localStorage);
					break;
			}
		}
		//TODO innan release: ta bort
		if(storage instanceof DriveStorage) {
			((DriveStorage) storage).activity = context;
		}

        return storage;
    }
	public static void migrateToDrive(Activity context) {
		storage = new DriveStorage(context, localStorage);
		storage.connect();
		storageChangedSubscription.broadcast(storage);
		localStorage.getPreferences().edit().putInt(PREFERENCE_STORAGE_TYPE, STORAGE_TYPE_DRIVE).apply();
	}
	public static void migrateToLocal(Context context) {
		storage.disconnect();
		storage = localStorage;
		storageChangedSubscription.broadcast(storage);
		localStorage.getPreferences().edit().putInt(PREFERENCE_STORAGE_TYPE, STORAGE_TYPE_LOCAL).apply();
	}

	private static void restart(Activity context) {
		System.exit(0);
		//context.finishAffinity();
		//context.startActivity(new Intent(context, FeedActivity.class));
	}
}
