package com.alperez.bt_microphone.bluetoorh.connector;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.alperez.bt_microphone.bluetoorh.BtUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.Executors;

/**
 * Created by stanislav.perchenko on 3/17/2017.
 */

public class BtConnector {


    public static BtConnector forDevice(BluetoothDevice device, UUID serviceUUID) {
        return new BtConnector(device, serviceUUID);
    }


    private BluetoothDevice device;
    private UUID serviceUUID;
    private int preConnectionDelay;
    private OnDeviceConnectListener callback;

    private boolean started;

    private BtConnector(BluetoothDevice device, UUID serviceUUID) {
        this.device = device;
        this.serviceUUID = serviceUUID;
    }

    public synchronized BtConnector preConnectionDelay(int timeMillis) {
        preConnectionDelay = timeMillis;
        return this;
    }

    public synchronized BtConnector connectAsync(OnDeviceConnectListener callback) {
        if (started) throw new IllegalStateException("Already used");
        started = true;
        this.callback = callback;

        Executors.newSingleThreadExecutor().submit(() -> execute());
        return this;
    }


    /**
     * This is the actual background worker
     */
    private void execute() {
        BluetoothSocket soc = null;
        InputStream is = null;
        OutputStream os = null;
        Exception err = null;
        try {
            soc = device.createRfcommSocketToServiceRecord(serviceUUID);
            soc.connect(); // Do the actual connecting job!!!!
            is = soc.getInputStream();
            os = soc.getOutputStream();
        } catch (Exception e) {
            BtUtils.silentlyCloseCloseable(is);
            BtUtils.silentlyCloseCloseable(os);
            BtUtils.silentlyCloseCloseable(soc);
            err = e;
        } finally {
            if (err == null) {
                callback.onConnected(soc, is, os);
            } else {
                callback.onConnectionFailure(err);
            }
        }
    }




    /*****************************  Callback interface of this Connector  *************************/
    public interface OnDeviceConnectListener {
        void onConnected(BluetoothSocket socket, InputStream iStream, OutputStream oStream);
        void onConnectionFailure(Throwable reason);
    }
}
