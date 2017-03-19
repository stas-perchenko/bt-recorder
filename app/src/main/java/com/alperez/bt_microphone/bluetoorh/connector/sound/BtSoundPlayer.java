package com.alperez.bt_microphone.bluetoorh.connector.sound;

import com.alperez.bt_microphone.bluetoorh.connector.OnConnectionStatusListener;

/**
 * Created by stanislav.perchenko on 3/19/2017.
 */

public interface BtSoundPlayer {
    void setOnPlayerPerformanceListener(OnPlayerPerformanceListener l);
    void setOnConnectionStatusListener(OnConnectionStatusListener l);
    void play();
    void pause();
    void release();

    boolean isConnected();
    boolean isPlaying();
    boolean isAudioTrackPlaying();
}
