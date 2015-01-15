package com.johnsimon.payback.core;

import android.app.Activity;

import com.johnsimon.payback.loader.ContactLoader;
import com.johnsimon.payback.storage.Storage;
import com.johnsimon.payback.util.AppData;

/**
 * Created by John on 2015-01-15.
 */
public interface DataActivityInterface {
	AppData getData();
	User getUser();

	Activity getContext();

	Storage getStorage();
	ContactLoader getContactLoader();
	Subscription<AppData> getDataLink();
}
