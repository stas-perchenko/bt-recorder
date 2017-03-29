package com.alperez.bt_microphone.rest.response;

import com.alperez.bt_microphone.rest.response.commonmodels.DevicePosition;

/**
 * Created by stanislav.perchenko on 3/29/2017.
 */

public interface PositionSuccessResponse extends BaseResponse {
    DevicePosition getCurrentposition();
}
