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
		Cursor cursor = params[0].query(ContactsContract.Profile.CONTENT_URI, null, null, null, null);

		String name = cursor.moveToFirst()
			? cursor.getString(cursor.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME))
			: null;

		cursor.close();

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return new User(name);
	}

	@Override
	protected void onPostExecute(User user) {
		promise.fire(user);
	}
}
