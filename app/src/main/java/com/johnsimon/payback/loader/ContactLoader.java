package com.johnsimon.payback.loader;

import android.content.ContentResolver;
import android.content.Context;

import com.johnsimon.payback.core.Callback;
import com.johnsimon.payback.core.Contact;
import com.johnsimon.payback.core.Promise;
import com.johnsimon.payback.core.User;

import java.util.ArrayList;

/**
 * Created by John on 2015-01-13.
 */
public class ContactLoader {
	private static ContactLoader instance = null;

	public Promise<User> userLoaded;
	public Promise<ArrayList<Contact>> contactsLoaded;
	public Promise<Void> phoneNumbersLoaded;

    private User user;

	public ContactLoader(Context context) {
		final ContentResolver contentResolver = context.getContentResolver();

		UserLoader userLoader = new UserLoader();
		userLoaded = userLoader.promise;

		final ContactsLoader contactsLoader = new ContactsLoader();
		contactsLoaded = contactsLoader.promise;
		
		final PhoneNumberLoader phoneNumberLoader = new PhoneNumberLoader();
		phoneNumbersLoaded = phoneNumberLoader.promise;


		userLoader.execute(contentResolver);
		userLoaded.then(new Callback<User>() {
			@Override
			public void onCalled(User _user) {
                user = _user;
				contactsLoader.execute(contentResolver);
			}
		});

		contactsLoaded.then(new Callback<ArrayList<Contact>>() {
			@Override
			public void onCalled(ArrayList<Contact> contacts) {
				phoneNumberLoader.execute(new PhoneNumberLoader.Argument(contentResolver, contacts, user));
			}
		});
	}

	public static ContactLoader getLoader(Context context) {
		if(instance == null) {
			instance = new ContactLoader(context);
		}

		return instance;
	}
}
