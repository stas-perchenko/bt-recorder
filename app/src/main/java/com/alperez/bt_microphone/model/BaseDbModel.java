package com.alperez.bt_microphone.model;

/**
 * Created by Stas on 20.02.2017.
 */

public abstract class BaseDbModel implements Cloneable {
    public abstract long id();

    public abstract BaseDbModel clone();
}
