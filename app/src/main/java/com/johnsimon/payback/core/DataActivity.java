package com.johnsimon.payback.core;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.johnsimon.payback.loader.ContactLoader;
import com.johnsimon.payback.storage.Storage;
import com.johnsimon.payback.storage.StorageManager;
import com.johnsimon.payback.util.AppData;
import com.johnsimon.payback.util.Contacts;
import com.johnsimon.payback.util.DataLinker;

public abstract class DataActivity extends ActionBarActivity {

    protected Storage storage;
    public AppData data;

	protected Subscription<AppData> dataLinkedSubscription;
    protected ContactLoader contactLoader;
    public Contacts contacts;
	public User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        storage = StorageManager.getStorage(this);

		contactLoader = ContactLoader.getLoader(this);

		dataLinkedSubscription = DataLinker.link(storage.subscription, contactLoader.contactsLoaded);
    }

	@Override
    protected void onStart() {
        super.onStart();

		storage.subscription.listen(dataLoadedCallback);

		dataLinkedSubscription.listen(dataLinkedCallback);

		contactLoader.contactsLoaded.then(contactsLoadedCallback);

		contactLoader.userLoaded.then(userLoadedCallback);

		contactLoader.phoneNumbersLoaded.then(phoneNumbersLoadedCallback);

		storage.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

		storage.subscription.unregister(dataLoadedCallback);

		contactLoader.contactsLoaded.unregister(contactsLoadedCallback);

		contactLoader.userLoaded.unregister(userLoadedCallback);

		contactLoader.phoneNumbersLoaded.unregister(phoneNumbersLoadedCallback);

        storage.disconnect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean handled = storage.handleActivityResult(requestCode, resultCode, data);

        if(!handled) {
            super.onActivityResult(requestCode, resultCode, data);
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

	private Callback<AppData> dataLinkedCallback = new Callback<AppData>() {
		@Override
		public void onCalled(AppData data) {
			onDataLinked();
		}
	};

	private boolean contactsLoaded = false;
    private Callback<Contacts> contactsLoadedCallback = new Callback<Contacts>() {
        @Override
        public void onCalled(Contacts contacts) {
			if(contactsLoaded) return;

			contactsLoaded = true;

            self.contacts = contacts;

            onContactsLoaded();
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

	private boolean phoneNumbersLoaded = false;
    private Callback<Contacts> phoneNumbersLoadedCallback = new Callback<Contacts>() {
        @Override
        public void onCalled(Contacts contacts) {
			if(phoneNumbersLoaded) return;

			phoneNumbersLoaded = true;

            onPhoneNumbersLoaded();
        }
    };

    protected void onDataReceived() {
    }

	protected void onDataLinked() {
	}

    protected void onContactsLoaded() {
    }

	protected void onUserLoaded() {
	}

    protected void onPhoneNumbersLoaded() {
    }

}
