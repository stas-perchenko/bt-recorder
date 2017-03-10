package com.alperez.bt_microphone.bluetoorh.management;

/**
 * The interface of entities which can discover devices.
 * It has actions to start/pause/resume discoveryprocess
 * and the callback for discovered devices
 *
 * Created by stanislav.perchenko on 3/9/2017.
 */

public interface DeviceDiscovery {

    void resumeDiscovery();
    void stopDiscovery();

    void setOnDeviceDiscoveryListener(OnDeviceDiscoveryListener l);
}
