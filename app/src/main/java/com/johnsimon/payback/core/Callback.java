package com.johnsimon.payback.core;

public interface Callback<D> {
    void onCalled(D data);
}
