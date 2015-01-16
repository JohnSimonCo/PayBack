package com.johnsimon.payback.storage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.johnsimon.payback.core.Promise;
import com.johnsimon.payback.core.Subscription;
import com.johnsimon.payback.util.AppData;

public abstract class Storage {
    protected Context context;
    public Subscription<AppData> subscription = new Subscription<>();

    protected AppData data;

    public Storage(Context context) {
        this.context = context;
    }

	public abstract SharedPreferences getPreferences();

    protected void emit(AppData data) {
        this.data = data;
        subscription.broadcast(data);
    }

    public abstract void commit();

	public void wipe() {
		emit(new AppData());
		commit();
	}

    public void commit(AppData data) {
        this.data = data;
        commit();
    }

    public void connect() {

    }
    public void disconnect() {

    }

	public void requestRefresh() {
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
