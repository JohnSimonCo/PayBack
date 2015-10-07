package com.johnsimon.payback.loader;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Patterns;

import com.johnsimon.payback.core.Contact;
import com.johnsimon.payback.async.Promise;
import com.johnsimon.payback.data.User;
import com.johnsimon.payback.util.EmailUtils;
import com.johnsimon.payback.util.PhoneNumberUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ContactDataLoader extends AsyncTask<ContactDataLoader.Argument, Void, Void> {

	public Promise<Void> promise = new Promise<>();

	@Override
	protected Void doInBackground(Argument... params) {
		ContentResolver contentResolver = params[0].contentResolver;

        User user = params[0].user;
        ArrayList<Contact> contacts = params[0].contacts;

		String[] userData = getUserData(contentResolver);
		user.setNumbers(getUserPhoneNumbers(userData));
		user.setEmails(getUserEmails(userData));

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

	/*
	private String[] getUserPhoneNumbers(ContentResolver contentResolver) {
		Cursor cursor = contentResolver.query(
				Uri.withAppendedPath(
						ContactsContract.Profile.CONTENT_URI,
						ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
				new String[] {ContactsContract.CommonDataKinds.Phone.Number},
				null, null, null);

		String[] phoneNumbers = getPhoneNumbers(cursor, 0);

		cursor.close();

		return phoneNumbers;
	}*/

	private String[] getUserData(ContentResolver contentResolver) {
		Cursor cursor = contentResolver.query(
				Uri.withAppendedPath(
						ContactsContract.Profile.CONTENT_URI,
						ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
				new String[] {ContactsContract.CommonDataKinds.Phone.DATA},
				null, null, null);

		String[] data = getEmails(cursor, 0);

		cursor.close();

		return data;
	}

	private String[] getUserEmails(String[] userData) {
		if(userData == null) return null;

		ArrayList<String> emails = new ArrayList<>(userData.length);

		for(String string: userData) {
			if(EmailUtils.isValidEmailAdress(string)) {
				emails.add(string);
			}
		}

		return emails.toArray(new String[emails.size()]);
	}

	private String[] getUserPhoneNumbers(String[] userData) {
		if(userData == null) return null;

		ArrayList<String> numbers = new ArrayList<>(userData.length);

		for(String string: userData) {
			if(PhoneNumberUtils.isValidPhoneNumber(string)) {
				numbers.add(string);
			}
		}

		return numbers.toArray(new String[numbers.size()]);
	}

	private String[] getContactEmails(ContentResolver contentResolver, long id) {
		Cursor cursor = contentResolver.query(
				ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
				ContactsContract.CommonDataKinds.Email.CONTACT_ID +" =?", new String[]{Long.toString(id)}, null);

		String[] emails = getEmails(cursor, cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));

		cursor.close();

		return emails;
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

		ArrayList<String> numbers = new ArrayList<>(count);
		HashSet<String> uniqueNumbers = new HashSet<>(count);

		while(cursor.moveToNext()) {
			String number = cursor.getString(column);
			String normalized = PhoneNumberUtils.normalizePhoneNumber(number);
			if(number != null && !uniqueNumbers.contains(normalized)) {
				uniqueNumbers.add(normalized);

				numbers.add(number);
			}
		}

		int size = numbers.size();
		return size > 0 ? numbers.toArray(new String[size]) : null;
	}

	private String[] getEmails(Cursor cursor, int column) {
		int count = cursor.getCount();

		if(count < 1) return null;

		HashSet<String> emails = new HashSet<>(count);

		while(cursor.moveToNext()) {
			String email = cursor.getString(column);
			if(email != null) {
				emails.add(email);
			}
		}

		int size = emails.size();
		return size > 0 ? emails.toArray(new String[size]) : null;
	}

	//Removes all formatting, so that numbers can be compared
	private String normalizeEmail(String email) {
		return email == null ? null : email.toLowerCase();
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
