package com.alperez.bt_microphone.core.response;

import com.alperez.bt_microphone.core.DeviceState;
import com.alperez.bt_microphone.core.DeviceStatus;

import org.json.JSONObject;

/**
 * Created by Stas on 27.03.2017.
 */

class DeviceStatusImpl implements DeviceStatus {


    public static DeviceStatusImpl fromJson(JSONObject jStatus) {
        fgdgfh;
        return null;
    }



    @Override
    public long freeSpaceBytes() {
        return 0;
    }

    @Override
    public int batteryLevel() {
        return 0;
    }

    @Override
    public boolean isPhantomPowerOn() {
        return false;
    }

    @Override
    public DeviceState deviceState() {
        return null;
    }

    @Override
    public int recordingSampleRate() {
        return 0;
    }

    @Override
    public int gainLevel() {
        return 0;
    }
}
