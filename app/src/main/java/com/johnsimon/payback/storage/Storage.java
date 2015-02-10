package com.johnsimon.payback.storage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.johnsimon.payback.async.Subscription;
import com.johnsimon.payback.data.AppData;

public abstract class Storage {
    public Subscription<AppData> subscription = new Subscription<>();

    protected AppData data;

	public abstract SharedPreferences getPreferences();

    protected void emit(AppData data) {
        this.data = data;
		emit();
    }

	public void emit() {
		subscription.broadcast(data);
	}

    protected abstract void commit(String JSON);

	public void wipe() {
		commit(AppData.defaultAppData());
		emit();
	}

	public void commit() {
		commit(data.save());
	}

    public void commit(AppData data) {
        this.data = data;
		commit();
    }

	public boolean isExternalStorage() {
		return this instanceof ExternalStorage;
	}

	public ExternalStorage asExternalStorage() {
		return (ExternalStorage) this;
	}

	public boolean isDriveStorage() {
		return this instanceof DriveStorage;
	}

	public DriveStorage asDriveStorage() {
		return (DriveStorage) this;
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
