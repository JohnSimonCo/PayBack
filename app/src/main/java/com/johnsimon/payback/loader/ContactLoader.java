package com.johnsimon.payback.loader;

import android.content.ContentResolver;
import android.content.Context;
import android.os.AsyncTask;

import com.johnsimon.payback.async.Callback;
import com.johnsimon.payback.core.Contact;
import com.johnsimon.payback.async.Promise;
import com.johnsimon.payback.data.User;

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
	private ArrayList<Contact> contacts;

	public ContactLoader(Context context) {
		final ContentResolver contentResolver = context.getContentResolver();

		UserLoader userLoader = new UserLoader();
		userLoaded = userLoader.promise;

		final ContactsLoader contactsLoader = new ContactsLoader();
		contactsLoaded = contactsLoader.promise;
		
		final ContactDataLoader contactDataLoader = new ContactDataLoader();
		phoneNumbersLoaded = contactDataLoader.promise;


		userLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, contentResolver);
		contactsLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, contentResolver);
		userLoaded.then(new Callback<User>() {
			@Override
			public void onCalled(User _user) {
                user = _user;
			}
		});

		contactsLoaded.then(new Callback<ArrayList<Contact>>() {
			@Override
			public void onCalled(ArrayList<Contact> _contacts) {
				contacts = _contacts;
			}
		});

		Promise.all(userLoaded, contactsLoaded).then(new Callback<Void>() {
			@Override
			public void onCalled(Void v) {
				contactDataLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
						new ContactDataLoader.Argument(contentResolver, contacts, user));
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
