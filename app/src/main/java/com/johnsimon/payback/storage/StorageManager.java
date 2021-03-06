package com.johnsimon.payback.storage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.johnsimon.payback.async.Callback;
import com.johnsimon.payback.async.Promise;
import com.johnsimon.payback.ui.FeedActivity;

public class StorageManager {
	public final static String PREFERENCE_STORAGE_TYPE = "STORAGE_TYPE";
	public final static int STORAGE_TYPE_LOCAL = 0;
	public final static int STORAGE_TYPE_DRIVE = 1;

	private static LocalStorage localStorage = null;
	private static Storage storage = null;

	public static DriveLoginManager loginManager = null;

	public static LocalStorage getLocalStorage(Context context) {
		if(localStorage == null) {
			localStorage = new LocalStorage(context);
		}
		return localStorage;
	}

	public static SharedPreferences getPreferences(Context context) {
		return getLocalStorage(context).getPreferences();
	}

    public static Storage getStorage(Context context) {
		if(storage == null) {
			LocalStorage localStorage = getLocalStorage(context);

			switch (localStorage.getPreferences().getInt(PREFERENCE_STORAGE_TYPE, STORAGE_TYPE_LOCAL)) {
				case STORAGE_TYPE_DRIVE:
					DriveConnector connector = new DriveConnector(context);

					DriveStorage driveStorage = new DriveStorage(connector.client, localStorage);
					driveStorage.listen(connector.connectedPromise);

					storage = driveStorage;
					break;
				case STORAGE_TYPE_LOCAL: default:
					storage = localStorage;
					break;
			}
		}

        return storage;
    }

	public static boolean isDrive(Activity activity) {
		return getStorage(activity).isDriveStorage();
	}

	public static Promise<DriveLoginManager.LoginResult> migrateToDrive(final Activity activity) {
		final DriveLoginManager loginManager = new DriveLoginManager(activity);
		setLoginManager(loginManager);

		loginManager.loginResult.then(new Callback<DriveLoginManager.LoginResult>() {
			@Override
			public void onCalled(DriveLoginManager.LoginResult result) {
				if(result.success){
					DriveStorage driveStorage = new DriveStorage(loginManager.getClient(), localStorage);
					driveStorage.listen(loginManager.connectedPromise);

					localStorage.getPreferences().edit()
							.putInt(PREFERENCE_STORAGE_TYPE, STORAGE_TYPE_DRIVE)
							.putString(DriveLoginManager.PREFERENCE_ACCOUNT_NAME, result.accountName).apply();

					setStorage(driveStorage);
					restart(activity);
				} else {
					loginManager.disconnect();
				}
				setLoginManager(null);
			}
		});

		loginManager.go();

		return loginManager.loginResult;
	}

	public static Promise<DriveLoginManager.LoginResult> changeDriveAccount(final Activity activity) {
		final DriveStorage driveStorage = storage.asDriveStorage();

		final DriveLoginManager loginManager = new DriveLoginManager(activity);
		setLoginManager(loginManager);

		loginManager.loginResult.then(new Callback<DriveLoginManager.LoginResult>() {
			@Override
			public void onCalled(DriveLoginManager.LoginResult result) {
				if(result.success) {
					localStorage.wipe(activity);
					driveStorage.emit(localStorage.data);
					driveStorage.listen(loginManager.connectedPromise);

					localStorage.getPreferences().edit().putString(DriveLoginManager.PREFERENCE_ACCOUNT_NAME, result.accountName).apply();

					restart(activity);
				} else {
					migrateToLocal(activity);
				}
				setLoginManager(null);
			}
		});

		loginManager.go(driveStorage.getClient());

		return loginManager.loginResult;
	}

	public static void migrateToLocal(Activity activity) {
		storage.asDriveStorage().disconnect();

		localStorage.getPreferences().edit()
			.putInt(PREFERENCE_STORAGE_TYPE, STORAGE_TYPE_LOCAL)
			.remove(DriveLoginManager.PREFERENCE_ACCOUNT_NAME).apply();

		setStorage(localStorage);
		restart(activity);
	}

	private static void setStorage(Storage storage) {
		StorageManager.storage = storage;
	}

	private static void setLoginManager(DriveLoginManager loginManager) {
		StorageManager.loginManager = loginManager;
	}

	private static void restart(Activity activity) {
        activity.finishAffinity();
		FeedActivity.goToAll();
        activity.startActivity(new Intent(activity, FeedActivity.class));
	}
}
