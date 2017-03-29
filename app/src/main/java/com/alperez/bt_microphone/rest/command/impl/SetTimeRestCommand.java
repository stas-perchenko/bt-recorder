package com.alperez.bt_microphone.rest.command.impl;

import android.support.annotation.NonNull;

import com.alperez.bt_microphone.bluetoorh.connector.data.BtDataTransceiver;
import com.alperez.bt_microphone.rest.RestUtils;
import com.alperez.bt_microphone.rest.command.BaseRestCommand;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by stanislav.perchenko on 3/29/2017.
 */

public class SetTimeRestCommand extends BaseRestCommand {
    private static final String COMMAND_NAME = "time";
    private Date time;

    public SetTimeRestCommand(@NonNull BtDataTransceiver dataTransceiver) {
        super(dataTransceiver);
    }

    public void setTime(Date time) {
        this.time = time;
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    protected void fillInRequestBody(JSONObject jBody) throws JSONException {
        jBody.put("time", RestUtils.dateToRemoteString(time));
    }
}
