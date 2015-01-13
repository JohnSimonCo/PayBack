package com.johnsimon.payback.loader;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;

import com.johnsimon.payback.core.Contact;
import com.johnsimon.payback.core.Promise;
import com.johnsimon.payback.core.User;
import com.johnsimon.payback.util.Contacts;

/**
 * Created by John on 2015-01-13.
 */
public class ContactsLoader extends AsyncTask<ContactsLoader.Argument, Void, Contacts> {

	public Promise<Contacts> promise = new Promise<Contacts>();

	@Override
	protected Contacts doInBackground(Argument... params) {
		Contacts contacts = new Contacts();

		contacts.user = params[0].user;

		Cursor cursor = params[0].contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		while (cursor.moveToNext()) {
			//Get contact name
			String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

			//Exlude empty names and email adresses
			if(name == null || name.matches(".*@.*\\..*")) continue;

			//Test if the name is unique
			boolean unique = true;
			for (Contact contact : contacts) {
				if (contact.name.equals(name)) unique = false;
			}

			//Exlude non-unique contacts
			if(!unique) continue;

			//Get rest of contact info
			long id = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));
			String photoURI = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));

			contacts.add(new Contact(name, photoURI, id));
		}
		cursor.close();

		return contacts;
	}

	@Override
	protected void onPostExecute(Contacts contacts) {
		promise.fire(contacts);
	}

	public static class Argument {
		public ContentResolver contentResolver;
		public User user;

		public Argument(ContentResolver contentResolver, User user) {
			this.contentResolver = contentResolver;
			this.user = user;
		}
	}
}
