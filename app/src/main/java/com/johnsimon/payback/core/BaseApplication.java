package com.johnsimon.payback.core;

import android.app.Application;
import android.content.Context;

import org.acra.*;
import org.acra.annotation.*;

import com.johnsimon.payback.BuildConfig;
import com.squareup.leakcanary.LeakCanary;


@ReportsCrashes(formUri = "https://collector.tracepot.com/195178b1")


public class BaseApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        ACRA.init(this);

        BaseApplication.context = getApplicationContext();

        if (BuildConfig.DEBUG) {
            LeakCanary.install(this);
        }
    }

    public static Context getAppContext() {
        return BaseApplication.context;
    }
}
