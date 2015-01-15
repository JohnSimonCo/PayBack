package com.johnsimon.payback.loader;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;

import com.johnsimon.payback.core.Contact;
import com.johnsimon.payback.core.Promise;

import java.util.ArrayList;

public class ContactsLoader extends AsyncTask<ContentResolver, Void, ArrayList<Contact>> {

	public Promise<ArrayList<Contact>> promise = new Promise<>();

	@Override
	protected ArrayList<Contact> doInBackground(ContentResolver... params) {
        ArrayList<Contact> contacts = new ArrayList<>();

		Cursor cursor = params[0].query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
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

		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return contacts;
	}

	@Override
	protected void onPostExecute(ArrayList<Contact> contacts) {
		promise.fire(contacts);
	}
}
