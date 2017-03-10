package com.alperez.bt_microphone.bluetoorh.management;

/**
 * Created by stanislav.perchenko on 3/10/2017.
 */

public interface DeviceFounder extends OnDeviceDiscoveryListener {
    void setOnDeviceFoundListener(OnDeviceFoundListener l);

    void start();
    void stop();
    void release();

    boolean isStarted();
}
