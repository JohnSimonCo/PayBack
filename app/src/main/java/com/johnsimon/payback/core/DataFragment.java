package com.johnsimon.payback.core;

import android.os.Bundle;

import com.johnsimon.payback.async.Callback;
import com.johnsimon.payback.async.NotificationCallback;
import com.johnsimon.payback.data.User;
import com.johnsimon.payback.storage.Storage;
import com.johnsimon.payback.data.AppData;
import com.johnsimon.payback.ui.base.BaseFragment;

public abstract class DataFragment extends BaseFragment {
	protected Storage storage;
	public AppData data;
	public User user;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		DataActivityInterface activity = getDataActivity();

		this.storage = activity.getStorage();
	}

	protected DataActivityInterface getDataActivity() {
		return (DataActivityInterface) getActivity();
	}

	@Override
	public void onStart() {
		super.onStart();

		DataActivityInterface activity = getDataActivity();

		storage.subscription.listen(dataLoadedCallback);

		activity.getContactLoader().userLoaded.then(userLoadedCallback);

		activity.getDataLinker().linked.listen(dataLinkedCallback);
	}

	@Override
	public void onStop() {
		super.onStop();

		DataActivityInterface activity = getDataActivity();

		storage.subscription.unregister(dataLoadedCallback);

		activity.getContactLoader().userLoaded.unregister(userLoadedCallback);

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
