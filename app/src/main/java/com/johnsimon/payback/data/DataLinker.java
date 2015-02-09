package com.johnsimon.payback.data;

import com.johnsimon.payback.async.Callback;
import com.johnsimon.payback.async.Notification;
import com.johnsimon.payback.core.Contact;
import com.johnsimon.payback.async.Promise;
import com.johnsimon.payback.async.Subscription;

import java.util.ArrayList;

public class DataLinker {

	private static AppData data;
    public static Notification linked = new Notification();

	private static Subscription<AppData> dataSubscription;
	private static Promise<ArrayList<Contact>> contactsPromise;

	public static void link(Subscription<AppData> dataSubscription, Promise<ArrayList<Contact>> contactsPromise) {
		if(DataLinker.dataSubscription != null) {
			DataLinker.dataSubscription.unregister(dataLoadedCallback);
		}

		if(DataLinker.contactsPromise != null) {
			DataLinker.contactsPromise.unregister(contactsLoadedCallback);
		}

		DataLinker.dataSubscription = dataSubscription;
		DataLinker.contactsPromise = contactsPromise;

		dataSubscription.listen(dataLoadedCallback);
	}

	public static Callback<AppData> dataLoadedCallback = new Callback<AppData>() {
		@Override
		public void onCalled(final AppData _data) {
			data = _data;
			contactsPromise.then(contactsLoadedCallback);
		}
	};

	public static Callback<ArrayList<Contact>> contactsLoadedCallback = new Callback<ArrayList<Contact>>() {
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

	public static void link(Person person, ArrayList<Contact> contacts) {
		for(Contact contact : contacts) {
			if(contact.matchTo(person)) {
				person.linkTo(contact);
			}
		}
	}
}
