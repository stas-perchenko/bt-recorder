package com.alperez.bt_microphone.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.alperez.bt_microphone.GlobalConstants;
import com.alperez.bt_microphone.model.BlacklistedBtDevice;
import com.alperez.bt_microphone.model.ValidBtDevice;

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





        db.execSQL(String.format("CREATE TABLE %9$s (%1$s INTEGER PRIMARY KEY, %2$s TEXT, %3$s TEXT, %4$s INTEGER, %5$s INTEGER, %6$s INTEGER, %7$s TEXT, %8$s INTEGER);",
                ValidBtDevice.COLUMN_ID,
                ValidBtDevice.COLUMN_MAC,
                ValidBtDevice.COLUMN_SERIAL_NUM,
                ValidBtDevice.COLUMN_HARD_VERSION,
                ValidBtDevice.COLUMN_SOFT_VERSION,
                ValidBtDevice.COLUMN_RELEASE_DATE,
                ValidBtDevice.COLUMN_LOCAL_NAME,
                ValidBtDevice.COLUMN_TIME_DISCOVERED, ValidBtDevice.TABLE_NAME));

        db.execSQL(String.format("CREATE TABLE %4$s (%1$s INTEGER PRIMARY KEY, %2$s TEXT, %3$s INTEGER,);",
                BlacklistedBtDevice.COLUMN_ID,
                BlacklistedBtDevice.COLUMN_MAC,
                BlacklistedBtDevice.COLUMN_TIME_DISCOVERED, BlacklistedBtDevice.TABLE_NAME));

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(String.format("DROP TABLE IF EXISTS %s", ValidBtDevice.TABLE_NAME));
        db.execSQL(String.format("DROP TABLE IF EXISTS %s", BlacklistedBtDevice.TABLE_NAME));

        onCreate(db);
    }

}
