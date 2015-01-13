package com.johnsimon.payback.core;

import android.app.DialogFragment;
import android.os.Bundle;

import com.johnsimon.payback.storage.Storage;
import com.johnsimon.payback.util.AppData;
import com.johnsimon.payback.util.Contacts;

public abstract class DataDialogFragment extends DialogFragment {
    protected Storage storage;
    public AppData data;

    public Contacts contacts;

	private Promise<Contacts> contactsPromise;
	private Promise<Contacts> phoneNumbersPromise;
	private Promise fullyLoadedPromise;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        DataActivity activity = (DataActivity) getActivity();
        this.storage = activity.storage;

		contactsPromise = activity.contactLoader.promise;

		phoneNumbersPromise = activity.phoneNumberLoader.promise;

		fullyLoadedPromise = activity.fullyLoadedPromise;

        super.onCreate(savedInstanceState);
    }

	@Override
	public void onStart() {
		super.onStart();

		storage.subscription.listen(dataLoadedCallback);

		contactsPromise.then(contactsLoadedCallback);

		phoneNumbersPromise.then(phoneNumbersLoadedCallback);

	}

	@Override
	public void onStop() {
		super.onStop();

		storage.subscription.unregister(dataLoadedCallback);

		contactsPromise.unregister(contactsLoadedCallback);

		phoneNumbersPromise.unregister(phoneNumbersLoadedCallback);

		fullyLoadedPromise.unregister(fullyLoadedCallback);
	}

	private DataDialogFragment self = this;
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

    }

    protected void onFullyLoaded() {

    }
}
