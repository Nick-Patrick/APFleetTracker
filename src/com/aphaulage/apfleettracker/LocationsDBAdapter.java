package com.aphaulage.apfleettracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LocationsDBAdapter {

	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String ADDRESS1 = "address1";
	public static final String ADDRESS2 = "address2";
	public static final String ADDRESS3 = "address3";
	public static final String TOWN = "town";
	public static final String COUNTY = "county";
	public static final String POSTCODE = "postcode";
	public static final String LOCATION_OPENING_TIMES_ID = "location_opening_times_id";
	public static final String TELEPHONE = "telephone";
	public static final String CREATED = "created";
	public static final String MODIFIED = "modified";
	public static final String LONGITUDE = "longitude";
	public static final String LATITUDE = "latitude";
	
	private static final String DATABASE_TABLE = "locations";
	
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
	
	public LocationsDBAdapter(Context context){
		this.context = context;
	}
	
	//Open Users database table. If not created -> Create new table.
	public LocationsDBAdapter open() throws SQLException {
		this.mDbHelper = new DatabaseHelper(this.context);
		this.mDb = this.mDbHelper.getWritableDatabase();
		return this;
	}
	
	public void close(){
		this.mDbHelper.close();
	}
	
	//Create a new user. Return rowId if create successful. Otherwise return -1.
	public long createLocation(String id, String name, String address1, String address2, String address3, String town, String county, String postcode, String location_opening_times_id, String telephone, String created, String modified, String longitude, String latitude){
		ContentValues initialValues = new ContentValues();
		initialValues.put(ID, id);
		initialValues.put(NAME, name);
		initialValues.put(ADDRESS1, address1);
		initialValues.put(ADDRESS2, address2);
		initialValues.put(ADDRESS3, address3);
		initialValues.put(TOWN, town);
		initialValues.put(COUNTY, county);
		initialValues.put(POSTCODE, postcode);
		initialValues.put(LOCATION_OPENING_TIMES_ID, location_opening_times_id);
		initialValues.put(TELEPHONE, telephone);
		initialValues.put(CREATED, created);
		initialValues.put(MODIFIED, modified);
		initialValues.put(LONGITUDE, longitude);
		initialValues.put(LATITUDE, latitude);
		
		Cursor mCursor = this.mDb.rawQuery("SELECT * FROM Locations WHERE id= '" + id + "'", null);
		if(mCursor.moveToFirst()){
			return this.mDb.update(DATABASE_TABLE, initialValues, ID + "=" + id, null);
		}
		else {
			return this.mDb.insert(DATABASE_TABLE, null, initialValues);
		}
	}
	
	/** Delete user from rowId. Return true if deleted.
	 * @param table's column to query
	 * @param table's value to query
	 * @return true if row deleted
	 */
	public boolean deleteLocation(String column, String value){
		return this.mDb.delete(DATABASE_TABLE, column + " = '" + value + "'", null) > 0;
	}
	
	//Return cursor of all users
	public Cursor getAllLocations(){
		return this.mDb.query(DATABASE_TABLE, new String[] {ID, NAME, ADDRESS1, ADDRESS2, ADDRESS3, TOWN, COUNTY, POSTCODE, LOCATION_OPENING_TIMES_ID, TELEPHONE, CREATED, MODIFIED, LONGITUDE, LATITUDE},
				null,null,null,null,null);
	}
	
	//Return cursor of selected user
	public Cursor getLocation(String id) throws SQLException {
		/*Cursor mCursor = this.mDb.query(true, DATABASE_TABLE, new String[] {ID, NAME, ADDRESS1, ADDRESS2, ADDRESS3, TOWN, COUNTY, POSTCODE, LOCATION_OPENING_TIMES_ID, TELEPHONE, CREATED, MODIFIED},
					ID + "=" + id,
					null,null,null,null,null);*/
		Cursor mCursor = this.mDb.rawQuery("SELECT * FROM Locations WHERE id = '" + id + "'", null);
		
		if(mCursor != null){
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	//Update user record.
	public boolean updateLocation(String id, String name, String address1, String address2, String address3, String town, String county, String postcode, String location_opening_times_id, String telephone, String created, String modified, String longitude, String latitude){
		ContentValues args = new ContentValues();
		args.put(NAME, name);
		args.put(ADDRESS1, address1);
		args.put(ADDRESS2, address2);
		args.put(ADDRESS3, address3);
		args.put(TOWN, town);
		args.put(COUNTY, county);
		args.put(POSTCODE, postcode);
		args.put(LOCATION_OPENING_TIMES_ID, location_opening_times_id);
		args.put(TELEPHONE, telephone);
		args.put(CREATED, created);
		args.put(MODIFIED, modified);
		args.put(LONGITUDE, longitude);
		args.put(LATITUDE, latitude);
		
		return this.mDb.update(DATABASE_TABLE, args, ID + "=" + id, null) > 0;
	}
	
	public boolean updateLocationRecord(String column, String value, String location_id){
		ContentValues args = new ContentValues();
		args.put(column.toUpperCase(), value);
		this.mDb.update(DATABASE_TABLE, args, ID + "= '" + location_id + "'", null);
		return true;
	}
	

}















