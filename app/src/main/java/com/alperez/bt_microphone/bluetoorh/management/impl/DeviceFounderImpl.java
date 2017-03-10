package com.alperez.bt_microphone.bluetoorh.management.impl;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import com.alperez.bt_microphone.bluetoorh.management.DeviceDiscovery;
import com.alperez.bt_microphone.bluetoorh.management.DeviceFounder;
import com.alperez.bt_microphone.bluetoorh.management.OnDeviceFoundListener;
import com.alperez.bt_microphone.model.BlacklistedBtDevice;
import com.alperez.bt_microphone.model.ValidBtDevice;
import com.alperez.bt_microphone.storage.DatabaseAdapter;
import com.alperez.bt_microphone.utils.RunnableSequantialExecuter;

/**
 * Created by stanislav.perchenko on 3/10/2017.
 */

public class DeviceFounderImpl implements DeviceFounder {

    private BluetoothAdapter btAdapter;
    private DeviceDiscovery deviceDiscovery;
    private OnDeviceFoundListener devFoundListener;

    private RunnableSequantialExecuter sequantialExecuter;
    private DatabaseAdapter dbAdapter;

    private volatile boolean isDiscoveryStated;
    private volatile boolean isThisStarted;
    private volatile boolean released;

    public DeviceFounderImpl(@NonNull BluetoothAdapter btAdapter, @NonNull DeviceDiscovery deviceDiscovery) {
        this.btAdapter = btAdapter;
        this.deviceDiscovery = deviceDiscovery;
        deviceDiscovery.setOnDeviceDiscoveryListener(this);
    }

    public void setOnDeviceFoundListener(OnDeviceFoundListener l) {
        devFoundListener = l;
    }


    public void start() {
        if (released) throw new IllegalStateException("Already released");
        synchronized (this) {
            if (sequantialExecuter == null) {
                sequantialExecuter = new RunnableSequantialExecuter();
            }
            if (dbAdapter == null) {
                dbAdapter = new DatabaseAdapter();
            }
            isThisStarted = true;
            startDiscovery();
        }
    }

    public void stop() {
        if (released) throw new IllegalStateException("Already released");
        synchronized (this) {
            isThisStarted = false;
            stopDiscovery();
        }
    }

    /**
     * No operation allowed after this call;
     */
    public void release() {
        if (!released) {
            synchronized (this) {
                if (sequantialExecuter != null) {
                    sequantialExecuter.release();
                }
                if (dbAdapter != null) {
                    dbAdapter.close();
                }
                released = true;
            }
        }
    }

    private synchronized void startDiscovery() {
        if (!isDiscoveryStated) {
            isDiscoveryStated = true;
            deviceDiscovery.resumeDiscovery();
        }
    }

    private synchronized void stopDiscovery() {
        if (isDiscoveryStated) {
            isDiscoveryStated = false;
            deviceDiscovery.stopDiscovery();
        }
    }

    @Override
    public void onDeviceDiscovered(BluetoothDevice device) {
        sequantialExecuter.enqueueRunnable(() -> doJobOnDiscoveredDevice(device));
    }

    /**
     * This is the entry points of handling newly discovered devices.
     * This method must be called from the background thread.
     * A working result is reported using the Result Handler
     * @param device
     */
    private void doJobOnDiscoveredDevice(BluetoothDevice device) {

    }




























    /**
     * The handler which posts out result of discovery to a client in the UI thread.
     */
    private Handler resultHandlerToUi = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (resultHandlerToUi != null) {
                if ((msg.obj != null) && (msg.obj instanceof ValidBtDevice)) {
                    devFoundListener.onValidDeviceFouns((ValidBtDevice) msg.obj);
                } else if ((msg.obj != null) && (msg.obj instanceof BlacklistedBtDevice)) {
                    devFoundListener.onBlacklistedDeviceFound((BlacklistedBtDevice) msg.obj);
                }
            }
        }
    };
}
