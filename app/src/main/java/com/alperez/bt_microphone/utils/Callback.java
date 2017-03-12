package com.alperez.bt_microphone.utils;

/**
 * Created by stanislav.perchenko on 3/12/2017.
 */

public interface Callback<T> {
    void onComplete(T result);
    void onError(Throwable  error);
}
