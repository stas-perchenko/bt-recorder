package com.alperez.bt_microphone.core;

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
