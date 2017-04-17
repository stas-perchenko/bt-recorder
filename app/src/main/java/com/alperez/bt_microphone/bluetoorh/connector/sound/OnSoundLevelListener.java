package com.alperez.bt_microphone.bluetoorh.connector.sound;

/**
 * Created by stanislav.perchenko on 4/11/2017.
 */

public interface OnSoundLevelListener {
    void onLevelUpdated(float rms, int peak);
}
