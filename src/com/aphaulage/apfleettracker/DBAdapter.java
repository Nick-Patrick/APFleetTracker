package com.aphaulage.apfleettracker;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBAdapter {

	public static final String DATABASE_NAME = "apTrackerDB.db";
	public static final int DATABASE_VERSION = 1;
	
	//Tables to be created on App startup.
	
	private static final String CREATE_TABLE_USERS = 
			"create table users (_id integer primary key autoincrement,"
				+ UsersDBAdapter.FIRST_NAME + " TEXT,"
				+ UsersDBAdapter.LAST_NAME + " TEXT,"
				+ UsersDBAdapter.EMAIL + " TEXT,"
				+ UsersDBAdapter.TELEPHONE + " TEXT,"
				+ UsersDBAdapter.LAST_LOGGED_IN + " TEXT,"
				+ UsersDBAdapter.AVAILABLE + " TEXT,"
				+ UsersDBAdapter.SYNCED + " TEXT"
				+ ");"
			;
	
	private final Context context;
	private DatabaseHelper DBHelper;
	private SQLiteDatabase db;
	
	public DBAdapter(Context ctx){
		this.context = ctx;
		this.DBHelper = new DatabaseHelper(this.context);
	}
	
	//Create database helper class.
	private static class DatabaseHelper extends SQLiteOpenHelper {
		
		DatabaseHelper(Context context){
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db){
			db.execSQL(CREATE_TABLE_USERS);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
			
		}
		
	}
	
	//Open database
	public DBAdapter open() throws SQLException {
		this.db = this.DBHelper.getWritableDatabase();
		return this;
	}
	
	//Close database
	public void close(){
		this.DBHelper.close();
	}
	
}
