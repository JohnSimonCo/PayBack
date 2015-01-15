package com.johnsimon.payback.core;

import android.app.DialogFragment;
import android.os.Bundle;

import com.johnsimon.payback.loader.ContactLoader;
import com.johnsimon.payback.storage.Storage;
import com.johnsimon.payback.util.AppData;

import java.util.ArrayList;

public abstract class DataDialogFragment extends DialogFragment {
    protected Storage storage;
    public AppData data;
    public User user;

    private ContactLoader contactLoader;

    private Subscription<AppData> dataLink;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		DataActivityInterface activity = (DataActivityInterface) getActivity();

		this.storage = activity.getStorage();

		contactLoader = activity.getContactLoader();

		dataLink = activity.getDataLink();
	}

    @Override
    public void onStart() {
        super.onStart();

        storage.subscription.listen(dataLoadedCallback);

        dataLink.listen(dataLinkedCallback);

        contactLoader.userLoaded.then(userLoadedCallback);
    }

    @Override
    public void onStop() {
        super.onStop();

        storage.subscription.unregister(dataLoadedCallback);

        dataLink.unregister(dataLinkedCallback);

        contactLoader.userLoaded.unregister(userLoadedCallback);
    }

    private Callback<AppData> dataLoadedCallback = new Callback<AppData>() {
        @Override
        public void onCalled(AppData _data) {
            data = _data;
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
        public void onCalled(User _user) {
            if(userLoaded) return;

            userLoaded = true;

            user = _user;

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
