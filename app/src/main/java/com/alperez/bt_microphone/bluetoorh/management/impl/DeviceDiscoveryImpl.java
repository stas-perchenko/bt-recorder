package com.alperez.bt_microphone.bluetoorh.management.impl;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.alperez.bt_microphone.bluetoorh.management.DeviceDiscovery;
import com.alperez.bt_microphone.bluetoorh.management.OnDeviceDiscoveryListener;
import com.alperez.bt_microphone.model.DiscoveredBluetoothDevice;

/**
 * Created by stanislav.perchenko on 3/10/2017.
 */

public class DeviceDiscoveryImpl implements DeviceDiscovery {

    private Context context;
    private BluetoothAdapter btAdapter;
    private OnDeviceDiscoveryListener listener;


    private boolean released;
    private boolean discoveryRequested;

    public DeviceDiscoveryImpl(Context context, BluetoothAdapter btAdapter) {
        this.context = context;
        this.btAdapter = btAdapter;

        context.registerReceiver(discoverReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    }



    @Override
    public void setOnDeviceDiscoveryListener(OnDeviceDiscoveryListener l) {
        listener = l;
    }


    @Override
    public void resumeDiscovery() {
        if (released) throw new IllegalStateException("Already released");
        if (!discoveryRequested) {
            btAdapter.startDiscovery();
            discoveryRequested = true;
        }
    }

    @Override
    public void stopDiscovery() {
        if (released) throw new IllegalStateException("Already released");
        if (discoveryRequested) {
            btAdapter.cancelDiscovery();
            discoveryRequested = false;
        }
    }



    @Override
    public void release() {
        if (!released) {
            stopDiscovery();
            released = true;
            context.unregisterReceiver(discoverReceiver);
        }
    }

    @Override
    public boolean isDiscovering() {
        return btAdapter.isDiscovering();
    }


    /**********************************************************************************************/
    private final BroadcastReceiver discoverReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (listener != null) {
                BluetoothDevice dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                listener.onDeviceDiscovered(new DiscoveredBluetoothDevice(dev, rssi));
            }
        }
    };


}
