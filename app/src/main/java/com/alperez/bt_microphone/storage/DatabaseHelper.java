package com.alperez.bt_microphone.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.alperez.bt_microphone.GlobalConstants;
import com.alperez.bt_microphone.model.BlacklistedBtDevice;
import com.alperez.bt_microphone.model.ValidDeviceDbModel;

/**
 * Created by stanislav.perchenko on 3/9/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private volatile static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (DatabaseHelper.class) {
                if (instance == null) {
                    instance = new DatabaseHelper(context.getApplicationContext());
                }
            }
        }
        return instance;
    }


    private DatabaseHelper(Context context) {
        super(context, GlobalConstants.DB_NAME, null, GlobalConstants.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {





        db.execSQL(String.format("CREATE TABLE %11$s (%1$s INTEGER PRIMARY KEY, %2$s TEXT, %3$s TEXT, %4$s TEXT, %5$s INTEGER, %6$s INTEGER, %7$s INTEGER, %8$s TEXT, %9$s INTEGER, %10$s INTEGER);",
                ValidDeviceDbModel.COLUMN_ID,
                ValidDeviceDbModel.COLUMN_MAC,
                ValidDeviceDbModel.COLUMN_ORIG_DEVICE_NAME,
                ValidDeviceDbModel.COLUMN_SERIAL_NUM,
                ValidDeviceDbModel.COLUMN_HARD_VERSION,
                ValidDeviceDbModel.COLUMN_SOFT_VERSION,
                ValidDeviceDbModel.COLUMN_RELEASE_DATE,
                ValidDeviceDbModel.COLUMN_USER_DEVINED_NAME,
                ValidDeviceDbModel.COLUMN_TIME_DISCOVERED,
                ValidDeviceDbModel.COLUMN_TIME_LAST_CONNECTED, ValidDeviceDbModel.TABLE_NAME));

        db.execSQL(String.format("CREATE TABLE %5$s (%1$s INTEGER PRIMARY KEY, %2$s TEXT, %3$s TEXT, %4$s INTEGER);",
                BlacklistedBtDevice.COLUMN_ID,
                BlacklistedBtDevice.COLUMN_MAC,
                BlacklistedBtDevice.COLUMN_DEVICE_NAME,
                BlacklistedBtDevice.COLUMN_TIME_DISCOVERED, BlacklistedBtDevice.TABLE_NAME));

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(String.format("DROP TABLE IF EXISTS %s", ValidDeviceDbModel.TABLE_NAME));
        db.execSQL(String.format("DROP TABLE IF EXISTS %s", BlacklistedBtDevice.TABLE_NAME));

        onCreate(db);
    }

}
