package com.johnsimon.payback.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;

import com.johnsimon.payback.core.Promise;
import com.johnsimon.payback.core.Contact;
import com.johnsimon.payback.core.User;

public class ContactLoader extends AsyncTask<Context, Void, Contacts> {

    public Promise<Contacts> promise = new Promise<>();

    @Override
    protected Contacts doInBackground(Context... params) {
        Context context = params[0];

        ContentResolver contentResolver = context.getContentResolver();

        Contacts contacts = getContacts(contentResolver);

        contacts.user = getUser(contentResolver);

        return contacts;
    }

    public static User getUser(ContentResolver contentResolver) {
        String name = null;

        Cursor cursor = contentResolver.query(ContactsContract.Profile.CONTENT_URI, null, null, null, null);
        if(cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME));
        }
        cursor.close();

        return new User(name);
    }

    public static Contacts getContacts(ContentResolver contentResolver) {
        Contacts contacts = new Contacts();
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

        return contacts;
    }



    @Override
    protected void onPostExecute(Contacts contacts) {
        promise.fire(contacts);
    }
}