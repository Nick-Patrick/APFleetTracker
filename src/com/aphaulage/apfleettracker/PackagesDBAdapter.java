package com.aphaulage.apfleettracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PackagesDBAdapter {

	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String LENGTH = "length";
	public static final String WIDTH = "width";
	public static final String HEIGHT = "height";
	public static final String WEIGHT = "weight";
	public static final String SPECIAL_REQS = "special_reqs";

	
	private static final String DATABASE_TABLE = "packages";
	
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
	
	public PackagesDBAdapter(Context context){
		this.context = context;
	}
	
	//Open Users database table. If not created -> Create new table.
	public PackagesDBAdapter open() throws SQLException {
		this.mDbHelper = new DatabaseHelper(this.context);
		this.mDb = this.mDbHelper.getWritableDatabase();
		return this;
	}
	
	public void close(){
		this.mDbHelper.close();
	}
	
	//Create a new user. Return rowId if create successful. Otherwise return -1.
	public long createPackage(String id, String name, String length, String width, String height, String weight, String special_reqs){
		ContentValues initialValues = new ContentValues();
		initialValues.put(ID, id);
		initialValues.put(NAME, name);
		initialValues.put(LENGTH, length);
		initialValues.put(WIDTH, width);
		initialValues.put(HEIGHT, height);
		initialValues.put(WEIGHT, weight);
		initialValues.put(SPECIAL_REQS, special_reqs);
		
		Cursor mCursor = this.mDb.rawQuery("SELECT * FROM Packages WHERE id= '" + id + "'", null);
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
	public boolean deletePackage(String column, String value){
		return this.mDb.delete(DATABASE_TABLE, column + " = '" + value + "'", null) > 0;
	}
	
	//Return cursor of all users
	public Cursor getAllPackages(){
		return this.mDb.query(DATABASE_TABLE, new String[] {ID, NAME, LENGTH, WIDTH, HEIGHT, WEIGHT, SPECIAL_REQS},
				null,null,null,null,null);
	}
	
	//Return cursor of selected user
	public Cursor getPackage(String id) throws SQLException {
		/*Cursor mCursor = this.mDb.query(true, DATABASE_TABLE, new String[] {ID, NAME, ADDRESS1, ADDRESS2, ADDRESS3, TOWN, COUNTY, POSTCODE, LOCATION_OPENING_TIMES_ID, TELEPHONE, CREATED, MODIFIED},
					ID + "=" + id,
					null,null,null,null,null);*/
		Cursor mCursor = this.mDb.rawQuery("SELECT * FROM packages WHERE id = '" + id + "'", null);
		
		if(mCursor != null){
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	//Update user record.
	public boolean updatePackage(String id, String name, String length, String width, String height, String weight, String special_reqs){
		ContentValues args = new ContentValues();
		args.put(NAME, name);
		args.put(LENGTH, length);
		args.put(WIDTH, width);
		args.put(HEIGHT, height);
		args.put(WEIGHT, weight);
		args.put(SPECIAL_REQS, special_reqs);
				
		return this.mDb.update(DATABASE_TABLE, args, ID + "=" + id, null) > 0;
	}
	
	public boolean updatePackageRecord(String column, String value, String package_id){
		ContentValues args = new ContentValues();
		args.put(column.toUpperCase(), value);
		this.mDb.update(DATABASE_TABLE, args, ID + "= '" + package_id + "'", null);
		return true;
	}
	

}















