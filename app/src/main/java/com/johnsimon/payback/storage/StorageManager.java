package com.johnsimon.payback.storage;

import android.app.Activity;

public class StorageManager {
	public final static String PREFERENCE_STORAGE_TYPE = "STORAGE_TYPE";
	public final static int STORAGE_TYPE_LOCAL = 0;
	public final static int STORAGE_TYPE_DRIVE = 1;

	private static LocalStorage localStorage = null;
	private static Storage storage = null;

    public static Storage getStorage(Activity context) {
        if(localStorage == null) {
            localStorage = new LocalStorage(context);
        }

		if(storage == null) {
			switch (localStorage.getPreferences().getInt(PREFERENCE_STORAGE_TYPE, STORAGE_TYPE_LOCAL)) {
				case STORAGE_TYPE_LOCAL:
					storage = localStorage;
					break;
				case STORAGE_TYPE_DRIVE:
					storage = new DriveStorage(context, localStorage);
					break;
			}
		}
        //((DriveStorage) storage).activity = context;

        return storage;
    }
	public static void migrateToDrive(Activity context) {
		storage = new DriveStorage(context, localStorage);
		storage.connect();
		localStorage.getPreferences().edit().putInt(PREFERENCE_STORAGE_TYPE, STORAGE_TYPE_DRIVE).apply();
	}
	public static void migrateToLocal(Activity context) {
		storage.disconnect();
		storage = localStorage;
		localStorage.getPreferences().edit().putInt(PREFERENCE_STORAGE_TYPE, STORAGE_TYPE_LOCAL).apply();
	}
}
