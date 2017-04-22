package com.alperez.bt_microphone.rest.response.commonmodels;

import android.location.Location;

import com.alperez.bt_microphone.core.DeviceState;

import java.util.Date;

/**
 * Created by Stas on 26.03.2017.
 */

public interface DeviceStatus {
    Date deviceTime();
    long freeSpaceBytes();
    int batteryLevel();
    boolean isPhantomPowerOn();
    DeviceState deviceState();
    int recordingSampleRate();
    int gainLevel();
    Location deviceLocation();
    int memoryCardstatus();
}
