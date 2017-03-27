package com.alperez.bt_microphone.core.response;

import com.alperez.bt_microphone.core.DeviceFile;

/**
 * Created by Stas on 26.03.2017.
 */

public interface FileSuccessResponse extends BaseResponse {
    DeviceFile getCurrentlySetFile();
}
