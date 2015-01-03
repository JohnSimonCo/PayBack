package com.johnsimon.payback.storage;

import android.app.Activity;

/**
 * Created by johnrs on 2015-01-03.
 */
public class StorageManager {
    private static Storage storage = null;

    public static Storage getStorage(Activity context) {
        if(storage == null) {
            storage = new DriveStorage(context);
        }

        ((DriveStorage) storage).activity = context;

        return storage;
    }
}
