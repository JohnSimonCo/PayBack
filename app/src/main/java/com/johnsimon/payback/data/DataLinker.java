package com.johnsimon.payback.data;

import com.johnsimon.payback.async.Callback;
import com.johnsimon.payback.async.Notification;
import com.johnsimon.payback.core.Contact;
import com.johnsimon.payback.async.Promise;
import com.johnsimon.payback.async.Subscription;

import java.util.ArrayList;

public class DataLinker {

	private AppData data;
    public Notification linked = new Notification();

	private Subscription<AppData> dataSubscription;
	private Promise<ArrayList<Contact>> contactsPromise;

	public DataLinker(Subscription<AppData> dataSubscription, Promise<ArrayList<Contact>> contactsPromise) {
		this.dataSubscription = dataSubscription;
		this.contactsPromise = contactsPromise;

		dataSubscription.listen(dataLoadedCallback);
	}

	public Callback<AppData> dataLoadedCallback = new Callback<AppData>() {
		@Override
		public void onCalled(final AppData _data) {
			data = _data;
			contactsPromise.then(contactsLoadedCallback);
		}
	};

	public Callback<ArrayList<Contact>> contactsLoadedCallback = new Callback<ArrayList<Contact>>() {
		@Override
		public void onCalled(ArrayList<Contact> contacts) {
			if(data.contacts == null) {
				data.contacts = contacts;

				for(Person person : data.people) {
					link(person, contacts);
				}
			}

			linked.broadcast();
		}
	};

	public void die() {
		dataSubscription.unregister(dataLoadedCallback);
		contactsPromise.unregister(contactsLoadedCallback);
		linked.clearCallbacks();
	}

	public static void link(Person person, ArrayList<Contact> contacts) {
		for(Contact contact : contacts) {
			if(contact.matchTo(person)) {
				person.linkTo(contact);
			}
		}
	}
}
