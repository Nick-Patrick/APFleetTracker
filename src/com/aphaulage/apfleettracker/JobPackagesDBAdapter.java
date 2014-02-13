package com.aphaulage.apfleettracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class JobPackagesDBAdapter {

	public static final String ID = "id";
	public static final String JOB_ID = "job_id";
	public static final String PACKAGE_ID = "package_id";
	public static final String NOTES = "notes";
	public static final String STATUS = "statues";
	public static final String MODIFIED = "modified";
	public static final String SYNCED = "synced";

	
	private static final String DATABASE_TABLE = "job_packages";
	
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
	
	public JobPackagesDBAdapter(Context context){
		this.context = context;
	}
	
	//Open Users database table. If not created -> Create new table.
	public JobPackagesDBAdapter open() throws SQLException {
		this.mDbHelper = new DatabaseHelper(this.context);
		this.mDb = this.mDbHelper.getWritableDatabase();
		return this;
	}
	
	public void close(){
		this.mDbHelper.close();
	}
	
	//Create a new user. Return rowId if create successful. Otherwise return -1.
	public long createJobPackage(String id, String job_id, String package_id, String notes, String status, String modified){
		ContentValues initialValues = new ContentValues();
		initialValues.put(ID, id);
		initialValues.put(JOB_ID, job_id);
		initialValues.put(PACKAGE_ID, package_id);
		initialValues.put(NOTES, notes);
		initialValues.put(STATUS, status);
		initialValues.put(MODIFIED, modified);
		initialValues.put(SYNCED, "No");
		
		Cursor mCursor = this.mDb.rawQuery("SELECT * FROM Job_Packages WHERE id= '" + id + "'", null);
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
	public boolean deleteJobPackage(String column, String value){
		return this.mDb.delete(DATABASE_TABLE, column + " = '" + value + "'", null) > 0;
	}
	
	//Return cursor of all users
	public Cursor getAllJobPackages(){
		return this.mDb.query(DATABASE_TABLE, new String[] {ID, JOB_ID, PACKAGE_ID, NOTES, STATUS, MODIFIED, SYNCED},
				null,null,null,null,null);
	}
	
	//Return cursor of selected user
	public Cursor getJobPackage(String id) throws SQLException {
		/*Cursor mCursor = this.mDb.query(true, DATABASE_TABLE, new String[] {ID, NAME, ADDRESS1, ADDRESS2, ADDRESS3, TOWN, COUNTY, POSTCODE, LOCATION_OPENING_TIMES_ID, TELEPHONE, CREATED, MODIFIED},
					ID + "=" + id,
					null,null,null,null,null);*/
		Cursor mCursor = this.mDb.rawQuery("SELECT * FROM Job_Packages WHERE id = '" + id + "'", null);
		
		if(mCursor != null){
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	public Cursor getJobPackagesByJob(String job_id) throws SQLException {
		Cursor mCursor = this.mDb.rawQuery("SELECT * FROM Job_Packages WHERE job_id = '" + job_id + "'", null);
		
		if(mCursor != null){
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	//Update user record.
	public boolean updateJobPackage(String id, String job_id, String package_id, String notes, String status, String modified, String synced){
		ContentValues args = new ContentValues();
		args.put(JOB_ID, job_id);
		args.put(PACKAGE_ID, package_id);
		args.put(NOTES, notes);
		args.put(STATUS, status);
		args.put(SYNCED, synced);
		args.put(MODIFIED, modified);
		
		return this.mDb.update(DATABASE_TABLE, args, ID + "=" + id, null) > 0;
	}
	
	public boolean updateJobPackageRecord(String column, String value, String job_package_id){
		ContentValues args = new ContentValues();
		args.put(column.toUpperCase(), value);
		this.mDb.update(DATABASE_TABLE, args, ID + "= '" + job_package_id + "'", null);
		return true;
	}
	

}















