package com.johnsimon.payback.core;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import com.johnsimon.payback.storage.Storage;
import com.johnsimon.payback.util.AppData;
import com.johnsimon.payback.util.ContactLoader;
import com.johnsimon.payback.util.Contacts;

/**
 * Created by johnrs on 2015-01-02.
 */
public abstract class DataDialogFragment extends DialogFragment {
    protected Storage storage;
    public AppData data;

    protected ContactLoader contactLoader;
    public Contacts contacts;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DataActivity activity = (DataActivity) getActivity();
        this.storage = activity.storage;
        storage.callbacks.add(dataCallback);

        this.contactLoader = activity.contactLoader;
        contactLoader.callbacks.add(contactCallback);

        return super.onCreateDialog(savedInstanceState);
    }

    private DataDialogFragment self = this;
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
