package com.johnsimon.payback.core;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.johnsimon.payback.storage.Storage;
import com.johnsimon.payback.util.AppData;
import com.johnsimon.payback.util.ContactLoader;
import com.johnsimon.payback.util.Contacts;

/**
 * Created by johnrs on 2015-01-02.
 */
public abstract class DataFragment extends Fragment {
    protected Storage storage;
    public AppData data;

    protected ContactLoader contactLoader;
    public Contacts contacts;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        DataActivity activity = (DataActivity) getActivity();
        this.storage = activity.storage;
        storage.callbacks.add(dataCallback);

        this.contactLoader = activity.contactLoader;
        contactLoader.callbacks.add(contactCallback);

        Callbacks.all(bothCallback, storage.callbacks, contactLoader.callbacks);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private DataFragment self = this;
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

    private Callback bothCallback = new Callback() {
        @Override
        public void onFired(Object data) {
            onFullyLoaded();
        }
    };

    protected void onDataReceived() {

    }

    protected void onContactsLoaded() {

    }

    protected void onFullyLoaded() {

    }
}
