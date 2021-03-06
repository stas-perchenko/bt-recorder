package com.alperez.bt_microphone.model;

import android.bluetooth.BluetoothDevice;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.alperez.bt_microphone.GlobalConstants;
import com.alperez.bt_microphone.bluetoorh.connector.data.BtDataTransceiver;
import com.alperez.bt_microphone.bluetoorh.connector.data.BtDataTransceiverImpl;
import com.alperez.bt_microphone.ui.viewmodel.BtDeviceViewModel;
import com.google.auto.value.AutoValue;

import java.util.Date;

/**
 * Created by stanislav.perchenko on 3/9/2017.
 */
@AutoValue
public abstract class ValidDeviceDbModel extends BaseDbModel implements BtDeviceViewModel, Parcelable, Cloneable {
    public static final String TABLE_NAME = "valid_devices";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_MAC = "mac";
    public static final String COLUMN_ORIG_DEVICE_NAME = "dev_name";
    public static final String COLUMN_SERIAL_NUM = "serial";
    public static final String COLUMN_HARD_VERSION = "h_version";
    public static final String COLUMN_SOFT_VERSION = "s_version";
    public static final String COLUMN_RELEASE_DATE = "release_date";
    public static final String COLUMN_USER_DEVINED_NAME = "local_name";
    public static final String COLUMN_TIME_DISCOVERED = "discovered_at";
    public static final String COLUMN_TIME_LAST_CONNECTED = "last_time_connected";


    //--- Storeable fields ---
    public abstract String macAddress();
    @Nullable
    public abstract String deviceName();
    public abstract String serialNumber();
    public abstract int hardwareVersion();
    public abstract int firmwareVersion();
    public abstract Date releaseDate();
    @Nullable
    public abstract String userDefinedName();
    public abstract Date timeDiscovered();
    public abstract Date timeLastConnected();

    public String hardwareVersionText() {
        return Integer.toString(hardwareVersion());
    }

    public String firmwareVersionText() {
        return Integer.toString(firmwareVersion());
    }

    //--- Non-storeable runtime-calculated fields ---
    @Nullable
    public abstract BluetoothDevice bluetoothDevice();


    @Override
    protected String getStringForId() {
        return macAddress();
    }

    public static Builder builder() {
        return new AutoValue_ValidDeviceDbModel.Builder();
    }


    public abstract Builder toBuilder();

    public ValidDeviceDbModel withUserDefinedName(@Nullable String userDefinedName) {
        return toBuilder().setUserDefinedName(userDefinedName).build();
    }

    public ValidDeviceDbModel withTimeDiscovered(Date timeDiscovered) {
        return toBuilder().setTimeDiscovered(timeDiscovered).build();
    }

    public ValidDeviceDbModel withBluetoothDevice(@Nullable BluetoothDevice device) {
        ValidDeviceDbModel newModel = toBuilder().setBluetoothDevice(device).build();
        newModel.dataTransceiver = null;
        return newModel;
    }

    public ValidDeviceDbModel withTimeLastConnected(Date timeLastConnected) {
        return toBuilder().setTimeLastConnected(new Date(timeLastConnected.getTime())).build();
    }

    public ValidDeviceDbModel clone() {
        return builder()
                .setMacAddress(macAddress())
                .setDeviceName(deviceName())
                .setSerialNumber(serialNumber())
                .setHardwareVersion(hardwareVersion())
                .setFirmwareVersion(firmwareVersion())
                .setReleaseDate(releaseDate())
                .setUserDefinedName(userDefinedName())
                .setTimeDiscovered(new Date(timeDiscovered().getTime()))
                .setBluetoothDevice(bluetoothDevice())
                .setTimeLastConnected(timeLastConnected())
                .build();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setMacAddress(String macAddress);
        public abstract Builder setDeviceName(String deviceName);
        public abstract Builder setSerialNumber(String serialNumber);
        public abstract Builder setHardwareVersion(int hardwareVersion);
        public abstract Builder setFirmwareVersion(int firmwareVersion);
        public abstract Builder setReleaseDate(Date releaseDate);
        public abstract Builder setUserDefinedName(@Nullable String userDefinedName);
        public abstract Builder setTimeDiscovered(Date timeDiscovered);
        public abstract Builder setTimeLastConnected(Date timeLastConnected);

        public abstract Builder setBluetoothDevice(BluetoothDevice bluetoothDevice);

        public abstract ValidDeviceDbModel build();
    }


    public void setRssi(short rssi) {
        this.rssi = rssi;
    }
    /*********************  the BtDeviceViewModel implementation  *********************************/
    private short rssi;
    @Override
    public int getDeviceType() {
        return BtDeviceViewModel.DEVICE_TYPE_VALID_SAVED;
    }

    @Override
    public String getName() {
        return userDefinedName();
    }

    @Override
    public String getMacAddress() {
        return macAddress();
    }

    @Override
    public short getRSSI() {
        return rssi;
    }

    @Nullable
    @Override
    public Date getTimeFirstDiscovered() {
        return timeDiscovered();
    }

    @Nullable
    @Override
    public BluetoothDevice getDevice() {
        return bluetoothDevice();
    }


    private BtDataTransceiver dataTransceiver;

    public synchronized BtDataTransceiver getDataTransceiver() {
        if (dataTransceiver == null) {
            dataTransceiver = new BtDataTransceiverImpl(bluetoothDevice(), GlobalConstants.UUID_SERVICE_1);
        }
        return dataTransceiver;
    }

    public synchronized void releaseDataTransceiver() {
        if (dataTransceiver != null) {
            dataTransceiver.release();
            dataTransceiver = null;
        }
    }

}
