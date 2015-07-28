package com.johnsimon.payback.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.johnsimon.payback.async.Callback;
import com.johnsimon.payback.data.AppData;
import com.johnsimon.payback.data.DataSyncer;

public abstract class ExternalStorage extends Storage {

	protected LocalStorage localStorage;

	public ExternalStorage(final LocalStorage localStorage) {
		this.localStorage = localStorage;

		localStorage.subscription.listen(new Callback<AppData>() {
			@Override
			public void onCalled(AppData data) {
				localStorage.subscription.unregister(this);
				show("emit localStorage data");
				emit(data);
			}
		});
	}

	@Override
	public SharedPreferences getPreferences() {
		return localStorage.getPreferences();
	}

	@Override
	public void emit() {
		super.emit();
		localStorage.emit(data);
	}

	@Override
	protected void commit(String JSON) {
		show("commited data to localStorage");
		localStorage.commit(JSON);

		commitExternally(JSON);
	}

	protected abstract void commitExternally(String JSON);

	protected void sync(Context context, AppData externalData) {
		show("checking for external changes");
		AppData data = new AppData();
		if(DataSyncer.sync(localStorage.data, externalData, data)) {
			show("found changes and synced");

			commit(context, data);
			emit(data);
		}
	}

	@Override
	protected void show(String text) {
		/*
		if(StorageManager.activity != null) {
			Toast.makeText(StorageManager.activity.getApplicationContext(), text, Toast.LENGTH_SHORT).show();
		}*/
	}

	protected void error(String title, String content) {
		/*if(StorageManager.activity != null) {
			new MaterialDialog.Builder(StorageManager.activity)
					.title(title)
					.content(content)
					.show();
		}*/
	}
}
