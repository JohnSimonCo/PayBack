package com.johnsimon.payback.core;

import java.util.ArrayList;

public class Subscription<D> {
    private ArrayList<Callback<D>> callbacks = new ArrayList<>();
    private D data = null;

    public void listen(Callback<D> callback) {
        callbacks.add(callback);
        if(data != null) {
            callback.onCalled(data);
        }
    }

	public void unregister(Callback<D> callback) {
		callbacks.remove(callback);
	}

    public void broadcast(D data) {
        this.data = data;

        for(Callback<D> callback : callbacks) {
            callback.onCalled(data);
        }
    }
}
