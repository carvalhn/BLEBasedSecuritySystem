package com.redbear.chat;

/**
 * Created by User on 05/03/2015.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MyDBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "Security.db";
    public static final String TABLE_PRODUCTS = "BLESensorValues";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DEVICENAME = "devicename";
    public static final String COLUMN_SENSORCODE = "sensorcode";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    public MyDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PRODUCTS_TABLE = "CREATE TABLE " +
                TABLE_PRODUCTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_DEVICENAME
                + " TEXT," + COLUMN_SENSORCODE
                + " TEXT," +COLUMN_TIMESTAMP + " TEXT" + ")";
        db.execSQL(CREATE_PRODUCTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    public void addBLESensorValue(BLESensorValues sensorValues) {

        ContentValues values = new ContentValues();
        values.put(COLUMN_DEVICENAME, sensorValues.get_devicename());
        values.put(COLUMN_SENSORCODE, sensorValues.get_sensorcode());
        values.put(COLUMN_TIMESTAMP, new Date().toString());
        SQLiteDatabase db = this.getWritableDatabase();

        db.insert(TABLE_PRODUCTS, null, values);
        db.close();
    }
    public BLESensorValues findBLESensorValue(String devicename) {
        String query = "Select * FROM " + TABLE_PRODUCTS + " WHERE " + COLUMN_DEVICENAME + " =  \"" + devicename + "\"";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        BLESensorValues sensorValues = new BLESensorValues();

        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            sensorValues.set_id(Integer.parseInt(cursor.getString(0)));
            sensorValues.set_devicename(cursor.getString(1));
            sensorValues.set_sensorcode(cursor.getString(2));
            sensorValues.set_timestamp(new Date(cursor.getString(3)));
            cursor.close();
        } else {
            sensorValues = null;
        }
        db.close();
        return sensorValues;
    }

    public List<BLESensorValues> getAllSensorValues(String devicename) {
        List<BLESensorValues> allSensorValues = new LinkedList<BLESensorValues>();

        // 1. build the query
        String query =  "Select * FROM " + TABLE_PRODUCTS + " WHERE " + COLUMN_DEVICENAME + " =  \"" + devicename + "\"";

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build book and add it to list
        BLESensorValues sensorValues = null;
        if (cursor.moveToFirst()) {
            do {
                sensorValues = new BLESensorValues();
                sensorValues.set_id(Integer.parseInt(cursor.getString(0)));
                sensorValues.set_devicename(cursor.getString(1));
                sensorValues.set_sensorcode(cursor.getString(2));
                sensorValues.set_timestamp(new Date(cursor.getString(3)));
                // Add book to books
                allSensorValues.add(sensorValues);
            } while (cursor.moveToNext());
        }

        //Log.d("getAllBooks()", sensorValues.toString());

        // return books
        return allSensorValues;
    }
}
