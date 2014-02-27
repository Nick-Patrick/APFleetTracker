package com.aphaulage.apfleettracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DriverSettingsDBAdapter {

	public static final String _ID = "id";
	public static final String TRACKING_STATUS = "tracking_status";

	
	private static final String DATABASE_TABLE = "driver_settings";
	
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
	
	public DriverSettingsDBAdapter(Context context){
		this.context = context;
	}
	
	//Open database table. If not created -> Create new table.
	public DriverSettingsDBAdapter open() throws SQLException {
		this.mDbHelper = new DatabaseHelper(this.context);
		this.mDb = this.mDbHelper.getWritableDatabase();
		return this;
	}
	
	public void close(){
		this.mDbHelper.close();
	}
	
	//Create a new record. Return rowId if create successful. Otherwise return -1.
	public long createDriverSetting(){
		ContentValues initialValues = new ContentValues();
		//initialValues.put(ID, id);
		initialValues.put(TRACKING_STATUS, "No");
		
		//Cursor mCursor = this.mDb.rawQuery("SELECT * FROM Driver_Settings WHERE id= '" + id + "'", null);
	//	if(mCursor.moveToFirst()){
	//		return this.mDb.update(DATABASE_TABLE, initialValues, ID + "=" + id, null);
	//	}
//		else {
			return this.mDb.insert(DATABASE_TABLE, null, initialValues);
//		}
	}
	
	/** Delete user from rowId. Return true if deleted.
	 * @param table's column to query
	 * @param table's value to query
	 * @return true if row deleted
	 */
	public boolean deleteDriverSetting(String column, String value){
		return this.mDb.delete(DATABASE_TABLE, column + " = '" + value + "'", null) > 0;
	}
	
	//Return cursor of all users
	public Cursor getAllDriverSettings(){
		return this.mDb.query(DATABASE_TABLE, new String[] {TRACKING_STATUS},
				null,null,null,null,null);
	}
	
	//Return cursor of selected user
	public Cursor getDriverSetting() throws SQLException {
		/*Cursor mCursor = this.mDb.query(true, DATABASE_TABLE, new String[] {ID, NAME, ADDRESS1, ADDRESS2, ADDRESS3, TOWN, COUNTY, POSTCODE, LOCATION_OPENING_TIMES_ID, TELEPHONE, CREATED, MODIFIED},
					ID + "=" + id,
					null,null,null,null,null);*/
		Cursor mCursor = this.mDb.rawQuery("SELECT * FROM Driver_Settings", null);
		
		if(mCursor != null){
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	
	
	public boolean updateDriverSettingRecord(String column, String value){
		ContentValues args = new ContentValues();
		args.put(column.toUpperCase(), value);
		this.mDb.update(DATABASE_TABLE, args,null, null);
		return true;
	}
	

}















