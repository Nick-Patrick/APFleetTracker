package com.aphaulage.apfleettracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class UsersDBAdapter {

	public static final String ROW_ID = "_id";
	public static final String FIRST_NAME = "first_name";
	public static final String LAST_NAME = "last_name";
	public static final String EMAIL = "email";
	public static final String TELEPHONE = "telephone";
	public static final String LAST_LOGGED_IN = "last_logged_in";
	public static final String AVAILABLE = "available";
	public static final String SYNCED = "synced";
	public static final String DRIVER_ID = "driver_id";
	
	private static final String DATABASE_TABLE = "users";
	
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
	
	public UsersDBAdapter(Context context){
		this.context = context;
	}
	
	//Open Users database table. If not created -> Create new table.
	public UsersDBAdapter open() throws SQLException {
		this.mDbHelper = new DatabaseHelper(this.context);
		this.mDb = this.mDbHelper.getWritableDatabase();
		return this;
	}
	
	public void close(){
		this.mDbHelper.close();
	}
	
	//Create a new user. Return rowId if create successful. Otherwise return -1.
	public long createUser(String first_name, String last_name, String email, String telephone, String last_logged_in, String available, String synced, String driver_id){
		ContentValues initialValues = new ContentValues();
		initialValues.put(FIRST_NAME, first_name);
		initialValues.put(LAST_NAME, last_name);
		initialValues.put(EMAIL, email);
		initialValues.put(TELEPHONE, telephone);
		initialValues.put(LAST_LOGGED_IN, last_logged_in);
		initialValues.put(AVAILABLE, available);
		initialValues.put(SYNCED, "No");
		initialValues.put(DRIVER_ID, driver_id);
		
		return this.mDb.insert(DATABASE_TABLE, null, initialValues);
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
	public Cursor getAllUsers(){
		return this.mDb.query(DATABASE_TABLE, new String[] {ROW_ID, FIRST_NAME, LAST_NAME, EMAIL, TELEPHONE, LAST_LOGGED_IN, AVAILABLE, SYNCED, DRIVER_ID},
				null,null,null,null,null);
	}
	
	//Return cursor of selected user
	public Cursor getUser(long rowId) throws SQLException {
		Cursor mCursor = this.mDb.query(true, DATABASE_TABLE, new String[] {ROW_ID, FIRST_NAME, LAST_NAME, EMAIL, TELEPHONE, LAST_LOGGED_IN, AVAILABLE, SYNCED, DRIVER_ID},
					ROW_ID + "=" + rowId,
					null,null,null,null,null);
		
		if(mCursor != null){
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	//Update user record.
	public boolean updateUser(long rowId, String first_name, String last_name, String email, String telephone, String last_logged_in, String available, String synced, String driver_id){
		ContentValues args = new ContentValues();
		args.put(FIRST_NAME, first_name);
		args.put(LAST_NAME, last_name);
		args.put(EMAIL, email);
		args.put(TELEPHONE, telephone);
		args.put(LAST_LOGGED_IN, last_logged_in);
		args.put(AVAILABLE, available);
		args.put(SYNCED, synced);
		args.put(DRIVER_ID, driver_id);
		
		return this.mDb.update(DATABASE_TABLE, args, ROW_ID + "=" + rowId, null) > 0;
	}
	
	public boolean updateUserRecord(String column, String value, String driver_id){
		ContentValues args = new ContentValues();
		args.put(column.toUpperCase(), value);
		this.mDb.update(DATABASE_TABLE, args, DRIVER_ID + "= '" + driver_id + "'", null);
		return true;
	}
	

}















