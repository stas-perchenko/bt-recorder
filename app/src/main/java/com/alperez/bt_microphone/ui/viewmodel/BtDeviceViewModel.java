package com.alperez.bt_microphone.ui.viewmodel;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.Nullable;

import java.util.Date;

/**
 * Created by stanislav.perchenko on 3/12/2017.
 */

public interface BtDeviceViewModel {
    int DEVICE_TYPE_INVALID = 0;
    int DEVICE_TYPE_VALID_NEW = 1;
    int DEVICE_TYPE_VALID_SAVED = 2;


    int getDeviceType();
    String getName();
    String getMacAddress();
    short getRSSI();
    @Nullable
    Date getTimeFirstDiscovered();
    @Nullable
    BluetoothDevice getDevice();
}
