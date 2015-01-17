package com.johnsimon.payback.preferences;

import java.util.HashMap;

/**
 * Created by johnrs on 2015-01-17.
 */
public class Preferences extends HashMap<String, Preference> {
    public boolean getBoolean(String key, boolean defaultValue) {
        Preference preference = get(key);
        return preference == null
            ? defaultValue
            : (boolean) preference.getValue();
    }

    public void setBoolean(String key, boolean value) {
        Preference preference = get(key);
        if(preference == null) {
            put(key, new Preference<>(value));
        } else {
            preference.setValue(value);
        }
    }
}