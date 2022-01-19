package com.jyoryo.app.android.smsmover.util;

import android.util.Log;

import com.jyoryo.app.android.smsmover.BuildConfig;

public class Logs {
    public static final int VERBOSE = 1;
    public static final int DEBUG = 2;
    public static final int INFO = 3;
    public static final int WARN = 4;
    public static final int ERROR = 5;
    public static final int NOTHING = 6;
    public static int level = BuildConfig.DEBUG ? VERBOSE : NOTHING;

    public static void v(String tag, String msg) {
        if (level <= VERBOSE) {
            Log.d(tag, msg);
        }
    }
    public static void d(String tag, String msg) {
        if (level <= DEBUG) {
            Log.d(tag, msg);
        }
    }
    public static void i(String tag, String msg) {
        if (level <= INFO) {
            Log.d(tag, msg);
        }
    }
    public static void w(String tag, String msg) {
        if (level <= WARN) {
            Log.d(tag, msg);
        }
    }
    public static void w(String tag, String msg, Throwable e) {
        if (level <= WARN) {
            Log.d(tag, msg, e);
        }
    }
    public static void e(String tag, String msg) {
        if (level <= ERROR) {
            Log.d(tag, msg);
        }
    }
    public static void e(String tag, String msg, Throwable e) {
        if (level <= ERROR) {
            Log.d(tag, msg, e);
        }
    }
}
