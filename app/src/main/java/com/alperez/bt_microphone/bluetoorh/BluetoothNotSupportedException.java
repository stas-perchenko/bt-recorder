package com.alperez.bt_microphone.bluetoorh;

/**
 * Created by stanislav.perchenko on 3/10/2017.
 */

public class BluetoothNotSupportedException extends Exception {
    public BluetoothNotSupportedException() {
        super("Bluetooth is not supported");
    }
}
