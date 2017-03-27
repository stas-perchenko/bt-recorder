package com.alperez.bt_microphone.core.response;

import com.alperez.bt_microphone.core.DeviceStatus;

/**
 * Created by Stas on 26.03.2017.
 */

public interface StatusSuccessResponse extends BaseResponse {
    DeviceStatus getDeviceStatus();
}
