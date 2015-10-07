package com.johnsimon.payback.util;

import android.util.Patterns;

public class EmailUtils {

    public static boolean isValidEmailAdress(CharSequence target) {
        return target != null && Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

}
