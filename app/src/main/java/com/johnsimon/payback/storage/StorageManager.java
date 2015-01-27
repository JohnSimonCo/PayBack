package com.johnsimon.payback.storage;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.johnsimon.payback.async.Callback;
import com.johnsimon.payback.async.NotificationCallback;
import com.johnsimon.payback.async.Subscription;
import com.johnsimon.payback.core.DataActivityInterface;

public class StorageManager {
	public final static String PREFERENCE_STORAGE_TYPE = "STORAGE_TYPE";
	public final static int STORAGE_TYPE_LOCAL = 0;
	public final static int STORAGE_TYPE_DRIVE = 1;

	private static LocalStorage localStorage = null;
	private static Storage storage = null;

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
	public static void migrateToDrive(final DataActivityInterface dataActivity) {
		final DriveStorage driveStorage = new DriveStorage(dataActivity.getContext(), localStorage);
		dataActivity.setStorage(driveStorage);
		driveStorage.connect();
		driveStorage.loginSubscription.listen(new Callback<String>() {
			@Override
			public void onCalled(String data) {
				localStorage.getPreferences().edit().putInt(PREFERENCE_STORAGE_TYPE, STORAGE_TYPE_DRIVE).apply();
				restart(dataActivity.getContext());
			}
		});

		driveStorage.loginCancelledNotification.listen(new NotificationCallback() {
			@Override
			public void onNotify() {
				dataActivity.setStorage(storage);
			}
		});
	}
	public static void migrateToLocal(Context context) {
		localStorage.wipe();
		localStorage.getPreferences().edit().putInt(PREFERENCE_STORAGE_TYPE, STORAGE_TYPE_LOCAL).apply();
		restart(context);
	}

	private static void restart(Context context) {
		System.exit(0);
		//context.finishAffinity();
		//context.startActivity(new Intent(context, FeedActivity.class));
	}
}
