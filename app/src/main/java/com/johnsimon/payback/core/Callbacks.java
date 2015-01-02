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

    public static void all(final Callback callback, Callbacks... callbacks) {
        final FiredCounter counter = new FiredCounter(callbacks.length);

        Callback check = new Callback() {
            @Override
            public void onFired(Object data) {
                if(counter.fire().isDone()) {
                    callback.onFired(null);
                }
            }
        };

        for(Callbacks cb : callbacks) {
            cb.add(check);
        }
    }

    private static class FiredCounter {
        private int fired = 0;
        private int shouldFire;

        public FiredCounter(int shouldFire) {
            this.shouldFire = shouldFire;
        }

        public FiredCounter fire() {
            ++fired;
            return this;
        }

        public boolean isDone() {
            return fired >= shouldFire;
        }
    }
}
