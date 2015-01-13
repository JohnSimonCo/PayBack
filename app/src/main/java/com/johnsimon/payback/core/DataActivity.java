package com.johnsimon.payback.core;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.johnsimon.payback.storage.Storage;
import com.johnsimon.payback.storage.StorageManager;
import com.johnsimon.payback.util.AppData;
import com.johnsimon.payback.util.Contacts;
import com.johnsimon.payback.util.ContactsLoader;

/**
 * Created by johnrs on 2015-01-02.
 */
public abstract class DataActivity extends ActionBarActivity {

    protected Storage storage;
    public AppData data;

    protected ContactsLoader contactsLoader;
    public Contacts contacts;

    protected Promise fullyLoadedPromise;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        storage = StorageManager.getStorage(this);

		contactsLoader = ContactsLoader.run(this);

        fullyLoadedPromise = Promise.all(storage.promise, contactsLoader.contactsLoaded);
    }

	@Override
    protected void onStart() {
        super.onStart();

		storage.subscription.listen(dataLoadedCallback);

		contactsLoader.contactsLoaded.then(contactsLoadedCallback);

		contactsLoader.numbersLoaded.then(phoneNumbersLoadedCallback);

		storage.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

		storage.subscription.unregister(dataLoadedCallback);

		contactsLoader.contactsLoaded.unregister(contactsLoadedCallback);

		contactsLoader.numbersLoaded.unregister(phoneNumbersLoadedCallback);

		fullyLoadedPromise.unregister(fullyLoadedCallback);

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

	private boolean contactsLoaded = false;
    private Callback<Contacts> contactsLoadedCallback = new Callback<Contacts>() {
        @Override
        public void onCalled(Contacts contacts) {
			if(contactsLoaded) return;

			contactsLoaded = true;

            self.contacts = contacts;

            onContactsLoaded();

			fullyLoadedPromise.then(fullyLoadedCallback);
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

	private boolean fullyLoaded = false;
    private Callback fullyLoadedCallback = new Callback() {
        @Override
        public void onCalled(Object data) {
			if(fullyLoaded) return;

			fullyLoaded = true;

            onFullyLoaded();
        }
    };

    protected void onDataReceived() {

    }

    protected void onContactsLoaded() {

    }

    protected void onPhoneNumbersLoaded() {
		data.link(contacts);
    }

    protected void onFullyLoaded() {

    }
}
