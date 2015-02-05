package com.johnsimon.payback.core;

import android.content.Context;

import com.johnsimon.payback.data.AppData;

public interface DataContextInterface {
	AppData getData();
	Context getContext();
}
