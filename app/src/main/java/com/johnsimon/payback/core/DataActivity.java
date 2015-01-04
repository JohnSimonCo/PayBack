package com.johnsimon.payback.core;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.johnsimon.payback.storage.Storage;
import com.johnsimon.payback.storage.StorageManager;
import com.johnsimon.payback.util.AppData;
import com.johnsimon.payback.util.ContactLoader;
import com.johnsimon.payback.util.Contacts;
import com.johnsimon.payback.util.PhoneNumberLoader;

/**
 * Created by johnrs on 2015-01-02.
 */
public abstract class DataActivity extends ActionBarActivity {

    protected Storage storage;
    public AppData data;

    protected ContactLoader contactLoader;
    protected PhoneNumberLoader phoneNumberLoader;
    public Contacts contacts;

    protected Promise fullyLoadedPromise;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        storage = StorageManager.getStorage(this);
        storage.subscription.listen(dataLoadedCallback);

        contactLoader = new ContactLoader();
        contactLoader.promise.then(contactsLoadedCallback);
        contactLoader.execute(this);

        phoneNumberLoader = new PhoneNumberLoader();
        phoneNumberLoader.promise.then(phoneNumbersLoadedCallback);

        fullyLoadedPromise = Promise.all(storage.promise, contactLoader.promise);
        fullyLoadedPromise.then(fullyLoadedCallback);
    }

    @Override
    protected void onStart() {
        super.onStart();

        storage.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

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

    private Callback<Contacts> contactsLoadedCallback = new Callback<Contacts>() {
        @Override
        public void onCalled(Contacts contacts) {
            phoneNumberLoader.execute(new PhoneNumberLoader.Argument(self, contacts));

            self.contacts = contacts;
            onContactsLoaded();
        }
    };

    private Callback<Contacts> phoneNumbersLoadedCallback = new Callback<Contacts>() {
        @Override
        public void onCalled(Contacts contacts) {
            onPhoneNumbersLoaded();
        }
    };

    private Callback fullyLoadedCallback = new Callback() {
        @Override
        public void onCalled(Object data) {
            onFullyLoaded();
        }
    };

    protected void onDataReceived() {

    }

    protected void onContactsLoaded() {

    }

    protected void onPhoneNumbersLoaded() {

    }

    protected void onFullyLoaded() {

    }
}
