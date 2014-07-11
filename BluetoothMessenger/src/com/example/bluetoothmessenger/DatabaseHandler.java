package com.example.bluetoothmessenger;

import java.util.ArrayList;
import java.util.List;
 
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;
 
public class DatabaseHandler extends SQLiteOpenHelper {
 
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
 
    // Database Name
    private static final String DATABASE_NAME = "BodySoundSensorDataManager";
 
    // Contacts table name
    private static final String TABLE_MOBILE_SENSOR = "BodySoundSensorData";
 
    // Contacts Table Columns names
    private static final String KEY_ID = "_id";
    private static final String KEY_DATE_TIME = "date_time";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_ALTITUDE = "altitude";
    private static final String KEY_PROVIDER = "provider";
    private static final String KEY_SPEED = "speed";
    private static final String KEY_BEARING = "bearing";
    private static final String KEY_BODYSOUNDCLASS="bodysoundclass";
    private static final String KEY_ACTIVITY="activity";
 
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
    	Log.i("MainActivity", "onCreate SQLiteDatabase");
        //create mobile sensor table
        String CREATE_MOBILE_SENSOR_TABLE ="CREATE TABLE " + TABLE_MOBILE_SENSOR + " ("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_DATE_TIME + " TEXT,"
                + KEY_LATITUDE + " TEXT," + KEY_LONGITUDE + " TEXT," + KEY_ALTITUDE + " TEXT,"
                + KEY_PROVIDER + " TEXT," + KEY_SPEED + " TEXT,"+ KEY_BEARING + " TEXT," 
                + KEY_BODYSOUNDCLASS + " TEXT," + KEY_ACTIVITY + " TEXT," + ")";
        
        db.execSQL(CREATE_MOBILE_SENSOR_TABLE);
    }
 
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOBILE_SENSOR);
 
        // Create tables again
        onCreate(db);
    }
 
    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */
 
    // Adding new Entry
    long addContact(SensorData sd) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_DATE_TIME, sd.getDateAndTime()); // Contact Date and Time
        values.put(KEY_LATITUDE, sd.getLatitude());
        values.put(KEY_LONGITUDE, sd.getLongitude());
        values.put(KEY_ALTITUDE, sd.getAltitude());
        values.put(KEY_PROVIDER, sd.getProvider());
        values.put(KEY_SPEED, sd.getSpeed());
        values.put(KEY_BEARING, sd.getBearing());
        values.put(KEY_BODYSOUNDCLASS, sd.getBodySoundClass());
        values.put(KEY_ACTIVITY, sd.getActivity());
        
        // Inserting Row
        long newRowId = db.insert(TABLE_MOBILE_SENSOR, null, values);
        db.close(); // Closing database connection
        return newRowId;
    }
 
    // Getting single Entry
    SensorData getContact(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_MOBILE_SENSOR, new String[] { KEY_ID,
                KEY_DATE_TIME, KEY_LATITUDE, KEY_LONGITUDE, KEY_ALTITUDE, KEY_PROVIDER,
                KEY_SPEED, KEY_BEARING, KEY_BODYSOUNDCLASS, KEY_ACTIVITY}, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null){
            cursor.moveToFirst();
        }
 
        SensorData sd = new SensorData(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2),cursor.getString(3), 
                cursor.getString(4), cursor.getString(5), cursor.getString(6), 
                cursor.getString(7), cursor.getString(8), cursor.getString(9));
        // return contact
        return sd;
    }
/* 
    // Getting All Contacts
    public List<SensorData> getAllContacts() {
        List<SensorData> sensorDataList = new ArrayList<SensorData>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_MOBILE_SENSOR;
        
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
            	SensorData sd = new SensorData(Integer.parseInt(cursor.getString(0)),cursor.getString(1), 
            			cursor.getString(2),cursor.getString(3), cursor.getString(4), cursor.getString(5), 
            			cursor.getString(6), cursor.getString(7), cursor.getString(8), cursor.getString(9), 
            			cursor.getString(10), cursor.getString(11), cursor.getString(12));
                // Adding contact to list
                sensorDataList.add(sd);
            } while (cursor.moveToNext());
        }
 
        // return list
        return sensorDataList;
    }
*/
 
    // Updating single contact
    public int updateContact(SensorData sd) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_DATE_TIME, sd.getDateAndTime()); // Contact Date and TIme        
        values.put(KEY_LATITUDE, sd.getLatitude());
        values.put(KEY_LONGITUDE, sd.getLongitude());
        values.put(KEY_ALTITUDE, sd.getAltitude());
        values.put(KEY_PROVIDER, sd.getProvider());
        values.put(KEY_SPEED, sd.getSpeed());
        values.put(KEY_BEARING, sd.getBearing());
        values.put(KEY_BODYSOUNDCLASS, sd.getBodySoundClass());
        values.put(KEY_ACTIVITY, sd.getActivity());
 
        // updating row
        return db.update(TABLE_MOBILE_SENSOR, values, KEY_ID + " = ?",
                new String[] { String.valueOf(sd.getID()) });
    }
 
    // Deleting single contact
    public void deleteContact(SensorData sd) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MOBILE_SENSOR, KEY_ID + " = ?",
                new String[] { String.valueOf(sd.getID()) });
        db.close();
    }
 
    // Getting contacts Count
    public long getContactsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_MOBILE_SENSOR;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        long totalEntry=cursor.getCount();
        cursor.close();
        // return count
        return totalEntry;
    }
 
}