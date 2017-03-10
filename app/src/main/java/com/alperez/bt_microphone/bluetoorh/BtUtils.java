package com.alperez.bt_microphone.bluetoorh;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;

/**
 * Created by stanislav.perchenko on 3/9/2017.
 */

public class BtUtils {
    @SuppressLint("NewApi")
    public static BluetoothAdapter getBtAdapter(Context ctx) throws BluetoothNotSupportedException {

        BluetoothAdapter ad = (Build.VERSION.SDK_INT >= 18) ? ((BluetoothManager) ctx.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter() : BluetoothAdapter.getDefaultAdapter();
        if (ad != null) {
            return ad;
        } else {
            throw new BluetoothNotSupportedException();
        }
    }
}
