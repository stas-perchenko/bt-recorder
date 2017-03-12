package com.alperez.bt_microphone.model;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.Nullable;

import com.alperez.bt_microphone.ui.viewmodel.BtDeviceViewModel;

import java.util.Date;

/**
 * Created by stanislav.perchenko on 3/10/2017.
 */

public class DiscoveredBluetoothDevice implements BtDeviceViewModel {
    private BluetoothDevice device;
    private short rssi;

    public DiscoveredBluetoothDevice(BluetoothDevice device, short rssi) {
        this.device = device;
        this.rssi = rssi;
    }


    /*********************  the BtDeviceViewModel implementation  *********************************/
    @Override
    public int getDeviceType() {
        return BtDeviceViewModel.DEVICE_TYPE_VALID_NEW;
    }

    @Override
    public String getName() {
        return device.getName();
    }

    @Override
    public String getMacAddress() {
        return device.getAddress();
    }

    @Override
    public short getRSSI() {
        return rssi;
    }

    @Nullable
    @Override
    public Date getTimeFirstDiscovered() {
        return null;
    }

    @Override
    public BluetoothDevice getDevice() {
        return device;
    }
}
