package com.aphaulage.apfleettracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class JobsDBAdapter {

	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String COLLECTION_ID = "collection_id";
	public static final String DROPOFF_ID = "dropoff_id";
	public static final String STATUS = "status";
	public static final String COMPLETED_DATE = "completed_date";
	public static final String CREATED = "created";
	public static final String MODIFIED = "modified";
	public static final String ADDITIONAL_DETAILS = "additional_details";
	public static final String DUE_DATE = "due_date";
	public static final String VEHICLE_ID = "vehicle_id";
	public static final String SYNCED = "synced";
	public static final String CREATED_BY = "created_by";
	public static final String STARTED_AT = "started_at";
	
	
	private static final String DATABASE_TABLE = "jobs";
	
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
	
	public JobsDBAdapter(Context context){
		this.context = context;
	}
	
	//Open Users database table. If not created -> Create new table.
	public JobsDBAdapter open() throws SQLException {
		this.mDbHelper = new DatabaseHelper(this.context);
		this.mDb = this.mDbHelper.getWritableDatabase();
		return this;
	}
	
	public void close(){
		this.mDbHelper.close();
	}
	
	//Create a new user. Return rowId if create successful. Otherwise return -1.
	public long createJob(String id, String name, String collection_id, String dropoff_id, String status, String completed_date, String created, String modified, String additional_details, String due_date, String vehicle_id, String created_by, String started_at){
		ContentValues initialValues = new ContentValues();
		initialValues.put(ID, id);
		initialValues.put(NAME, name);
		initialValues.put(COLLECTION_ID, collection_id);
		initialValues.put(DROPOFF_ID, dropoff_id);
		initialValues.put(STATUS, status);
		initialValues.put(COMPLETED_DATE, completed_date);
		initialValues.put(CREATED, created);
		initialValues.put(MODIFIED, modified);
		initialValues.put(ADDITIONAL_DETAILS, additional_details);
		initialValues.put(DUE_DATE, due_date);
		initialValues.put(VEHICLE_ID, vehicle_id);
		initialValues.put(SYNCED, "No");
		initialValues.put(CREATED_BY, created_by);
		initialValues.put(STARTED_AT, started_at);
		
		/*Cursor mCursor = this.mDb.query(true, DATABASE_TABLE, new String[] {ID},
				ID.toString() + "=" + id,
				null,null,null,null,null);*/
		Cursor mCursor = this.mDb.rawQuery("SELECT * FROM Jobs WHERE id= '" + id + "'", null);
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
	public boolean deleteUser(String column, String value){
		return this.mDb.delete(DATABASE_TABLE, column + " = '" + value + "'", null) > 0;
	}
	
	//Return cursor of all users
	public Cursor getAllJobs(){
		return this.mDb.query(DATABASE_TABLE, new String[] {ID, NAME, COLLECTION_ID, DROPOFF_ID, STATUS, COMPLETED_DATE, CREATED, MODIFIED, ADDITIONAL_DETAILS, DUE_DATE, VEHICLE_ID, SYNCED, CREATED_BY, STARTED_AT},
				null,null,null,null,null);
	}
	
	//Return cursor of all users
	public Cursor getAllAssignedJobs(){
		return this.mDb.rawQuery("SELECT * FROM Jobs WHERE status = 'Assigned'", null);
	}
	
	//Return cursor of selected user
	public Cursor getJob(String job_id) throws SQLException {

		Cursor mCursor = this.mDb.rawQuery("SELECT * FROM Jobs WHERE id = '" + job_id + "'", null);

		if(mCursor != null){
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	public Cursor getJobByParam(String whereParam, String valueParam) throws SQLException {
		
		Cursor mCursor = this.mDb.rawQuery("SELECT * FROM Jobs WHERE " + whereParam + " = '" + valueParam + "'" , null);
		
		if(mCursor != null){
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	//Update user record.
	public boolean updateJob(String id, String name, String collection_id, String dropoff_id, String status, String completed_date, String created, String modified, String additional_details, String due_date, String vehicle_id, String synced){
		ContentValues args = new ContentValues();
		args.put(NAME, name);
		args.put(COLLECTION_ID, collection_id);
		args.put(DROPOFF_ID, dropoff_id);
		args.put(STATUS, status);
		args.put(COMPLETED_DATE, completed_date);
		args.put(CREATED, created);
		args.put(MODIFIED, modified);
		args.put(ADDITIONAL_DETAILS, additional_details);
		args.put(DUE_DATE, due_date);
		args.put(VEHICLE_ID, vehicle_id);
		args.put(SYNCED, synced);
	
		
		return this.mDb.update(DATABASE_TABLE, args, ID + "=" + id, null) > 0;
	}
	
	public boolean updateJobRecord(String column, String value, String job_id){
		ContentValues args = new ContentValues();
		args.put(column.toUpperCase(), value);
		this.mDb.update(DATABASE_TABLE, args, ID + "= '" + job_id + "'", null);
		return true;
	}
	

}















