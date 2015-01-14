package com.johnsimon.payback.loader;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;

import com.johnsimon.payback.core.Promise;
import com.johnsimon.payback.core.User;

public class UserLoader extends AsyncTask<ContentResolver, Void, User> {

	public Promise<User> promise = new Promise<>();

	@Override
	protected User doInBackground(ContentResolver... params) {
		String name = null;

		Cursor cursor = params[0].query(ContactsContract.Profile.CONTENT_URI, null, null, null, null);
		if(cursor.moveToFirst()) {
			name = cursor.getString(cursor.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME));
		}
		cursor.close();

		return new User(name);
	}

	@Override
	protected void onPostExecute(User user) {
		promise.fire(user);
	}
}
