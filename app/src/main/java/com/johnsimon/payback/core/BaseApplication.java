package com.johnsimon.payback.core;

import android.app.Application;

import com.johnsimon.payback.BuildConfig;
import com.squareup.leakcanary.LeakCanary;

public class BaseApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            LeakCanary.install(this);
        }
    }
}
