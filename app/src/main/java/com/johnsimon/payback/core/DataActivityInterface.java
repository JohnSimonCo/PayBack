package com.johnsimon.payback.core;

import android.app.Activity;

import com.johnsimon.payback.async.Notification;
import com.johnsimon.payback.data.DataLinker;
import com.johnsimon.payback.data.User;
import com.johnsimon.payback.loader.ContactLoader;
import com.johnsimon.payback.storage.Storage;
import com.johnsimon.payback.data.AppData;

/**
 * Created by John on 2015-01-15.
 */
public interface DataActivityInterface extends DataContextInterface {
	User getUser();

	Activity getContext();

	Storage getStorage();
	ContactLoader getContactLoader();
	DataLinker getDataLinker();

	void setStorage(Storage storage);
}
