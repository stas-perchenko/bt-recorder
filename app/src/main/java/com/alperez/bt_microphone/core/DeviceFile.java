package com.alperez.bt_microphone.core;

import android.location.Location;

import java.util.Date;

/**
 * Created by Stas on 26.03.2017.
 */

public interface DeviceFile {
    Date startTime();
    long durationMillis();
    int sampleRate();
    Location geoLocation();
}
