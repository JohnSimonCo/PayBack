package com.johnsimon.payback.core;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.johnsimon.payback.loader.ContactLoader;
import com.johnsimon.payback.storage.Storage;
import com.johnsimon.payback.util.AppData;
import com.johnsimon.payback.util.Contacts;
import com.johnsimon.payback.loader.ContactsLoader;

/**
 * Created by johnrs on 2015-01-02.
 */
public abstract class DataFragment extends Fragment {
    protected Storage storage;
    public AppData data;

    public Contacts contacts;

	private ContactLoader contactLoader;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        DataActivity activity = (DataActivity) getActivity();
        this.storage = activity.storage;

		contactLoader = activity.contactLoader;

        return super.onCreateView(inflater, container, savedInstanceState);
    }

	@Override
	public void onStart() {
		super.onStart();

		storage.subscription.listen(dataLoadedCallback);

		contactLoader.contactsLoaded.then(contactsLoadedCallback);

		contactLoader.phoneNumbersLoaded.then(phoneNumbersLoadedCallback);
	}

	@Override
	public void onStop() {
		super.onStop();

		storage.subscription.unregister(dataLoadedCallback);

		contactLoader.contactsLoaded.unregister(contactsLoadedCallback);

		contactLoader.phoneNumbersLoaded.unregister(phoneNumbersLoadedCallback);
	}

	private DataFragment self = this;
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

	protected void onDataLinked() {
	}

	protected void onContactsLoaded() {
	}

	protected void onUserLoaded() {
	}

	protected void onPhoneNumbersLoaded() {
	}
}
