package com.johnsimon.payback.data;

import com.johnsimon.payback.async.Callback;
import com.johnsimon.payback.async.Notification;
import com.johnsimon.payback.core.Contact;
import com.johnsimon.payback.data.AppData;
import com.johnsimon.payback.data.Person;
import com.johnsimon.payback.async.Promise;
import com.johnsimon.payback.async.Subscription;

import java.util.ArrayList;

public class DataLinker {

	//TODO can probably be optimized by reusing old links
	private AppData data;
    private Notification output = new Notification();

	public Notification link(Subscription<AppData> dataSubscription, final Promise<ArrayList<Contact>> contactsPromise) {
		dataSubscription.listen(new Callback<AppData>() {
			@Override
			public void onCalled(final AppData _data) {
                data = _data;
				contactsPromise.thenUnique(contactsLoadedCallback);
			}
		});

		return output;
	}


	public static void link(AppData data, ArrayList<Contact> contacts) {
        data.contacts = contacts;

		for(Person person : data.people) {
			link(person, contacts);
		}
	}

	public static void link(Person person, ArrayList<Contact> contacts) {
		for(Contact contact : contacts) {
			if(contact.matchTo(person)) {
				person.linkTo(contact);
			}
		}
	}

    Callback<ArrayList<Contact>> contactsLoadedCallback = new Callback<ArrayList<Contact>>() {
        @Override
        public void onCalled(ArrayList<Contact> contacts) {
            link(data, contacts);
            output.broadcast();
        }
    };
}
