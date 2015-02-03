package com.johnsimon.payback.core;

import android.app.Activity;

import com.johnsimon.payback.data.AppData;

/**
 * Created by John on 2015-02-03.
 */
public interface DataContextInterface {
	AppData getData();
	Activity getContext();
}
