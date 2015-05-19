package com.johnsimon.payback.core;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

public class BaseApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
    }
}
