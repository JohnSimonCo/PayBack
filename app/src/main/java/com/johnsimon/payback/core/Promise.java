package com.johnsimon.payback.core;

import java.util.ArrayList;

public class Promise<D> {
    private ArrayList<Callback<D>> callbacks = new ArrayList<>();
    private D data;

    private boolean hasFired = false;

    //TODO then unique
    public void then(Callback<D> callback) {
        if(hasFired) {
            callback.onCalled(data);
        } else {
            callbacks.add(callback);
        }
    }

    public void thenUnique(Callback<D> callback) {
        if(!callbacks.contains(callback)) {
            then(callback);
        }
    }

	public void unregister(Callback<D> callback) {
		callbacks.remove(callback);
	}

    public void fire(D data) {
        if(hasFired) return;

        hasFired = true;
        this.data = data;

        for(Callback<D> callback : callbacks) {
            callback.onCalled(data);
        }
        callbacks.clear();
    }

    public static Promise all(Promise... promises) {
        final Promise promise = new Promise();

        final Counter counter = new Counter(promises.length);

        Callback check = new Callback() {
            @Override
            public void onCalled(Object data) {
                if(counter.increment().isDone()) {
                    promise.fire(null);
                }
            }
        };

        for(Promise p : promises) {
            p.then(check);
        }

        return promise;
    }

    private static class Counter {
        private int i = 0;
        private int max;

        public Counter(int max) {
            this.max = max;
        }

        public Counter increment() {
            ++i;
            return this;
        }

        public boolean isDone() {
            return i >= max;
        }
    }
}
