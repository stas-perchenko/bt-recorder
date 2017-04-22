package com.alperez.bt_microphone.rest.command.impl;

import android.location.Location;
import android.support.annotation.NonNull;

import com.alperez.bt_microphone.bluetoorh.connector.data.BtDataTransceiver;
import com.alperez.bt_microphone.rest.command.BaseRestCommand;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by stanislav.perchenko on 4/21/2017.
 */

public class SetLocationLommand extends BaseRestCommand {
    private static final String COMMAND_NAME = "geo";
    private Location location;

    public SetLocationLommand(@NonNull BtDataTransceiver dataTransceiver) {
        super(dataTransceiver);
    }


    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    protected void fillInRequestBody(JSONObject jBody) throws JSONException {
        jBody.put("lat", String.format("%.8f",location.getLatitude()));
        jBody.put("lon", String.format("%.8f",location.getLongitude()));
    }
}
