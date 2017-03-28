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
        model.durationMillis = jFile.getLong("dur");
        model.sampleRate = jFile.getInt("freq");
        model.geoLocation = new Location("GLONASS");
        model.geoLocation.setLatitude(jFile.getDouble("lat"));
        model.geoLocation.setLongitude(jFile.getDouble("lon"));
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
    public int sampleRate() {
        return sampleRate;
    }

    @Override
    public Location geoLocation() {
        return geoLocation;
    }
}
