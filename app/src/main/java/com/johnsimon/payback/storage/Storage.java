package com.johnsimon.payback.storage;

import android.app.Activity;
import android.content.Intent;

import com.johnsimon.payback.core.Callbacks;
import com.johnsimon.payback.util.AppData;
import com.nispok.snackbar.Snackbar;

/**
 * Created by johnrs on 2015-01-02.
 */
public abstract class Storage {
    protected Activity context;
    public Callbacks<AppData> callbacks = new Callbacks<>();

    protected AppData data;

    public Storage(Activity context) {
        this.context = context;
    }

    protected void emit(AppData data) {
        this.data = data;
        callbacks.fire(data);
    }

    public abstract void commit();

    public void connect() {

    }
    public void disconnect() {

    }

    public boolean handleActivityResult(final int requestCode, final int resultCode, final Intent data) {
        return false;
    }

    protected void show(String text) {
        Snackbar.with(context)
                .text(text)
                .show(context);
    }
}
