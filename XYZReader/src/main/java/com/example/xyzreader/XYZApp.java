package com.example.xyzreader;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by alexander on 09.02.16.
 */
public class XYZApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
