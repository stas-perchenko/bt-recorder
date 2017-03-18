package com.alperez.bt_microphone.bluetoorh.connector.sound;

import com.alperez.bt_microphone.bluetoorh.connector.OnTransceiverStatusListener;

/**
 * Created by stanislav.perchenko on 3/18/2017.
 */

public interface BtSoundReceiver {
    void setOnTransceiverStatusListener(OnTransceiverStatusListener l);
    void release();
}
