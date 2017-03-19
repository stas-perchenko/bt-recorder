package com.alperez.bt_microphone.bluetoorh.connector.sound;

/**
 * Created by stanislav.perchenko on 3/19/2017.
 */

public interface OnPlayerPerformanceListener {
    void onBytesReceived(int nBytes);
    void onBytesPlayed(int nBytes);
}
