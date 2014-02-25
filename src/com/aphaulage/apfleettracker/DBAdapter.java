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
				+ UsersDBAdapter.SYNCED + " TEXT," 
				+ UsersDBAdapter.DRIVER_ID + " TEXT"
				+ ");"
			;
	
	private static final String CREATE_TABLE_JOBS = 
			"create table jobs (id primary key,"
			+ JobsDBAdapter.NAME + " TEXT,"
			+ JobsDBAdapter.COLLECTION_ID + " TEXT,"
			+ JobsDBAdapter.DROPOFF_ID + " TEXT,"
			+ JobsDBAdapter.STATUS + " TEXT,"
			+ JobsDBAdapter.COMPLETED_DATE + " TEXT,"
			+ JobsDBAdapter.CREATED + " TEXT,"
			+ JobsDBAdapter.MODIFIED + " TEXT,"
			+ JobsDBAdapter.ADDITIONAL_DETAILS + " TEXT,"
			+ JobsDBAdapter.DUE_DATE + " TEXT,"
			+ JobsDBAdapter.VEHICLE_ID + " TEXT,"
			+ JobsDBAdapter.SYNCED + " TEXT,"
			+ JobsDBAdapter.CREATED_BY + " TEXT,"
			+ JobsDBAdapter.STARTED_AT + " TEXT"
			+ ");"
		;

	private static final String CREATE_TABLE_LOCATIONS = 
			"create table locations (id primary key,"
			+ LocationsDBAdapter.NAME + " TEXT,"
			+ LocationsDBAdapter.ADDRESS1 + " TEXT,"
			+ LocationsDBAdapter.ADDRESS2 + " TEXT,"
			+ LocationsDBAdapter.ADDRESS3 + " TEXT,"
			+ LocationsDBAdapter.TOWN + " TEXT,"
			+ LocationsDBAdapter.COUNTY + " TEXT,"
			+ LocationsDBAdapter.POSTCODE + " TEXT,"
			+ LocationsDBAdapter.LOCATION_OPENING_TIMES_ID + " TEXT,"
			+ LocationsDBAdapter.TELEPHONE + " TEXT,"
			+ LocationsDBAdapter.CREATED + " TEXT,"
			+ LocationsDBAdapter.MODIFIED + " TEXT,"
			+ LocationsDBAdapter.LONGITUDE + " TEXT,"
			+ LocationsDBAdapter.LATITUDE + " TEXT"
			+ ");"
		;
	
	private static final String CREATE_TABLE_VEHICLES = 
			"create table vehicles (id primary key,"
			+ VehiclesDBAdapter.NAME + " TEXT,"
			+ VehiclesDBAdapter.REG_NUMBER + " TEXT,"
			+ VehiclesDBAdapter.DESCRIPTION + " TEXT,"
			+ VehiclesDBAdapter.AVAILABLE + " TEXT,"
			+ VehiclesDBAdapter.STATUS + " TEXT,"
			+ VehiclesDBAdapter.SYNCED + " TEXT,"
			+ VehiclesDBAdapter.MODIFIED + " TEXT"
			+ ");"
		;
	
	private static final String CREATE_TABLE_PACKAGES = 
			"create table packages (id primary key," 
			+ PackagesDBAdapter.NAME + " TEXT,"
			+ PackagesDBAdapter.LENGTH + " TEXT,"
			+ PackagesDBAdapter.WIDTH + " TEXT," 
			+ PackagesDBAdapter.HEIGHT + " TEXT,"
			+ PackagesDBAdapter.WEIGHT + " TEXT,"
			+ PackagesDBAdapter.SPECIAL_REQS + " TEXT"
			+ ");"
		;

	private static final String CREATE_TABLE_JOB_PACKAGES = 
			"create table job_packages (id primary key,"
			+ JobPackagesDBAdapter.JOB_ID + " TEXT,"
			+ JobPackagesDBAdapter.PACKAGE_ID + " TEXT,"
			+ JobPackagesDBAdapter.NOTES + " TEXT,"
			+ JobPackagesDBAdapter.STATUS + " TEXT,"
			+ JobPackagesDBAdapter.MODIFIED + " TEXT," 
			+ JobPackagesDBAdapter.SYNCED + " TEXT"
			+ ");"
		;
	
	private static final String CREATE_TABLE_DRIVER_LOCATIONS = 
			"create table driver_locations (_id integer primary key autoincrement,"
			+ DriverLocationsDBAdapter.DRIVER_ID + " TEXT,"
			+ DriverLocationsDBAdapter.DATE_TIME_STAMP + " TEXT,"
			+ DriverLocationsDBAdapter.LATITUDE + " TEXT,"
			+ DriverLocationsDBAdapter.LONGITUDE + " TEXT,"
			+ DriverLocationsDBAdapter.SYNCED + " TEXT"
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
			db.execSQL(CREATE_TABLE_JOBS);
			db.execSQL(CREATE_TABLE_LOCATIONS);
			db.execSQL(CREATE_TABLE_VEHICLES);
			db.execSQL(CREATE_TABLE_PACKAGES);
			db.execSQL(CREATE_TABLE_JOB_PACKAGES);
			db.execSQL(CREATE_TABLE_DRIVER_LOCATIONS);
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
	
	public void clearAllTables(){
		SQLiteDatabase db = this.DBHelper.getWritableDatabase();
		db.delete("users", null, null);
		db.delete("jobs", null, null);
		db.delete("locations", null, null);
		db.delete("vehicles", null, null);
		db.delete("packages", null, null);
		db.delete("job_packages", null, null);
		db.delete("driver_locations", null, null);
	}
	
	//Close database
	public void close(){
		this.DBHelper.close();
	}
	
}
