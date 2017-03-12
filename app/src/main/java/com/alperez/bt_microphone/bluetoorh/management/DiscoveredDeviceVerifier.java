package com.alperez.bt_microphone.bluetoorh.management;

import com.alperez.bt_microphone.model.DiscoveredBluetoothDevice;
import com.alperez.bt_microphone.model.ValidBtDevice;
import com.alperez.bt_microphone.utils.Callback;

/**
 * Created by stanislav.perchenko on 3/12/2017.
 */

public class DiscoveredDeviceVerifier {
    private final DiscoveredBluetoothDevice device;
    private Callback<ValidBtDevice> resultCallback;


    public static DiscoveredDeviceVerifier createForDevice(DiscoveredBluetoothDevice device) {
        return new DiscoveredDeviceVerifier(device);
    }


    private DiscoveredDeviceVerifier(DiscoveredBluetoothDevice device) {
        this.device = device;
        //TODO Implement this!!!!!!!!!!!!!!!!!!!!!
    }

    public DiscoveredDeviceVerifier withResultCallback(Callback<ValidBtDevice> callback) {
        resultCallback = callback;
        return this;
    }


    public DiscoveredDeviceVerifier start() {
        //TODO Implement this!!!!!!!!!!!!!!!!!!!!!
        return this;
    }

    public void release() {
//TODO Implement this!!!!!!!!!!!!!!!!!!!!!
    }
}
