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
public abstract class ValidBtDevice extends BaseDbModel implements Parcelable, Cloneable {
    public static final String TABLE_NAME = "valid-devices";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_MAC = "mac";
    public static final String COLUMN_SERIAL_NUM = "serial";
    public static final String COLUMN_HARD_VERSION = "h_version";
    public static final String COLUMN_SOFT_VERSION = "s_version";
    public static final String COLUMN_RELEASE_DATE = "release_date";
    public static final String COLUMN_LOCAL_NAME = "local_name";
    public static final String COLUMN_TIME_DISCOVERED = "discovered_at";

    //--- Storeable fields ---
    public abstract String macAddress();
    public abstract String serialNumber();
    public abstract int hardwareVersion();
    public abstract int firmwareVersion();
    public abstract Date releaseDate();
    @Nullable
    public abstract String userDefinedName();
    public abstract Date timeDiscovered();

    //--- Non-storeable runtime-calculated fields ---
    @Nullable
    public abstract BluetoothDevice bluetoothDevice();


    @Override
    public long id() {
        return macAddress().hashCode();
    }


    public static Builder builder() {
        return new AutoValue_ValidBtDevice.Builder();
    }


    public abstract Builder toBuilder();

    public ValidBtDevice withBluetoothDevice(@Nullable BluetoothDevice device) {
        return toBuilder().setBluetoothDevice(device).build();
    }

    public ValidBtDevice clone() {
        return builder()
                .setMacAddress(macAddress())
                .setSerialNumber(serialNumber())
                .setHardwareVersion(hardwareVersion())
                .setFirmwareVersion(firmwareVersion())
                .setReleaseDate(releaseDate())
                .setUserDefinedName(userDefinedName())
                .setTimeDiscovered(timeDiscovered())
                .setBluetoothDevice(bluetoothDevice())
                .build();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setMacAddress(String macAddress);
        public abstract Builder setSerialNumber(String serialNumber);
        public abstract Builder setHardwareVersion(int hardwareVersion);
        public abstract Builder setFirmwareVersion(int firmwareVersion);
        public abstract Builder setReleaseDate(Date releaseDate);
        public abstract Builder setUserDefinedName(@Nullable String userDefinedName);
        public abstract Builder setTimeDiscovered(Date timeDiscovered);

        public abstract Builder setBluetoothDevice(BluetoothDevice bluetoothDevice);

        public abstract ValidBtDevice build();
    }
}
