package com.johnsimon.payback.core;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import com.johnsimon.payback.storage.Storage;
import com.johnsimon.payback.util.AppData;
import com.johnsimon.payback.util.Contacts;

public abstract class DataDialogFragment extends DialogFragment {
    protected Storage storage;
    public AppData data;

    public Contacts contacts;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        DataActivity activity = (DataActivity) getActivity();
        this.storage = activity.storage;
        storage.promise.then(dataLoadedCallback);

        activity.contactLoader.promise.then(contactsLoadedCallback);
        activity.phoneNumberLoader.promise.then(phoneNumbersLoadedCallback);

        activity.fullyLoadedPromise.then(fullyLoadedCallback);

        super.onCreate(savedInstanceState);
    }

    private DataDialogFragment self = this;
    private Callback<AppData> dataLoadedCallback = new Callback<AppData>() {
        @Override
        public void onFired(AppData data) {
            self.data = data;
            onDataReceived();
        }
    };

    private Callback<Contacts> contactsLoadedCallback = new Callback<Contacts>() {
        @Override
        public void onFired(Contacts contacts) {
            self.contacts = contacts;
            onContactsLoaded();
        }
    };

    private Callback<Contacts> phoneNumbersLoadedCallback = new Callback<Contacts>() {
        @Override
        public void onFired(Contacts contacts) {
            onPhoneNumbersLoaded();
        }
    };

    private Callback fullyLoadedCallback = new Callback() {
        @Override
        public void onFired(Object data) {
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
