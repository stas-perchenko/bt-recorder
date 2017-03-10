package com.alperez.bt_microphone.model;

import android.bluetooth.BluetoothDevice;

/**
 * Created by stanislav.perchenko on 3/10/2017.
 */

public class DiscoveredBluetoothDevice {
    private BluetoothDevice device;
    private short rssi;

    public DiscoveredBluetoothDevice(BluetoothDevice device, short rssi) {
        this.device = device;
        this.rssi = rssi;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public short getRssi() {
        return rssi;
    }
}
