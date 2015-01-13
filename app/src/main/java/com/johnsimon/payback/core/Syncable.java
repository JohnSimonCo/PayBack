package com.johnsimon.payback.core;

public interface Syncable<T> extends Identifiable {
    T syncWith(T other);
}
