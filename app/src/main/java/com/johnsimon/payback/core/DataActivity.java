package com.johnsimon.payback.core;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.johnsimon.payback.async.Callback;
import com.johnsimon.payback.async.NotificationCallback;
import com.johnsimon.payback.data.User;
import com.johnsimon.payback.loader.ContactLoader;
import com.johnsimon.payback.storage.Storage;
import com.johnsimon.payback.storage.StorageManager;
import com.johnsimon.payback.data.AppData;
import com.johnsimon.payback.data.DataLinker;
import com.johnsimon.payback.ui.FeedActivity;
import com.johnsimon.payback.ui.base.BaseActivity;
import com.johnsimon.payback.util.AlarmScheduler;
import com.johnsimon.payback.util.PermissionManager;
import com.johnsimon.payback.util.Resource;
import com.johnsimon.payback.util.Undo;

public abstract class DataActivity extends BaseActivity implements DataActivityInterface {

    protected Storage storage;
    public AppData data;

	public User user;

	public boolean permissionContacts = false;

    protected ContactLoader contactLoader;

	private DataLinker dataLinker;
	private AlarmScheduler alarmScheduler;

	@Override
	public Activity getContext() {
		return this;
	}

	@Override
	public void setStorage(Storage storage) {
		this.storage = storage;
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
	public Storage getStorage() {
		return storage;
	}
	@Override
	public ContactLoader getContactLoader() {
		return contactLoader;
	}

	@Override
	public DataLinker getDataLinker() {
		return dataLinker;
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        storage = StorageManager.getStorage(getApplicationContext());

		if (!Resource.isFirstRun(storage.getPreferences(), false)) {
			if (PermissionManager.getPermission(Manifest.permission.READ_CONTACTS, this)) {
				contactLoader = ContactLoader.getLoader(getApplicationContext());
				permissionContacts = true;
			} else {
				permissionContacts = false;
			}
		}

	}

	@Override
    protected void onStart() {
        super.onStart();

		Undo.completeAction();

		storage.subscription.listen(dataLoadedCallback);
		if (contactLoader != null) {
			contactLoader.userLoaded.then(userLoadedCallback);

			dataLinker = new DataLinker(storage.subscription, contactLoader.contactsLoaded);
			dataLinker.linked.listen(dataLinkedCallback);
		}

		alarmScheduler = new AlarmScheduler(this, storage.subscription);

		storage.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

		storage.subscription.unregister(dataLoadedCallback);

		if (contactLoader != null) {
			contactLoader.userLoaded.unregister(userLoadedCallback);
		}

		if (dataLinker != null) {
			dataLinker.die();
		}
		alarmScheduler.die();

        storage.disconnect();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(StorageManager.loginManager != null) {
			if(StorageManager.loginManager.handleActivityResult(requestCode, resultCode, data)) {
				return;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
    }

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case PermissionManager.PERMISSION_FLAG_READ_CONTACTS:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					contactLoader = ContactLoader.getLoader(getApplicationContext());

					contactLoader.userLoaded.then(userLoadedCallback);

					dataLinker = new DataLinker(storage.subscription, contactLoader.contactsLoaded);
					dataLinker.linked.listen(dataLinkedCallback);
				} else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
					//Toast.makeText(this, "nope", Toast.LENGTH_SHORT).show();
				}
				break;
			case PermissionManager.PERMISSION_FLAG_BOTH:
				//TODO SIMME FIRST START NO IMAGES
				contactLoader = ContactLoader.getLoader(getApplicationContext());

				contactLoader.userLoaded.then(userLoadedCallback);

				dataLinker = new DataLinker(storage.subscription, contactLoader.contactsLoaded);
				dataLinker.linked.listen(dataLinkedCallback);
				break;

		}
	}

    private DataActivity self = this;
    private Callback<AppData> dataLoadedCallback = new Callback<AppData>() {
        @Override
        public void onCalled(AppData data) {
            self.data = data;
            onDataReceived();
        }
    };


	private NotificationCallback dataLinkedCallback = new NotificationCallback() {
		@Override
		public void onNotify() {
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
