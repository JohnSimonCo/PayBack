package com.johnsimon.payback.core;

import android.app.Application;

import com.johnsimon.payback.BuildConfig;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(
        formUri = "https://collector.tracepot.com/195178b1"
)

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (!BuildConfig.DEBUG) {
            ACRA.init(this);
        }
    }
}
