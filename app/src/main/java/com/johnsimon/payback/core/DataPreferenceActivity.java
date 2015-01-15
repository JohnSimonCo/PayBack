package com.johnsimon.payback.core;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.johnsimon.payback.loader.ContactLoader;
import com.johnsimon.payback.storage.Storage;
import com.johnsimon.payback.storage.StorageManager;
import com.johnsimon.payback.util.AppData;
import com.johnsimon.payback.util.DataLinker;

public class DataPreferenceActivity extends PreferenceActivity implements DataActivityInterface {

	protected Storage storage;
	public AppData data;

	public User user;

	protected Subscription<AppData> dataLink;
	protected ContactLoader contactLoader;

	@Override
	public Storage getStorage() {
		return storage;
	}
	@Override
	public ContactLoader getContactLoader() {
		return contactLoader;
	}
	@Override
	public Subscription<AppData> getDataLink() {
		return dataLink;
	}

	@Override
	public Activity getContext() {
		return this;
	}

	@Override
	public AppData getData() {
		return data;
	}

	@Override
	public User getUser() {
		return user;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		storage = StorageManager.getStorage(this);

		contactLoader = ContactLoader.getLoader(this);

		dataLink = new DataLinker().link(storage.subscription, contactLoader.contactsLoaded);
	}

	@Override
	protected void onStart() {
		super.onStart();

		storage.subscription.listen(dataLoadedCallback);

		dataLink.listen(dataLinkedCallback);

		contactLoader.userLoaded.then(userLoadedCallback);

		storage.connect();
	}

	@Override
	protected void onStop() {
		super.onStop();

		storage.subscription.unregister(dataLoadedCallback);

		contactLoader.userLoaded.unregister(userLoadedCallback);

		storage.disconnect();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		boolean handled = storage.handleActivityResult(requestCode, resultCode, data);

		if(!handled) {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private DataPreferenceActivity self = this;
	private Callback<AppData> dataLoadedCallback = new Callback<AppData>() {
		@Override
		public void onCalled(AppData data) {
			self.data = data;
			onDataReceived();
		}
	};

	private Callback<AppData> dataLinkedCallback = new Callback<AppData>() {
		@Override
		public void onCalled(AppData data) {
			onDataLinked();
		}
	};

	private boolean userLoaded = false;
	private Callback<User> userLoadedCallback = new Callback<User>() {
		@Override
		public void onCalled(User user) {
			if(userLoaded) return;

			userLoaded = true;

			self.user = user;

			onUserLoaded();
		}
	};

	protected void onDataReceived() {
	}

	protected void onDataLinked() {
	}

	protected void onUserLoaded() {
	}

}
