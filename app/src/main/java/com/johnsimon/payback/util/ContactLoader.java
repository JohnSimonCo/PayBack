package com.johnsimon.payback.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;

import com.johnsimon.payback.core.Contact;
import com.johnsimon.payback.core.User;

public class ContactLoader extends AsyncTask<Activity, Void, Contacts> {

    @Override
    protected Contacts doInBackground(Activity... params) {
        Activity context = params[0];

        Contacts contacts = getContacts(context);

        contacts.user = getUser(context);

        return contacts;
    }

    public static User getUser(Activity context) {
        String name = null, number = null;

        ContentResolver contentResolver = context.getContentResolver();

        Cursor cursor = contentResolver.query(ContactsContract.Profile.CONTENT_URI, null, null, null, null);
        if(cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME));
            number = getUserPhoneNumber(contentResolver);
        }
        cursor.close();

        return new User(name, number);
    }

    public static Contacts getContacts(Activity context) {
        Contacts contacts = new Contacts();
        ContentResolver contentResolver = context.getContentResolver();
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
                String number = getContactPhoneNumber(contentResolver, id);
                String photoURI = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));

                contacts.add(new Contact(name, number, photoURI, id));
            }
        }
        cursor.close();

        return contacts;
    }

    private static String getUserPhoneNumber(ContentResolver contentResolver) {
        Cursor cursor = contentResolver.query(
                Uri.withAppendedPath(
                        ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
                new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER},
                null, null, null);

        String number = null;
        if(cursor.moveToFirst()) {
            number = cursor.getString(0);
        }

        cursor.close();

        return normalizePhoneNumber(number);
    }

    private static String getContactPhoneNumber(ContentResolver contentResolver, long id) {
        Cursor cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" =?", new String[]{Long.toString(id)}, null);

        String number = null;
        if(cursor.moveToFirst()) {
            number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }

        cursor.close();

        return normalizePhoneNumber(number);
    }

    //Removes all formatting, so that numbers can be compared
    private static String normalizePhoneNumber(String number) {
        return number == null ? null : number.replaceAll("[- ]", "").replaceAll("^\\+\\d{2}", "0");
    }
}