package com.alperez.bt_microphone.bluetoorh.connector;

/**
 * Created by stanislav.perchenko on 3/18/2017.
 */

public interface OnTransceiverStatusListener {
    void onConnectionRestorted(int nTry);
    void onConnectionAttemptFailed(int nTry);
    void onConnectionBroken(String nameThreadCauseFailure, Throwable reason);
}
