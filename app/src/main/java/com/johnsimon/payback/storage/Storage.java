package com.johnsimon.payback.storage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.johnsimon.payback.async.Background;
import com.johnsimon.payback.async.BackgroundBlock;
import com.johnsimon.payback.async.Callback;
import com.johnsimon.payback.async.Subscription;
import com.johnsimon.payback.data.AppData;
import com.johnsimon.payback.data.backup.AutoBackuper;
import com.johnsimon.payback.data.backup.Backup;

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

	public void wipe(final Context context) {
		commit(context, AppData.defaultAppData());
		emit();
	}

	public void commit(final Context context) {
		data.saveAsync(context).then(new Callback<String>() {
			@Override
			public void onCalled(String JSON) {
				commit(JSON);
				sheduleBackup(context, JSON);
			}
		});
	}

	protected void sheduleBackup(Context context, String json) {

	}

    public void commit(Context context, AppData data) {
        this.data = data;
		commit(context);
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
