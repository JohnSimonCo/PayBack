package com.johnsimon.payback.data;

public interface Syncable<T> extends Identifiable {
    T syncWith(T other);
}
