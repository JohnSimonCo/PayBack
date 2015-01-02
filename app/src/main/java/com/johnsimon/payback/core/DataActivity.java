package com.johnsimon.payback.core;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.johnsimon.payback.storage.LocalStorage;
import com.johnsimon.payback.storage.Storage;
import com.johnsimon.payback.util.AppData;
import com.johnsimon.payback.util.ContactLoader;
import com.johnsimon.payback.util.Contacts;

/**
 * Created by johnrs on 2015-01-02.
 */
public abstract class DataActivity extends ActionBarActivity {

    protected Storage storage;
    public AppData data;

    protected ContactLoader contactLoader;
    public Contacts contacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        storage = new LocalStorage(this);
        storage.callbacks.add(dataCallback);

        contactLoader = new ContactLoader();
        contactLoader.callbacks.add(contactCallback);
        contactLoader.execute(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        storage.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();

        storage.connect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean handled = storage.handleActivityResult(requestCode, resultCode, data);

        if(!handled) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private DataActivity self = this;
    private Callback<AppData> dataCallback = new Callback<AppData>() {
        @Override
        public void onFired(AppData data) {
            self.data = data;
            onDataReceived();
        }
    };

    private Callback<Contacts> contactCallback = new Callback<Contacts>() {
        @Override
        public void onFired(Contacts contacts) {
            self.contacts = contacts;
            onContactsLoaded();
        }
    };

    protected void onDataReceived() {

    }

    protected void onContactsLoaded() {

    }
}
