package com.alperez.bt_microphone.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.alperez.bt_microphone.model.BlacklistedBtDevice;
import com.alperez.bt_microphone.model.ValidBtDevice;

import java.util.Date;

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

    public ValidBtDevice selectValidDeviceById(long id) {
        String where = String.format("%s = %d", BlacklistedBtDevice.COLUMN_ID, id);
        Cursor c = db.query(BlacklistedBtDevice.TABLE_NAME, null, where, null, null, null, null);
        try {
            if (c.moveToFirst()) {
                return ValidBtDevice.builder()
                        .setMacAddress(c.getString(c.getColumnIndex(ValidBtDevice.COLUMN_MAC)))
                        .setDeviceName(c.getString(c.getColumnIndex(ValidBtDevice.COLUMN_ORIG_DEVICE_NAME)))
                        .setSerialNumber(c.getString(c.getColumnIndex(ValidBtDevice.COLUMN_SERIAL_NUM)))
                        .setHardwareVersion(c.getInt(c.getColumnIndex(ValidBtDevice.COLUMN_HARD_VERSION)))
                        .setFirmwareVersion(c.getInt(c.getColumnIndex(ValidBtDevice.COLUMN_SOFT_VERSION)))
                        .setReleaseDate(new Date(c.getLong(c.getColumnIndex(ValidBtDevice.COLUMN_RELEASE_DATE))))
                        .setUserDefinedName(c.getString(c.getColumnIndex(ValidBtDevice.COLUMN_USER_DEVINED_NAME)))
                        .setTimeDiscovered(new Date(c.getLong(c.getColumnIndex(ValidBtDevice.COLUMN_TIME_DISCOVERED))))
                        .setTimeLastConnected(new Date(c.getLong(c.getColumnIndex(ValidBtDevice.COLUMN_TIME_LAST_CONNECTED))))
                        .build();
            } else {
                return null;
            }
        } finally {
            c.close();
        }
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

    public boolean updateValidDeviceTimeLastConnected(ValidBtDevice device) {
        String where = String.format("%s = %d", ValidBtDevice.COLUMN_ID, device.id());
        ContentValues vals = new ContentValues();
        vals.put(ValidBtDevice.COLUMN_TIME_LAST_CONNECTED, device.timeLastConnected().getTime());
        return (db.update(ValidBtDevice.TABLE_NAME, vals, where, null) > 0);
    }

}