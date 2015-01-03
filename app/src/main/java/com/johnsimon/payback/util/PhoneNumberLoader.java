package com.johnsimon.payback.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;

import com.johnsimon.payback.core.Promise;
import com.johnsimon.payback.core.Contact;

/**
 * Created by johnrs on 2015-01-03.
 */
public class PhoneNumberLoader extends AsyncTask<PhoneNumberLoader.Argument, Void, Contacts> {
    public Promise<Contacts> promise = new Promise<>();

    @Override
    protected Contacts doInBackground(Argument... params) {
        Argument argument = params[0];
        Context context = argument.context;
        Contacts contacts = argument.contacts;

        ContentResolver contentResolver = context.getContentResolver();

        contacts.user.setNumber(getUserPhoneNumber(contentResolver));

        for(Contact contact : contacts) {
            contact.setNumber(getContactPhoneNumber(contentResolver, contact.id));
        }

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

    @Override
    protected void onPostExecute(Contacts contacts) {
        promise.fire(contacts);
    }

    public static class Argument {
        public Context context;
        public Contacts contacts;

        public Argument(Context context, Contacts contacts) {
            this.context = context;
            this.contacts = contacts;
        }
    }
}
