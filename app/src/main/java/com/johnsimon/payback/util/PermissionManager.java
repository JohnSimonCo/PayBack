package com.johnsimon.payback.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.HashMap;

public class PermissionManager {

    public final static int PERMISSION_FLAG_READ_CONTACTS = 1;
    public final static int PERMISSION_FLAG_READ_EXTERNAL_STORAGE = 2;
    public final static int PERMISSION_FLAG_BOTH = 3;
    public final static HashMap<String, Integer> permissionFlags;

    static {
        permissionFlags = new HashMap<>();
        permissionFlags.put(Manifest.permission.READ_CONTACTS, PERMISSION_FLAG_READ_CONTACTS);
        permissionFlags.put(Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_FLAG_READ_EXTERNAL_STORAGE);
    }

    public static boolean getPermission(String permission, Activity activity) {

        if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), permission)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    permission)) {
                // Explain to the user why we need to read the contacts
                //Toast.makeText(activity, "Fuck you grant permission", Toast.LENGTH_SHORT).show();
            }

            ActivityCompat.requestPermissions(activity, new String[]{permission},
                    permissionFlags.get(permission));

            return false;
        } else {
            return true;
        }
    }

    public static void requestAllPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS},
                PermissionManager.PERMISSION_FLAG_BOTH);
    }

}
