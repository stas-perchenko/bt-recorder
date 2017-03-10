package com.alperez.bt_microphone.model;

/**
 * Created by Stas on 20.02.2017.
 */

public abstract class BaseDbModel implements Cloneable {
    protected abstract String getStringForId();

    public abstract BaseDbModel clone();

    public final long id() {
        long h = 1125899906842597L; // prime
        String text = getStringForId();
        int len = text.length();

        for (int i = 0; i < len; i++) {
            h = 31*h + text.charAt(i);
        }
        return h;
    }
}
