package com.johnsimon.payback.loader;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;

import com.johnsimon.payback.core.Contact;
import com.johnsimon.payback.core.Promise;
import com.johnsimon.payback.util.Contacts;

/**
 * Created by John on 2015-01-13.
 */
public class PhoneNumberLoader extends AsyncTask<PhoneNumberLoader.Argument, Void, Contacts> {

	public Promise<Contacts> promise = new Promise<>();

	@Override
	protected Contacts doInBackground(Argument... params) {
		ContentResolver contentResolver = params[0].contentResolver;
		Contacts contacts = params[0].contacts;

		contacts.user.setNumbers(getUserPhoneNumbers(contentResolver));

		for(Contact contact : contacts) {
			contact.setNumbers(getContactPhoneNumbers(contentResolver, contact.id));
		}

		return contacts;
	}

	@Override
	protected void onPostExecute(Contacts contacts) {
		promise.fire(contacts);
	}

	private String[] getUserPhoneNumbers(ContentResolver contentResolver) {
		Cursor cursor = contentResolver.query(
				Uri.withAppendedPath(
						ContactsContract.Profile.CONTENT_URI,
						ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
				new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER},
				null, null, null);

		return getPhoneNumbers(cursor, 0);
	}

	private String[] getContactPhoneNumbers(ContentResolver contentResolver, long id) {
		Cursor cursor = contentResolver.query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" =?", new String[]{Long.toString(id)}, null);

		return getPhoneNumbers(cursor, cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
	}


	private String[] getPhoneNumbers(Cursor cursor, int column) {
		int count = cursor.getCount();

		if(count < 1) return null;

		String[] numbers = new String[count];
		int i = -1;
		while(cursor.moveToNext()) {
			numbers[++i] = normalizePhoneNumber(cursor.getString(column));
		}
		cursor.close();
		return numbers;
	}

	//Removes all formatting, so that numbers can be compared
	private String normalizePhoneNumber(String number) {
		return number == null ? null : number.replaceAll("[- ]", "").replaceAll("^\\+\\d{2}", "0");
	}

	public static class Argument {
		public ContentResolver contentResolver;
		public Contacts contacts;

		public Argument(ContentResolver contentResolver, Contacts contacts) {
			this.contentResolver = contentResolver;
			this.contacts = contacts;
		}
	}
}
