package com.johnsimon.payback.util;

import com.johnsimon.payback.core.Callback;
import com.johnsimon.payback.core.Contact;
import com.johnsimon.payback.core.Person;
import com.johnsimon.payback.core.Promise;
import com.johnsimon.payback.core.Subscription;

/**
 * Created by John on 2015-01-13.
 */
public class DataLinker {

	public static Subscription<AppData> link(Subscription<AppData> dataSubscription, final Promise<Contacts> contactsPromise) {
		final Subscription<AppData> output = new Subscription<>();

		dataSubscription.listen(new Callback<AppData>() {
			@Override
			public void onCalled(final AppData data) {
				contactsPromise.then(new Callback<Contacts>() {
					@Override
					public void onCalled(Contacts contacts) {
						link(data, contacts);
						output.broadcast(data);
					}
				});
			}
		});

		return output;
	}


	public static void link(AppData data, Contacts contacts) {
		for(Person person : data.people) {
			for(Contact contact : contacts) {
				if(contact.matchTo(person)) {
					person.linkTo(contact);
				}
			}
		}
	}
}
