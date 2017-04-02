package com.alperez.bt_microphone.rest.response;

import android.location.Location;

import com.alperez.bt_microphone.rest.response.commonmodels.DeviceFile;
import com.alperez.bt_microphone.rest.RestUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by Stas on 27.03.2017.
 */

class DeviceFileImpl implements DeviceFile {

    private Date startTime;
    private long durationMillis;
    private long currentPosition;
    private int sampleRate;
    private Location geoLocation;

    public static DeviceFileImpl fromJson(JSONObject jFile) throws JSONException {
        DeviceFileImpl model = new DeviceFileImpl();
        String dt = jFile.getString("time");
        try {
            model.startTime = RestUtils.parseRemoteDateTime(dt);
        } catch (ParseException e) {
            throw new JSONException("error parse date/time - "+dt);
        }
        model.durationMillis = RestUtils.parseLongOptString(jFile, "duration");
        model.currentPosition = RestUtils.parseLongOptString(jFile, "position");
        model.sampleRate = RestUtils.parseIntOptString(jFile, "freq");
        model.geoLocation = RestUtils.parseLocationFromJson(jFile);
        return model;
    }


    private DeviceFileImpl(){}

    @Override
    public Date startTime() {
        return startTime;
    }

    @Override
    public long durationMillis() {
        return durationMillis;
    }

    @Override
    public long currentPosition() {
        return currentPosition;
    }

    @Override
    public int sampleRate() {
        return sampleRate;
    }

    @Override
    public Location geoLocation() {
        return geoLocation;
    }
}
