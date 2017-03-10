package com.alperez.bt_microphone.bluetoorh.management;

import com.alperez.bt_microphone.model.BlacklistedBtDevice;
import com.alperez.bt_microphone.model.ValidBtDevice;

/**
 * Created by stanislav.perchenko on 3/10/2017.
 */

public interface OnDeviceFoundListener {
    void onValidDeviceFouns(ValidBtDevice device);
    void onBlacklistedDeviceFound(BlacklistedBtDevice device);
}
