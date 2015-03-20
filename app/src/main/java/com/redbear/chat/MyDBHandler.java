package com.redbear.chat;

/**
 * Created by User on 05/03/2015.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MyDBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Security.db";
    public static final String TABLE_PRODUCTS = "BLESensorValues";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_DEVICENAME = "devicename";
    public static final String COLUMN_SENSORCODE = "sensorcode";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    public MyDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_Sensor_TABLE = "CREATE TABLE " +
                TABLE_PRODUCTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_DEVICENAME
                + " TEXT," + COLUMN_SENSORCODE
                + " TEXT," +COLUMN_TIMESTAMP + " datetime DEFAULT (datetime('now')) " + ")";
        db.execSQL(CREATE_Sensor_TABLE);
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
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date=new Date();
        values.put(COLUMN_TIMESTAMP, dateFormat.format(date));
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

    public boolean isExists(String devicename) {
        String query = "Select max(timestamp) FROM " + TABLE_PRODUCTS + " WHERE " + COLUMN_DEVICENAME + " =  \"" + devicename + "\"";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);
        boolean doesExist=false;
        if (cursor!=null&&cursor.moveToFirst()) {

            SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date nowDate=new Date();
            try {
                Log.e("Chat", "here" + "A");
                if(cursor.getString(0)!=null) {
                    Date databaseDate = dateFormat.parse(cursor.getString(0));
                    Log.e("Chat", "here" + "B");
                    long millis = (nowDate.getTime() - databaseDate.getTime());
                    Log.e("Chat", "here" + "C");
                    if (((int) ((millis / (1000 * 60)) % 60)) <= 5) {
                        Log.e("Chat", "here" + databaseDate.toString());
                        doesExist = true;
                    }
                }
            }
            catch(Exception e)
            {
                Log.e("Chat","caught exception timestamp233",e);
            }

        }
        cursor.close();
        db.close();
        return doesExist;
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
                SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date=new Date();
                try {
                    sensorValues.set_timestamp(dateFormat.parse(cursor.getString(3)));
                }
                catch(Exception e)
                {
                   Log.e("Chat","caught exception timestamp"+e);
                }
                // Add book to books
                allSensorValues.add(sensorValues);
            } while (cursor.moveToNext());
        }

        //Log.d("getAllBooks()", sensorValues.toString());

        // return books
        return allSensorValues;
    }
}
