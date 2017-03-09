package com.alperez.bt_microphone;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.alperez.bt_microphone.storage.DatabaseManager;

/**
 * Created by stanislav.perchenko on 3/9/2017.
 */

public class TheApplication extends Application {
    private static volatile Context appContext;
    private static boolean debuggable;

    @Override
    public void onCreate() {
        super.onCreate();

        appContext = this;

        debuggable = (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;

        DatabaseManager.initializeInstance(this);

    }

    public static Context getStaticContext() {
        return appContext;
    }

    public static boolean isDebuggable() {
        return debuggable;
    }
}
