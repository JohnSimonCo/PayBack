package com.johnsimon.payback.async;

public interface Callback<D> {
    void onCalled(D data);
}
