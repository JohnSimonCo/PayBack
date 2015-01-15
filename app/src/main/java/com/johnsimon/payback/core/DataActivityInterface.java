package com.johnsimon.payback.core;

import com.johnsimon.payback.loader.ContactLoader;
import com.johnsimon.payback.storage.Storage;
import com.johnsimon.payback.util.AppData;

/**
 * Created by John on 2015-01-15.
 */
public interface DataActivityInterface {
	Storage getStorage();
	ContactLoader getContactLoader();
	Subscription<AppData> getDataLink();
}
