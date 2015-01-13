package com.johnsimon.payback.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.johnsimon.payback.core.Contact;
import com.johnsimon.payback.core.Promise;
import com.johnsimon.payback.core.User;

public class ContactsLoader implements Runnable {

	private ContentResolver contentResolver;

	public Promise<User> userLoaded = new Promise<User>();
	public Promise<Contacts> contactsLoaded = new Promise<Contacts>();
	public Promise<Contacts> numbersLoaded = new Promise<Contacts>();

	public ContactsLoader(ContentResolver contentResolver) {
		this.contentResolver = contentResolver;
	}

	public static ContactsLoader run(Context context) {
		//Create loader
		ContactsLoader contactsLoader = new ContactsLoader(context.getContentResolver());

		//Start thread
		new Thread(contactsLoader).start();

		//Return instance
		return contactsLoader;
	}

	@Override
	public void run() {
		User user = getUser();

		userLoaded.fire(user);

		Contacts contacts = new Contacts();

		fetchContacts(contacts);
		contacts.user = user;

		contactsLoaded.fire(contacts);

		fetchNumbers(contacts);

		numbersLoaded.fire(contacts);
	}

	public User getUser() {
		String name = null;

		Cursor cursor = contentResolver.query(ContactsContract.Profile.CONTENT_URI, null, null, null, null);
		if(cursor.moveToFirst()) {
			name = cursor.getString(cursor.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME));
		}
		cursor.close();

		return new User(name);
	}

	public void fetchContacts(Contacts contacts) {
		Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		if (cursor.getCount() > 0) {
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
		}
		cursor.close();
	}

	public void fetchNumbers(Contacts contacts) {
		contacts.user.setNumbers(getUserPhoneNumbers());

		for(Contact contact : contacts) {
			contact.setNumbers(getContactPhoneNumbers(contact.id));
		}
	}

	private String[] getUserPhoneNumbers() {
		Cursor cursor = contentResolver.query(
				Uri.withAppendedPath(
						ContactsContract.Profile.CONTENT_URI,
						ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
				new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER},
				null, null, null);

		return getPhoneNumbers(cursor, 0);
	}

	private String[] getContactPhoneNumbers(long id) {
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
}
