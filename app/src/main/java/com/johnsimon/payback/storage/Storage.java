package com.johnsimon.payback.storage;

import android.content.Context;
import android.content.Intent;

import com.johnsimon.payback.core.Promise;
import com.johnsimon.payback.core.Subscription;
import com.johnsimon.payback.util.AppData;

/**
 * Created by johnrs on 2015-01-02.
 */
public abstract class Storage {
    protected Context context;
    public Subscription<AppData> subscription = new Subscription<>();
    public Promise<AppData> promise = new Promise<>();

    protected AppData data;

    public Storage(Context context) {
        this.context = context;
    }

    protected void emit(AppData data) {
        this.data = data;
        promise.fire(data);
        subscription.broadcast(data);
    }

    public abstract void commit();

    public void commit(AppData data) {
        this.data = data;
        commit();
    }

    public void connect() {

    }
    public void disconnect() {

    }

    public boolean handleActivityResult(final int requestCode, final int resultCode, final Intent data) {
        return false;
    }

    protected void show(String text) {
        /*
        Snackbar.with(context)
                .text(text)
                .show(context);*/
    }
}
