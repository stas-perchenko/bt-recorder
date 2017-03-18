package com.alperez.bt_microphone.utils;

import android.util.Log;

import com.alperez.bt_microphone.TheApplication;

/**
 * Created by stanislav.perchenko on 3/18/2017.
 */

public class ThreadLog {

    public static void d(String tag, String text) {
        if (TheApplication.isDebuggable()) {
            Log.d(tag, String.format("%s:: %s", Thread.currentThread().getName(), text));
        }
    }

    public static void e(String tag, String text) {
        if (TheApplication.isDebuggable()) {
            Log.e(tag, String.format("%s:: %s", Thread.currentThread().getName(), text));
        }
    }

}
