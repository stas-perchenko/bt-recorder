package com.alperez.bt_microphone.model;

import android.bluetooth.BluetoothDevice;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

import java.util.Date;

/**
 * Created by stanislav.perchenko on 3/9/2017.
 */
@AutoValue
public abstract class BlacklistedBtDevice extends BaseDbModel implements Parcelable, Cloneable {
    public static final String TABLE_NAME = "blacklisted_devices";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_MAC = "mac";
    public static final String COLUMN_DEVICE_NAME = "dev_name";
    public static final String COLUMN_TIME_DISCOVERED = "discovered_at";


    public static BlacklistedBtDevice create(String macAddress,@Nullable String deviceName, Date timeDiscovered, @Nullable BluetoothDevice btDevice) {
        return new AutoValue_BlacklistedBtDevice(macAddress, deviceName, timeDiscovered, btDevice);
    }

    //--- Storeable fields ---
    public abstract String macAddress();
    @Nullable
    public abstract String deviceName();
    public abstract Date timeDiscovered();

    //--- Non-storeable runtime-calculated fields ---
    @Nullable
    public abstract BluetoothDevice bluetoothDevice();

    @Override
    protected String getStringForId() {
        return macAddress();
    }



    public BlacklistedBtDevice clone() {
        return create(macAddress(), deviceName(), new Date(timeDiscovered().getTime()), bluetoothDevice());
    }

    public BlacklistedBtDevice withBluetoothDevice(@Nullable BluetoothDevice device) {
        return BlacklistedBtDevice.create(macAddress(), deviceName(), new Date(timeDiscovered().getTime()), device);
    }



}
