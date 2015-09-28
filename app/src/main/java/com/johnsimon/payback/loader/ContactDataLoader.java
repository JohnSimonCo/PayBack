package com.johnsimon.payback.loader;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;

import com.johnsimon.payback.core.Contact;
import com.johnsimon.payback.async.Promise;
import com.johnsimon.payback.data.User;

import java.util.ArrayList;
import java.util.HashSet;

public class ContactDataLoader extends AsyncTask<ContactDataLoader.Argument, Void, Void> {

	public Promise<Void> promise = new Promise<>();

	@Override
	protected Void doInBackground(Argument... params) {
		ContentResolver contentResolver = params[0].contentResolver;

        User user = params[0].user;
        ArrayList<Contact> contacts = params[0].contacts;

		user.setNumbers(getUserPhoneNumbers(contentResolver));

		for(Contact contact : contacts) {
			contact.setNumbers(getContactPhoneNumbers(contentResolver, contact.id));
			contact.setEmails(getContactEmails(contentResolver, contact.id));
		}

		return null;
	}

	@Override
	protected void onPostExecute(Void v) {
		promise.fire(v);
	}

	private String[] getUserPhoneNumbers(ContentResolver contentResolver) {
		Cursor cursor = contentResolver.query(
				Uri.withAppendedPath(
						ContactsContract.Profile.CONTENT_URI,
						ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
				new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER},
				null, null, null);

		String[] phoneNumbers = getPhoneNumbers(cursor, 0);

		cursor.close();

		return phoneNumbers;
	}

	private String[] getContactEmails(ContentResolver contentResolver, long id) {
		Cursor cursor = contentResolver.query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" =?", new String[]{Long.toString(id)}, null);

		String[] phoneNumbers = getPhoneNumbers(cursor, cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));

		cursor.close();

		return phoneNumbers;
	}

	private String[] getContactPhoneNumbers(ContentResolver contentResolver, long id) {
		Cursor cursor = contentResolver.query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" =?", new String[]{Long.toString(id)}, null);

		String[] phoneNumbers = getPhoneNumbers(cursor, cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

		cursor.close();

		return phoneNumbers;
	}


	private String[] getPhoneNumbers(Cursor cursor, int column) {
		int count = cursor.getCount();

		if(count < 1) return null;

		HashSet<String> numbers = new HashSet<>(count);

		while(cursor.moveToNext()) {
			numbers.add(normalizePhoneNumber(cursor.getString(column)));
		}

		int size = numbers.size();
		return size > 0 ? numbers.toArray(new String[size]) : null;
	}

	private String[] getEmails(Cursor cursor, int column) {
		int count = cursor.getCount();

		if(count < 1) return null;

		HashSet<String> numbers = new HashSet<>(count);

		while(cursor.moveToNext()) {
			numbers.add(normalizePhoneNumber(cursor.getString(column)));
		}

		int size = numbers.size();
		return size > 0 ? numbers.toArray(new String[size]) : null;
	}


	//Removes all formatting, so that numbers can be compared
	private String normalizePhoneNumber(String number) {
		return number == null ? null : number.replaceAll("[- ]", "").replaceAll("^\\+\\d{2}", "0");
	}

	public static class Argument {
		public ContentResolver contentResolver;
        public ArrayList<Contact> contacts;
        public User user;

		public Argument(ContentResolver contentResolver, ArrayList<Contact> contacts, User user) {
			this.contentResolver = contentResolver;
			this.contacts = contacts;
            this.user = user;
		}
	}
}
