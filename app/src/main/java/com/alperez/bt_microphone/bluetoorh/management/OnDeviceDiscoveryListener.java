package com.alperez.bt_microphone.bluetoorh.management;

import com.alperez.bt_microphone.model.DiscoveredBluetoothDevice;

/**
 * Created by stanislav.perchenko on 3/10/2017.
 */

public interface OnDeviceDiscoveryListener {
    void onDeviceDiscovered(DiscoveredBluetoothDevice device);
}
