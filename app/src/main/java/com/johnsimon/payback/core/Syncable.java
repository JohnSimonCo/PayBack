package com.johnsimon.payback.core;

/**
 * Created by johnrs on 2015-01-04.
 */
public interface Syncable<T> extends Identifiable {
    T syncWith(T other);
}
