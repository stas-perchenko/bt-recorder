package com.alperez.bt_microphone.rest.response.commonmodels;

import com.alperez.bt_microphone.core.DeviceState;

/**
 * Created by Stas on 26.03.2017.
 */

public interface DeviceStatus {
    long freeSpaceBytes();
    int batteryLevel();
    boolean isPhantomPowerOn();
    DeviceState deviceState();
    int recordingSampleRate();
    int gainLevel();
}
