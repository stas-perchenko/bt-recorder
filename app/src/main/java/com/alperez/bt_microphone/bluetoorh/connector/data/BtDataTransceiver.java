package com.alperez.bt_microphone.bluetoorh.connector.data;

import com.alperez.bt_microphone.bluetoorh.connector.OnConnectionStatusListener;

/**
 * Created by stanislav.perchenko on 3/18/2017.
 */

public interface BtDataTransceiver {
    void setOnTransceiverStatusListener(OnConnectionStatusListener l);
    void sendDataNonBlocked(String data);
    void release();
}
