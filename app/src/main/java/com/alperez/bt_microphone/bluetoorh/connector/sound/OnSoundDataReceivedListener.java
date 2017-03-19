package com.alperez.bt_microphone.bluetoorh.connector.sound;

/**
 * Created by stanislav.perchenko on 3/18/2017.
 */

public interface OnSoundDataReceivedListener {
    void onDataReceiver(byte[] buffer, int offcet, int nBytes);
    void onDataRateMeasured(float bps);
}
