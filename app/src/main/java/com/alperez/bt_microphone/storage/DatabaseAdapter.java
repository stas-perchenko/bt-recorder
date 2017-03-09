package com.alperez.bt_microphone.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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




    /**********************************************************************************************/
    /******************************  Insert/Update methods  ***************************************/
    /**********************************************************************************************/


}
