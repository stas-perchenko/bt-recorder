package com.alperez.bt_microphone.bluetoorh.management;

import android.bluetooth.BluetoothDevice;

/**
 * Created by stanislav.perchenko on 3/10/2017.
 */

public interface OnDeviceDiscoveryListener {
    void onDeviceDiscovered(BluetoothDevice device);
}
