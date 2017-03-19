package com.alperez.bt_microphone.bluetoorh.connector.sound;

import com.alperez.bt_microphone.bluetoorh.connector.OnConnectionStatusListener;

/**
 * This is for testing
 *
 *
 * Created by stanislav.perchenko on 3/18/2017.
 */

public interface BtSoundReceiver {
    void setOnTransceiverStatusListener(OnConnectionStatusListener l);
    void release();
}
