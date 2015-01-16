package com.johnsimon.payback.core;

import android.app.DialogFragment;
import android.os.Bundle;

import com.johnsimon.payback.async.Callback;
import com.johnsimon.payback.async.Notification;
import com.johnsimon.payback.async.NotificationCallback;
import com.johnsimon.payback.async.Subscription;
import com.johnsimon.payback.loader.ContactLoader;
import com.johnsimon.payback.storage.Storage;
import com.johnsimon.payback.util.AppData;

public abstract class DataDialogFragment extends DialogFragment {
    protected Storage storage;
    public AppData data;
    public User user;

    private ContactLoader contactLoader;

    private Notification dataLink;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		DataActivityInterface activity = getDataActivity();

		this.storage = activity.getStorage();

		contactLoader = activity.getContactLoader();

		dataLink = activity.getDataLink();
	}

	protected DataActivityInterface getDataActivity() {
		return (DataActivityInterface) getActivity();
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

    private NotificationCallback dataLinkedCallback = new NotificationCallback() {
		@Override
		public void onNotify() {
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
