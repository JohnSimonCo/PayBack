package com.johnsimon.payback.preferences;

import com.johnsimon.payback.core.SyncedData;

public class Preference<T> extends SyncedData<Preference> {

    private T value;

    public Preference(T value) {
        super(System.currentTimeMillis());

        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        touch();
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Preference))return false;
        Preference other = (Preference) o;

        return value.equals(other.value);
    }
}
