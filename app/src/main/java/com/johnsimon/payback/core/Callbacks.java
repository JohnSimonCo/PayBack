package com.johnsimon.payback.core;

import java.util.ArrayList;

/**
 * Created by johnrs on 2015-01-02.
 */
public class Callbacks<D> {
    private ArrayList<Callback<D>> callbacks = new ArrayList<>();
    private D data;

    private boolean hasFired = false;

    public void add(Callback<D> callback) {
        if(hasFired) {
            callback.onFired(data);
        } else {
            callbacks.add(callback);
        }
    }

    public void fire(D data) {
        if(hasFired) return;

        hasFired = true;
        this.data = data;

        for(Callback<D> callback : callbacks) {
            callback.onFired(data);
        }
        callbacks.clear();
    }
}
