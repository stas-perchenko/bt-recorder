package com.alperez.bt_microphone.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by stanislav.perchenko on 3/9/2017.
 */

public class DatabaseManager {
    private AtomicInteger mOpenCounter = new AtomicInteger();

    private static DatabaseManager manager;
    private static DatabaseHelper mDatabaseHelper;
    private SQLiteDatabase mDatabase;

    public static synchronized void initializeInstance(Context context) {
        if (manager == null) {
            manager = new DatabaseManager();
            mDatabaseHelper = DatabaseHelper.getInstance(context);
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (manager == null) {
            throw new IllegalStateException(DatabaseManager.class.getSimpleName() + " is not initialized, call initializeInstance(...) method first.");
        }
        return manager;
    }

    public void reCreateDatabase() {
        SQLiteDatabase db = openDatabase();
        try {
            mDatabaseHelper.onUpgrade(db, db.getVersion(), db.getVersion());
        } finally {
            closeDatabase();
        }
    }

    public synchronized SQLiteDatabase openDatabase() {
        if(mOpenCounter.incrementAndGet() == 1) {
            // Opening new database
            mDatabase = mDatabaseHelper.getWritableDatabase();
        }
        return mDatabase;
    }

    public synchronized void closeDatabase() {
        if(mOpenCounter.decrementAndGet() == 0) {
            // Closing database
            mDatabase.close();

        }
    }
}
