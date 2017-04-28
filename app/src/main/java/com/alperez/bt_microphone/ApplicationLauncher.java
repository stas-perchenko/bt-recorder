package com.alperez.bt_microphone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.alperez.bt_microphone.storage.DatabaseAdapter;
import com.alperez.bt_microphone.ui.SearchDevicesActivity;
import com.alperez.bt_microphone.ui.activity.KnownDeviceListActivity;

/**
 * Created by stanislav.perchenko on 4/28/2017.
 */

public class ApplicationLauncher extends Activity {

    public static void launchWithFlagsToReloadApp(Context ctx) {
        Intent i = new Intent(ctx, ApplicationLauncher.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ctx.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Class<? extends Activity> aCls = checkHasStoredDevices() ? KnownDeviceListActivity.class : SearchDevicesActivity.class;
        startActivity(new Intent(this, aCls));

        finish();
    }

    private boolean checkHasStoredDevices() {
        DatabaseAdapter db = new DatabaseAdapter();
        try {
            return db.selectAllValidDevices().size() > 0;
        } finally {
            db.close();
        }
    }

}
