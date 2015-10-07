package com.johnsimon.payback.util;

import android.util.Patterns;

public class PhoneNumberUtils {

    public static boolean isValidPhoneNumber(String string) {
        return Patterns.PHONE.matcher(string).matches();
    }


    //Removes all formatting, so that numbers can be compared
    public static String normalizePhoneNumber(String number) {
        return number == null ? null : number.replaceAll("[- ]", "").replaceAll("^\\+\\d{2}", "0");
    }

}
