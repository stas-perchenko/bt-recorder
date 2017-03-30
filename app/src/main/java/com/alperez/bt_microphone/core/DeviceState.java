package com.alperez.bt_microphone.core;

/**
 * Created by Stas on 26.03.2017.
 */

public enum DeviceState {
    UNDEFINED("Unknown!"),

    STOPPED("Stopped"),
    PLAYING("Playing"),
    RECORDING("Recording"),
    PAUSED("Paused"),

    START_PLAYING("Start Playing..."),
    START_RECORDING("Start Recording..."),
    STOPPING("Stopping..."),
    PAUSING("Pausing...");

    String uiTextValue;

    DeviceState(String uiTextValue) {
        this.uiTextValue = uiTextValue;
    }

    public String getUiTextValue() {
        return uiTextValue;
    }
}
