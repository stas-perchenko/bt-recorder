package com.alperez.bt_microphone.rest.response;

import com.alperez.bt_microphone.core.DeviceState;
import com.alperez.bt_microphone.rest.response.commonmodels.DeviceStatus;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by Stas on 27.03.2017.
 */

class DeviceStatusImpl implements DeviceStatus {

    private long freeSpaceBytes;
    private int batteryLevel;
    private boolean phantomPowerOn;
    private DeviceState deviceState;
    private int recordingSampleRate;
    private int gainLevel;

    public static DeviceStatusImpl fromJson(JSONObject jStatus) throws JSONException {
        DeviceStatusImpl model = new DeviceStatusImpl();

        model.freeSpaceBytes = jStatus.getLong("space");
        model.batteryLevel = jStatus.getInt("battery");
        model.phantomPowerOn = jStatus.getBoolean("phantom");
        try {
            model.deviceState = parseDeviceState(jStatus.getString("state"));
        } catch (IllegalArgumentException e) {
            String err = String.format("Wrong DeviceState enum value %s. Supported values - %s", jStatus.getString("state"), Arrays.toString(DeviceState.values()));
            throw new JSONException(err);
        }
        model.recordingSampleRate = jStatus.getInt("frequency");
        model.gainLevel = jStatus.getInt("gain");

        return model;
    }

    private static DeviceState parseDeviceState(String state) throws JSONException {
        if ("stopped".equals(state)) {
            return DeviceState.STOPPED;
        } else if ("paused".equals(state)) {
            return DeviceState.PAUSED;
        } else if ("recording".equals(state)) {
            return DeviceState.RECORDING;
        } else if ("playing".equals(state)) {
            return DeviceState.PLAYING;
        } else {
            throw new JSONException("Wrong state value - "+state);
        }
    }


    private DeviceStatusImpl(){}

    @Override
    public long freeSpaceBytes() {
        return freeSpaceBytes;
    }

    @Override
    public int batteryLevel() {
        return batteryLevel;
    }

    @Override
    public boolean isPhantomPowerOn() {
        return phantomPowerOn;
    }

    @Override
    public DeviceState deviceState() {
        return deviceState;
    }

    @Override
    public int recordingSampleRate() {
        return recordingSampleRate;
    }

    @Override
    public int gainLevel() {
        return gainLevel;
    }
}
