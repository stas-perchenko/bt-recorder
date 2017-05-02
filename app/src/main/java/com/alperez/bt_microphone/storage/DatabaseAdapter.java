package com.alperez.bt_microphone.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.alperez.bt_microphone.model.BlacklistedBtDevice;
import com.alperez.bt_microphone.model.ValidDeviceDbModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by stanislav.perchenko on 3/9/2017.
 */

public class DatabaseAdapter {
    private final static String TAG = "DatabaseAdapter";
    private static final boolean D = true;

    //Variable to hold the database instance
    private SQLiteDatabase db;

    /**
     * Constructor. DB instance is also oped here!!! So separate open methods are not implemented
     * DatabaseManager must be init before calling this constructor.
     */
    public DatabaseAdapter() {
        this.db = DatabaseManager.getInstance().openDatabase();
    }

    /**
     * Close connection to the database
     */
    public synchronized void close() {
        DatabaseManager.getInstance().closeDatabase();
    }

    /**
     * Start transaction
     */
    public void beginTransaction() {
        db.beginTransaction();
    }

    /**
     * Commit transaction
     */
    public void commitTransaction() {
        db.setTransactionSuccessful();
    }

    /**
     * End transaction
     */
    public void endTransaction() {
        db.endTransaction();
    }


    /**********************************************************************************************/
    /******************************  Selection methods  *******************************************/
    /**********************************************************************************************/

    public BlacklistedBtDevice selectBlacklistedDeviceById(long id) {
        String where = String.format("%s = %d", BlacklistedBtDevice.COLUMN_ID, id);
        Cursor c = db.query(BlacklistedBtDevice.TABLE_NAME, null, where, null, null, null, null);
        try {
            if (c.moveToFirst()) {
                String mac = c.getString(c.getColumnIndex(BlacklistedBtDevice.COLUMN_MAC));
                String name = c.getString(c.getColumnIndex(BlacklistedBtDevice.COLUMN_DEVICE_NAME));
                long time = c.getLong(c.getColumnIndex(BlacklistedBtDevice.COLUMN_TIME_DISCOVERED));
                return BlacklistedBtDevice.create(mac, name, new Date(time), null);
            } else {
                return null;
            }
        } finally {
            c.close();
        }
    }

    public ValidDeviceDbModel selectValidDeviceById(long id) {
        String where = String.format("%s = %d", BlacklistedBtDevice.COLUMN_ID, id);
        Cursor c = db.query(ValidDeviceDbModel.TABLE_NAME, null, where, null, null, null, null);
        try {
            if (c.moveToFirst()) {
                return buildValidDeviceFromCursor(c);
            } else {
                return null;
            }
        } finally {
            c.close();
        }
    }

    public List<ValidDeviceDbModel> selectAllValidDevices() {
        Cursor c = db.query(ValidDeviceDbModel.TABLE_NAME, null, null, null, null, null, ValidDeviceDbModel.COLUMN_TIME_LAST_CONNECTED+" DESC");
        try {
            List<ValidDeviceDbModel> result = new ArrayList<>(5);
            if (c.moveToFirst()) {
                do {
                    result.add(buildValidDeviceFromCursor(c));
                } while (c.moveToNext());
            }
            return result;
        } finally {
            c.close();
        }
    }

    private ValidDeviceDbModel buildValidDeviceFromCursor(Cursor c) {
        return ValidDeviceDbModel.builder()
                .setMacAddress(c.getString(c.getColumnIndex(ValidDeviceDbModel.COLUMN_MAC)))
                .setDeviceName(c.getString(c.getColumnIndex(ValidDeviceDbModel.COLUMN_ORIG_DEVICE_NAME)))
                .setSerialNumber(c.getString(c.getColumnIndex(ValidDeviceDbModel.COLUMN_SERIAL_NUM)))
                .setHardwareVersion(c.getInt(c.getColumnIndex(ValidDeviceDbModel.COLUMN_HARD_VERSION)))
                .setFirmwareVersion(c.getInt(c.getColumnIndex(ValidDeviceDbModel.COLUMN_SOFT_VERSION)))
                .setReleaseDate(new Date(c.getLong(c.getColumnIndex(ValidDeviceDbModel.COLUMN_RELEASE_DATE))))
                .setUserDefinedName(c.getString(c.getColumnIndex(ValidDeviceDbModel.COLUMN_USER_DEVINED_NAME)))
                .setTimeDiscovered(new Date(c.getLong(c.getColumnIndex(ValidDeviceDbModel.COLUMN_TIME_DISCOVERED))))
                .setTimeLastConnected(new Date(c.getLong(c.getColumnIndex(ValidDeviceDbModel.COLUMN_TIME_LAST_CONNECTED))))
                .build();
    }

    /**********************************************************************************************/
    /******************************  Insert/Update methods  ***************************************/
    /**********************************************************************************************/
    public long insertDeviceToBlacklist(BlacklistedBtDevice device) {
        ContentValues vals = new ContentValues();
        vals.put(BlacklistedBtDevice.COLUMN_ID, device.id());
        vals.put(BlacklistedBtDevice.COLUMN_MAC, device.macAddress());
        vals.put(BlacklistedBtDevice.COLUMN_DEVICE_NAME, device.deviceName());
        vals.put(BlacklistedBtDevice.COLUMN_TIME_DISCOVERED, device.timeDiscovered().getTime());
        return db.insert(BlacklistedBtDevice.TABLE_NAME, null, vals);
    }


    public long insertValidDevice(ValidDeviceDbModel device) {
        ContentValues vals = new ContentValues();
        vals.put(ValidDeviceDbModel.COLUMN_ID, device.id());
        vals.put(ValidDeviceDbModel.COLUMN_MAC, device.macAddress());
        vals.put(ValidDeviceDbModel.COLUMN_ORIG_DEVICE_NAME, device.deviceName());
        vals.put(ValidDeviceDbModel.COLUMN_SERIAL_NUM, device.serialNumber());
        vals.put(ValidDeviceDbModel.COLUMN_HARD_VERSION, device.hardwareVersion());
        vals.put(ValidDeviceDbModel.COLUMN_SOFT_VERSION, device.firmwareVersion());
        vals.put(ValidDeviceDbModel.COLUMN_RELEASE_DATE, device.releaseDate().getTime());
        vals.put(ValidDeviceDbModel.COLUMN_USER_DEVINED_NAME, device.userDefinedName());
        vals.put(ValidDeviceDbModel.COLUMN_TIME_DISCOVERED, device.timeDiscovered().getTime());
        if (device.timeLastConnected() != null) {
            vals.put(ValidDeviceDbModel.COLUMN_TIME_LAST_CONNECTED, device.timeLastConnected().getTime());
        }

        return db.insert(ValidDeviceDbModel.TABLE_NAME, null, vals);
    }

    public boolean updateValidDeviceTimeLastConnected(ValidDeviceDbModel device) {
        String where = String.format("%s = '%s'", ValidDeviceDbModel.COLUMN_MAC, device.macAddress());
        ContentValues vals = new ContentValues();
        vals.put(ValidDeviceDbModel.COLUMN_TIME_LAST_CONNECTED, device.timeLastConnected().getTime());
        return (db.update(ValidDeviceDbModel.TABLE_NAME, vals, where, null) > 0);
    }


    public int clearValidDevices() {
        return db.delete(ValidDeviceDbModel.TABLE_NAME, null, null);
    }

    public int clearBlacklistedDevices() {
        return db.delete(BlacklistedBtDevice.TABLE_NAME, null, null);
    }

}
