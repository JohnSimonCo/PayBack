package com.johnsimon.payback.core;

/**
 * Created by johnrs on 2015-01-02.
 */
public interface Callback<D> {
    void onDataReceived(D data);
}
