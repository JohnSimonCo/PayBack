package com.johnsimon.payback.core;

import android.app.Fragment;
import android.os.Bundle;

import com.johnsimon.payback.async.Callback;
import com.johnsimon.payback.async.Notification;
import com.johnsimon.payback.async.NotificationCallback;
import com.johnsimon.payback.data.DataLinker;
import com.johnsimon.payback.data.User;
import com.johnsimon.payback.loader.ContactLoader;
import com.johnsimon.payback.storage.Storage;
import com.johnsimon.payback.data.AppData;
import com.johnsimon.payback.storage.StorageManager;

public abstract class DataFragment extends Fragment {
    protected Storage storage;
    public AppData data;
    public User user;

	private ContactLoader contactLoader;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		DataActivityInterface activity = getDataActivity();

		this.storage = activity.getStorage();

		contactLoader = activity.getContactLoader();
	}

	protected DataActivityInterface getDataActivity() {
		return (DataActivityInterface) getActivity();
	}

	@Override
	public void onStart() {
		super.onStart();

		storage.subscription.listen(dataLoadedCallback);

        DataLinker.linked.listen(dataLinkedCallback);

		contactLoader.userLoaded.then(userLoadedCallback);
	}

	@Override
	public void onStop() {
		super.onStop();

		storage.subscription.unregister(dataLoadedCallback);

		DataLinker.linked.unregister(dataLinkedCallback);

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
