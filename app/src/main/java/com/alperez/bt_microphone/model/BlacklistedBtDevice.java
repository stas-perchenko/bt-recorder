package com.alperez.bt_microphone.model;

import android.os.Parcelable;

import com.google.auto.value.AutoValue;

import java.util.Date;

/**
 * Created by stanislav.perchenko on 3/9/2017.
 */
@AutoValue
public abstract class BlacklistedBtDevice extends BaseDbModel implements Parcelable, Cloneable {
    public static final String TABLE_NAME = "blacklisted-devices";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_MAC = "mac";
    public static final String COLUMN_TIME_DISCOVERED = "discovered_at";


    public static BlacklistedBtDevice create(String macAddress, Date timeDiscovered) {
        return new AutoValue_BlacklistedBtDevice(macAddress, timeDiscovered);
    }

    public abstract String macAddress();
    public abstract Date timeDiscovered();

    @Override
    public long id() {
        return macAddress().hashCode();
    }



    public BlacklistedBtDevice clone() {
        return create(macAddress(), new Date(timeDiscovered().getTime()));
    }


}
