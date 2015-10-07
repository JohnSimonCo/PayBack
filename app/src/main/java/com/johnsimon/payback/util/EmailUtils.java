package com.johnsimon.payback.util;

import android.util.Patterns;

public class EmailUtils {

    public static boolean isValidEmailAddress(String string) {
       return Patterns.EMAIL_ADDRESS.matcher(string).matches();
    }

}
