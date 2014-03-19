package com.aphaulage.apfleettracker;

import java.sql.Date;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DriverLocationsDBAdapter {

	public static final String _ID = "_id";
	public static final String DRIVER_ID = "driver_id";
	public static final String DATE_TIME_STAMP = "date_time_stamp";
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
	public static final String SYNCED = "synced";	
	
	private static final String DATABASE_TABLE = "driver_locations";
	
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	
	private final Context context;
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		
		DatabaseHelper(Context context){
			super(context, DBAdapter.DATABASE_NAME, null, DBAdapter.DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db){
			
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
			
		}
		
	}
	
	public DriverLocationsDBAdapter(Context context){
		this.context = context;
	}
	
	//Open Users database table. If not created -> Create new table.
	public DriverLocationsDBAdapter open() throws SQLException {
		this.mDbHelper = new DatabaseHelper(this.context);
		this.mDb = this.mDbHelper.getWritableDatabase();
		return this;
	}
	
	public void close(){
		this.mDbHelper.close();
	}
	
	//Create a new user. Return 1 if create successful. Otherwise return -1.
	public long createDriverLocation(String driver_id, String currentTime, Double latitude, Double longitude, String synced){
		try {
			ContentValues initialValues = new ContentValues();
			initialValues.put(DRIVER_ID, driver_id);
			initialValues.put(DATE_TIME_STAMP, currentTime);
			initialValues.put(LATITUDE, latitude);
			initialValues.put(LONGITUDE, longitude);
			initialValues.put(SYNCED, synced);
			this.mDb.insert(DATABASE_TABLE, null, initialValues);
			return 1;
		}
		catch(Exception e) {
			e.printStackTrace();
			return -1;
		}

	}
	
	/** Delete user from rowId. Return true if deleted.
	 * @param table's column to query
	 * @param table's value to query
	 * @return true if row deleted
	 */
	public boolean deleteDriverLocation(String column, String value){
		return this.mDb.delete(DATABASE_TABLE, column + " = '" + value + "'", null) > 0;
	}
	
	//Return cursor of all driver locations.
	public Cursor getAllDriverLocations(){
		return this.mDb.query(DATABASE_TABLE, new String[] {_ID, DRIVER_ID, LATITUDE, LONGITUDE, SYNCED},
				null,null,null,null,null);
	}
	
	
	//Return cursor of selected user
	public Cursor getDriverLocation(String id) throws SQLException {

		Cursor mCursor = this.mDb.rawQuery("SELECT * FROM Driver_Locations WHERE _id = '" + id + "'", null);

		if(mCursor != null){
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	public Cursor getDriverLocationsByParam(String whereParam, String valueParam) throws SQLException {
		
		Cursor mCursor = this.mDb.rawQuery("SELECT * FROM Driver_Locations WHERE " + whereParam + " = '" + valueParam + "'" , null);
		return mCursor;

	}
	
	public Cursor getDriverLocationsNotSynced(){
		return this.mDb.query(DATABASE_TABLE, null,
				"SYNCED = 'No'",null,null,null,null);
	}
	
	public boolean updateDriverLocationRecord(String column, String value, String id){
		ContentValues args = new ContentValues();
		Locale l = new Locale("en_GB");
		args.put(column.toUpperCase(), value);
		this.mDb.update(DATABASE_TABLE, args, _ID + "= " + id + "", null);
		Log.i("updateDriverLocationRecord","updateDriverLocationRecord");
		return true;
	}
	

}















