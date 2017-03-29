package com.alperez.bt_microphone.rest.command.impl;

import android.support.annotation.NonNull;

import com.alperez.bt_microphone.bluetoorh.connector.data.BtDataTransceiver;
import com.alperez.bt_microphone.rest.command.BaseRestCommand;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Stas on 29.03.2017.
 */

public class PhantomOffRestCommand extends BaseRestCommand {
    private static final String COMMAND_NAME = "phantomoff";

    public PhantomOffRestCommand(@NonNull BtDataTransceiver dataTransceiver) {
        super(dataTransceiver);
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    protected void fillInRequestBody(JSONObject jBody) throws JSONException {
        //No options
    }
}
