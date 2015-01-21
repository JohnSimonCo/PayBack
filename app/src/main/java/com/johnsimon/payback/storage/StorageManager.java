package com.johnsimon.payback.storage;

import android.app.Activity;

public class StorageManager {
    private static Storage storage = null;

    public static Storage getStorage(Activity context) {
        if(storage == null) {
            storage = new LocalStorage(context);
        }

		//TODO remove this
        //((DriveStorage) storage).activity = context;

        return storage;
    }
}
