package com.jyoryo.app.android.smsmover;

import android.app.Application;
import android.content.Context;

/**
 * SmsMover Appliction
 */
public class SmsMoverApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this.getApplicationContext();
    }

    synchronized public static Context getContext() {
        return context;
    }
}
