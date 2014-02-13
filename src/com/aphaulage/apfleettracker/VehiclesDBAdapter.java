package com.aphaulage.apfleettracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class VehiclesDBAdapter {

	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String REG_NUMBER = "reg_number";
	public static final String DESCRIPTION = "description";
	public static final String AVAILABLE = "available";
	public static final String STATUS = "status";
	public static final String MODIFIED = "modified";
	public static final String SYNCED = "synced";

	
	private static final String DATABASE_TABLE = "vehicles";
	
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
	
	public VehiclesDBAdapter(Context context){
		this.context = context;
	}
	
	//Open Users database table. If not created -> Create new table.
	public VehiclesDBAdapter open() throws SQLException {
		this.mDbHelper = new DatabaseHelper(this.context);
		this.mDb = this.mDbHelper.getWritableDatabase();
		return this;
	}
	
	public void close(){
		this.mDbHelper.close();
	}
	
	//Create a new user. Return rowId if create successful. Otherwise return -1.
	public long createVehicle(String id, String name, String reg_number, String description, String available, String status){
		ContentValues initialValues = new ContentValues();
		initialValues.put(ID, id);
		initialValues.put(NAME, name);
		initialValues.put(REG_NUMBER, reg_number);
		initialValues.put(DESCRIPTION, description);
		initialValues.put(AVAILABLE, available);
		initialValues.put(STATUS, status);
		initialValues.put(SYNCED, "No");
		
		Cursor mCursor = this.mDb.rawQuery("SELECT * FROM Vehicles WHERE id= '" + id + "'", null);
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
	public boolean deleteVehicle(String column, String value){
		return this.mDb.delete(DATABASE_TABLE, column + " = '" + value + "'", null) > 0;
	}
	
	//Return cursor of all users
	public Cursor getAllVehicles(){
		return this.mDb.query(DATABASE_TABLE, new String[] {ID, NAME, REG_NUMBER, DESCRIPTION, AVAILABLE, STATUS, MODIFIED, SYNCED},
				null,null,null,null,null);
	}
	
	//Return cursor of selected user
	public Cursor getVehicle(String id) throws SQLException {
		/*Cursor mCursor = this.mDb.query(true, DATABASE_TABLE, new String[] {ID, NAME, ADDRESS1, ADDRESS2, ADDRESS3, TOWN, COUNTY, POSTCODE, LOCATION_OPENING_TIMES_ID, TELEPHONE, CREATED, MODIFIED},
					ID + "=" + id,
					null,null,null,null,null);*/
		Cursor mCursor = this.mDb.rawQuery("SELECT * FROM Vehicles WHERE id = '" + id + "'", null);
		
		if(mCursor != null){
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	//Update user record.
	public boolean updateVehicle(String id, String name, String reg_number, String description, String available, String status, String modified, String synced){
		ContentValues args = new ContentValues();
		args.put(NAME, name);
		args.put(REG_NUMBER, reg_number);
		args.put(DESCRIPTION, description);
		args.put(AVAILABLE, available);
		args.put(STATUS, status);
		args.put(SYNCED, synced);
		args.put(MODIFIED, modified);
		
		return this.mDb.update(DATABASE_TABLE, args, ID + "=" + id, null) > 0;
	}
	
	public boolean updateVehicleRecord(String column, String value, String vehicle_id){
		ContentValues args = new ContentValues();
		args.put(column.toUpperCase(), value);
		this.mDb.update(DATABASE_TABLE, args, ID + "= '" + vehicle_id + "'", null);
		return true;
	}
	

}















