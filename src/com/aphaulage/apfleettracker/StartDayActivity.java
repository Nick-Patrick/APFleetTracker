package com.aphaulage.apfleettracker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class StartDayActivity extends Activity {
	DBAdapter dbAdapter;
	UsersDBAdapter userDB;
	JobsDBAdapter jobsDB;
	LocationsDBAdapter locationsDB;
	VehiclesDBAdapter vehiclesDB;
	PackagesDBAdapter packagesDB;
	JobPackagesDBAdapter jobPackagesDB;
	DriverLocationsDBAdapter driverLocationsDB;
	
	Cursor usersCursor;
	Cursor jobsCursor;
	JsonParser jsonParser = new JsonParser();
	private static final String driversUpdateUrl = "http://aphaulage.co.uk/apTracker/drivers/update";
	private static final String getDriverJobsUrl = "http://aphaulage.co.uk/apTracker/jobs/assignedJobsByDriverId.json";
	private static final String getLocationsUrl = "http://aphaulage.co.uk/apTracker/locations/getAllLocations.json";
	private static final String getVehiclesUrl = "http://aphaulage.co.uk/apTracker/vehicles/getAllVehicles.json";
	private static final String getPackagesUrl = "http://aphaulage.co.uk/apTracker/packages/getAllPackages.json";
	private static final String updateJobUrl = "http://aphaulage.co.uk/apTracker/jobs/updateActiveJob/";
	private static final String updateDriverUrl = "http://aphaulage.co.uk/apTracker/drivers/update/";
	private static final String addDriverLocationUrl = "http://aphaulage.co.uk/apTracker/driverLocations/addDriverLocation.json";
	
	TextView currentDateTimeTextView;
	TextView userNameTextView;
	TextView userActiveJobTextView;
	TextView pendingJobsCountTextView;
	TextView completedJobsCountTextView;

	
	String mDriverId;
	String mDriverEmail;
	String mAvailable;
	
	Intent intent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_day);
		dbAdapter = new DBAdapter(getApplicationContext());

		getActionBar().setTitle("Dashboard");
		userDB = new UsersDBAdapter(getApplicationContext());
		jobsDB = new JobsDBAdapter(getApplicationContext());
		locationsDB = new LocationsDBAdapter(getApplicationContext());
		vehiclesDB = new VehiclesDBAdapter(getApplicationContext());
		packagesDB = new PackagesDBAdapter(getApplicationContext());
		jobPackagesDB = new JobPackagesDBAdapter(getApplicationContext());
		driverLocationsDB = new DriverLocationsDBAdapter(getApplicationContext());
		
		String now = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
		currentDateTimeTextView = (TextView)findViewById(R.id.start_day_current_date_time);
		currentDateTimeTextView.setText(now);
		
		userDB.open();
		usersCursor = userDB.getAllUsers();
		usersCursor.moveToNext();
		mDriverEmail = usersCursor.getString(3);
		mDriverId = usersCursor.getString(8);
		//userNameTextView = (TextView)findViewById(R.id.user_name);
		//userNameTextView.setText(usersCursor.getString(1) + " " + usersCursor.getString(2));
		

		
		userDB.close();
		
		jobsDB.open();
		jobsCursor = jobsDB.getJobByParam("STATUS", "Active");
		userActiveJobTextView = (TextView)findViewById(R.id.user_active_job);
		LinearLayout activeJobArea = (LinearLayout)findViewById(R.id.user_active_job_area);
		if(jobsCursor.getCount() != 0){
			userActiveJobTextView.setText(jobsCursor.getString(1));
		
			activeJobArea.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v){
					jobsDB.open();
					Cursor activeJobsCursor;
					activeJobsCursor = jobsDB.getJobByParam("STATUS", "Active");
					jobsDB.close();
					Intent startJobIntent = new Intent(v.getContext(), ActiveJobActivity.class);
					startJobIntent.putExtra("job_id", activeJobsCursor.getString(0));
					startJobIntent.putExtra("driver_id", mDriverId);
					startActivity(startJobIntent);
				}
			});
		}
		else {
			activeJobArea.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v){
					Toast.makeText(getApplicationContext(), "No job currently active", Toast.LENGTH_SHORT).show();
				}
			});
		}
		
		jobsDB.open();
		Cursor jobsAssignedCursor;
		jobsAssignedCursor = jobsDB.getJobByParam("STATUS", "Assigned");
		pendingJobsCountTextView = (TextView)findViewById(R.id.user_pending_jobs);
		LinearLayout pendingJobsArea = (LinearLayout)findViewById(R.id.user_pending_jobs_area);

		if(jobsAssignedCursor.getCount() != 0){
			pendingJobsCountTextView.setText(Integer.toString(jobsAssignedCursor.getCount()));
			
			pendingJobsArea.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					
					intent = new Intent(StartDayActivity.this, PendingJobsActivity.class);

					new UpdateAvailability().execute();
				}
				
			});
		}
		else {
			pendingJobsArea.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					
					Toast.makeText(getApplicationContext(), "No Pending Jobs", Toast.LENGTH_SHORT).show();
				}
				
			});
		}
		
		Cursor jobsCompletedCursor;
		jobsCompletedCursor = jobsDB.getJobByParam("STATUS", "Complete");
		completedJobsCountTextView = (TextView)findViewById(R.id.user_completed_jobs);
		LinearLayout completedJobsArea = (LinearLayout)findViewById(R.id.user_completed_jobs_area);
		if(jobsCompletedCursor.getCount() != 0){
			completedJobsCountTextView.setText(Integer.toString(jobsCompletedCursor.getCount()));
			completedJobsArea.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					Toast.makeText(getApplicationContext(), "Completed Jobs", Toast.LENGTH_SHORT).show();
				}
				
			});
		}
		else {
			completedJobsArea.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					Toast.makeText(getApplicationContext(), "Completed Jobs", Toast.LENGTH_SHORT).show();
				}
				
			});
		}
		jobsDB.close();

		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.dashboard, menu);
	    return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case R.id.menu_item_refresh:
			finish();
			Intent initIntent = new Intent(this.getApplicationContext(), InitDataActivity.class);
			startActivity(initIntent);
			break;
		case R.id.menu_item_sync:
			if(isNetworkAvailable()==true){
			new SyncData().execute();
			}
			else {
				Toast.makeText(getApplicationContext(), "No internet connection available", Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.menu_sign_out:
				try {
				dbAdapter.clearAllTables();
			}
			catch (Exception e){
				e.printStackTrace();
			}
			finish();
			Intent intent = new Intent(this.getApplicationContext(), LoginActivity.class);
			startActivity(intent);
			break;
		default:
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	
	
	
	
	class UpdateAvailability extends AsyncTask<String, String, String>{

	
		private ProgressDialog pDialog;

		@Override
	        protected void onPreExecute() {
	            super.onPreExecute();
	            pDialog = ProgressDialog.show(StartDayActivity.this, "Finding Jobs", "Searching..");
	            pDialog.show();
	        }
		
		@Override
		protected String doInBackground(String... args) {


			//	List<NameValuePair> params = new ArrayList<NameValuePair>();
			//	params.add(new BasicNameValuePair("email", mDriverEmail));
			//	params.add(new BasicNameValuePair("available", mAvailable));
			//	params.add(new BasicNameValuePair("key", "9c36c7108a73324100bc9305f581979071d45ee9"));
				Log.i("doInBackground", "Starting");
			//	Log.i("params: ", params.toString());
				try {
					//JSONObject json = jsonParser.makeHttpRequest(driversUpdateUrl + "/" + mDriverEmail + ".json", "POST", params);
					List<NameValuePair> jobParams = new ArrayList<NameValuePair>();
					jobParams.add(new BasicNameValuePair("driver_id", mDriverId));
					jobParams.add(new BasicNameValuePair("key", "9c36c7108a73324100bc9305f581979071d45ee9"));
					JSONObject jsonJobs = jsonParser.makeHttpRequest(getDriverJobsUrl, "POST", jobParams);
					Log.i("beforeJsonJobs", "hello");
					Log.i("jsonJobs", jsonJobs.toString());
					List<NameValuePair> locationParams = new ArrayList<NameValuePair>();
					locationParams.add(new BasicNameValuePair("key", "9c36c7108a73324100bc9305f581979071d45ee9"));
					JSONObject jsonLocations = jsonParser.makeHttpRequest(getLocationsUrl, "POST", locationParams);
					//Log.i("1: ", jsonJobs.getString("name"));
					//Log.i("2: ", jsonJobs.getString("Job.name"));
					//Log.i("count: ", "s: " + jsonJobs.getJSONArray("message").length());
					//jsonJobs.getJSONArray("message").getJSONObject(0).getJSONObject("Job"));
					try {
						//if(json.getString("message").equals("Driver Updated")){
						//	Log.i("Well done", "Well done");
						//	userDB.open();
						//	userDB.updateUserRecord("synced", "Yes", mDriverId);
						//	userDB.close();
							jobsDB.open();
							jobPackagesDB.open();
							Log.i("DB Open", "Yes");
							for(int i=0; i<jsonJobs.getJSONArray("message").length(); i++){
								Log.i("for", "for started");
								Log.i("1: ", jsonJobs.getJSONArray("message").getJSONObject(i).getJSONObject("Job").toString());
								//Log.i("1: ", jsonJobs.getJSONArray("message").getJSONObject(i).getJSONArray("DriverVehicleJob").toString());
								//Log.i("1: ", jsonJobs.getJSONArray("message").getJSONObject(i).getJSONObject("DriverVehicleJob").getString("driver_id"));
								//Log.i("2: ", jsonJobs.getJSONArray("message").getJSONObject(i).getJSONArray("DriverVehicleJob").getJSONObject(0).getString("vehicle_id"));
								Cursor existingJob = jobsDB.getJob(jsonJobs.getJSONArray("message").getJSONObject(i).getJSONObject("Job").getString("id"));
								if(existingJob.getCount() == 0){
									jobsDB.createJob(
											jsonJobs.getJSONArray("message").getJSONObject(i).getJSONObject("Job").getString("id"),
											jsonJobs.getJSONArray("message").getJSONObject(i).getJSONObject("Job").getString("name"), 
											jsonJobs.getJSONArray("message").getJSONObject(i).getJSONObject("Job").getString("collection_id"), 
											jsonJobs.getJSONArray("message").getJSONObject(i).getJSONObject("Job").getString("dropoff_id"), 
											jsonJobs.getJSONArray("message").getJSONObject(i).getJSONObject("Job").getString("status"), 
											jsonJobs.getJSONArray("message").getJSONObject(i).getJSONObject("Job").getString("completed_date"), 
											jsonJobs.getJSONArray("message").getJSONObject(i).getJSONObject("Job").getString("created"), 
											jsonJobs.getJSONArray("message").getJSONObject(i).getJSONObject("Job").getString("modified"), 
											jsonJobs.getJSONArray("message").getJSONObject(i).getJSONObject("Job").getString("additional_details"), 
											jsonJobs.getJSONArray("message").getJSONObject(i).getJSONObject("Job").getString("due_date"), 
											jsonJobs.getJSONArray("message").getJSONObject(i).getJSONArray("DriverVehicleJob").getJSONObject(0).getString("vehicle_id"),
											//jsonJobs.getJSONArray("message").getJSONObject(i).getJSONObject("DriverVehicleJob").getString("vehicle_id"),
											jsonJobs.getJSONArray("message").getJSONObject(i).getJSONObject("Job").getString("created_by"),
											""
											);
									
									for(int p=0;p<jsonJobs.getJSONArray("message").getJSONObject(i).getJSONArray("JobPackage").length();p++){
										jobPackagesDB.createJobPackage(
											jsonJobs.getJSONArray("message").getJSONObject(i).getJSONArray("JobPackage").getJSONObject(p).getString("id"),
											jsonJobs.getJSONArray("message").getJSONObject(i).getJSONArray("JobPackage").getJSONObject(p).getString("job_id"),
											jsonJobs.getJSONArray("message").getJSONObject(i).getJSONArray("JobPackage").getJSONObject(p).getString("package_id"),
											jsonJobs.getJSONArray("message").getJSONObject(i).getJSONArray("JobPackage").getJSONObject(p).getString("notes"),
											jsonJobs.getJSONArray("message").getJSONObject(i).getJSONArray("JobPackage").getJSONObject(p).getString("status"),
											jsonJobs.getJSONArray("message").getJSONObject(i).getJSONArray("JobPackage").getJSONObject(p).getString("modified")
											);
									}
								}
								
								

							}
							jobPackagesDB.close();
							jobsDB.close();
							try {
								Log.i("Start", "Fetching locations");
								locationsDB.open();
								for(int i=0; i<jsonLocations.getJSONArray("message").length(); i++){
									Log.i("Location: ", jsonLocations.getJSONArray("message").getJSONObject(i).getJSONObject("Location").toString());
									//Log.i("1: ", jsonLocations.getJSONArray("message").getJSONObject(1).getString("name"));
									locationsDB.createLocation(
											jsonLocations.getJSONArray("message").getJSONObject(i).getJSONObject("Location").getString("id"), 
											jsonLocations.getJSONArray("message").getJSONObject(i).getJSONObject("Location").getString("name"), 
											jsonLocations.getJSONArray("message").getJSONObject(i).getJSONObject("Location").getString("address1"), 
											jsonLocations.getJSONArray("message").getJSONObject(i).getJSONObject("Location").getString("address2"), 
											jsonLocations.getJSONArray("message").getJSONObject(i).getJSONObject("Location").getString("address3"), 
											jsonLocations.getJSONArray("message").getJSONObject(i).getJSONObject("Location").getString("town"), 
											jsonLocations.getJSONArray("message").getJSONObject(i).getJSONObject("Location").getString("county"), 
											jsonLocations.getJSONArray("message").getJSONObject(i).getJSONObject("Location").getString("postcode"), 
											jsonLocations.getJSONArray("message").getJSONObject(i).getJSONObject("Location").getString("location_opening_times_id"), 
											jsonLocations.getJSONArray("message").getJSONObject(i).getJSONObject("Location").getString("telephone"), 
											jsonLocations.getJSONArray("message").getJSONObject(i).getJSONObject("Location").getString("created"), 
											jsonLocations.getJSONArray("message").getJSONObject(i).getJSONObject("Location").getString("modified"),
											jsonLocations.getJSONArray("message").getJSONObject(i).getJSONObject("Location").getString("longitude"),
											jsonLocations.getJSONArray("message").getJSONObject(i).getJSONObject("Location").getString("latitude")
										);
									
								}
								locationsDB.close();
							}
							catch (Exception e) {
								e.printStackTrace();
							}
							try {
								Log.i("Vehicle Start", "Fetching Vehicles");
								List<NameValuePair> vehiclesParams = new ArrayList<NameValuePair>();
								vehiclesParams.add(new BasicNameValuePair("key", "9c36c7108a73324100bc9305f581979071d45ee9"));
								JSONObject jsonVehicles = jsonParser.makeHttpRequest(getVehiclesUrl, "POST", vehiclesParams);
								vehiclesDB.open();
								for(int i=0; i<jsonVehicles.getJSONArray("message").length(); i++){
									Log.i("Vehicle:", jsonVehicles.getJSONArray("message").getJSONObject(i).getJSONObject("Vehicle").toString());
									vehiclesDB.createVehicle(
											jsonVehicles.getJSONArray("message").getJSONObject(i).getJSONObject("Vehicle").getString("id"), 
											jsonVehicles.getJSONArray("message").getJSONObject(i).getJSONObject("Vehicle").getString("name"), 
											jsonVehicles.getJSONArray("message").getJSONObject(i).getJSONObject("Vehicle").getString("reg_number"), 
											jsonVehicles.getJSONArray("message").getJSONObject(i).getJSONObject("Vehicle").getString("description"), 
											jsonVehicles.getJSONArray("message").getJSONObject(i).getJSONObject("Vehicle").getString("available"), 
											jsonVehicles.getJSONArray("message").getJSONObject(i).getJSONObject("Vehicle").getString("status")
										);
								}
								vehiclesDB.close();
							}
							catch (Exception e){
								e.printStackTrace();
							}
							try {
								Log.i("Packages start", "Fetching packages");
								List<NameValuePair> packagesParams = new ArrayList<NameValuePair>();
								packagesParams.add(new BasicNameValuePair("key", "9c36c7108a73324100bc9305f581979071d45ee9"));
								JSONObject jsonPackages = jsonParser.makeHttpRequest(getPackagesUrl, "POST", packagesParams);
								Log.i("packages", jsonPackages.toString());
								packagesDB.open();
								for(int i=0; i<jsonPackages.getJSONArray("message").length();i++){
									Log.i("Package:", jsonPackages.getJSONArray("message").getJSONObject(i).getJSONObject("Package").toString());
									packagesDB.createPackage(
											jsonPackages.getJSONArray("message").getJSONObject(i).getJSONObject("Package").getString("id"), 
											jsonPackages.getJSONArray("message").getJSONObject(i).getJSONObject("Package").getString("name"), 
											jsonPackages.getJSONArray("message").getJSONObject(i).getJSONObject("Package").getString("length"), 
											jsonPackages.getJSONArray("message").getJSONObject(i).getJSONObject("Package").getString("width"), 
											jsonPackages.getJSONArray("message").getJSONObject(i).getJSONObject("Package").getString("height"), 
											jsonPackages.getJSONArray("message").getJSONObject(i).getJSONObject("Package").getString("weight"), 
											jsonPackages.getJSONArray("message").getJSONObject(i).getJSONObject("Package").getString("special_reqs")
										);
								}
								packagesDB.close();
							}
							catch (Exception e){
								e.printStackTrace();
							}
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				startActivity(intent);
				return null;
			}
		
		@Override
		protected void onPostExecute(String unused){
			super.onPostExecute(unused);
			pDialog.dismiss();
		}
		
	}
	
	class SyncData extends AsyncTask<String, String, String>{

		
		private ProgressDialog pDialogSync = ProgressDialog.show(StartDayActivity.this, "Please wait", "Syncing..");


		@Override
	        protected void onPreExecute() {
	            super.onPreExecute();
	            pDialogSync.show();
	        }
		
		@Override
		protected String doInBackground(String... args) {
			try{
				userDB.open();
				jobsDB.open();
				driverLocationsDB.open();
				Cursor unsyncedUserRecords = userDB.getUsersNotSynced();
				Cursor unsyncedJobRecords = jobsDB.getJobsNotSynced();
				Cursor unsyncedDriverLocationRecords = driverLocationsDB.getDriverLocationsNotSynced();
				
				
				for(int i=0;i<unsyncedUserRecords.getCount(); i++){
					unsyncedUserRecords.moveToPosition(i);
					List<NameValuePair> driverParams = new ArrayList<NameValuePair>();
					driverParams.add(new BasicNameValuePair("id", unsyncedUserRecords.getString(8)));
					driverParams.add(new BasicNameValuePair("key", "9c36c7108a73324100bc9305f581979071d45ee9"));
					driverParams.add(new BasicNameValuePair("available", unsyncedUserRecords.getString(6)));
					driverParams.add(new BasicNameValuePair("modified", unsyncedUserRecords.getString(5)));
					Log.i("AvailableDriver - Params", driverParams.toString());
					JSONObject jsonDriversResult = jsonParser.makeHttpRequest(updateDriverUrl + unsyncedUserRecords.getString(8) + ".json", "POST", driverParams);
					Log.i("AvailableDriver - JsonDriversResult", jsonDriversResult.toString());
					userDB.updateUserRecord("SYNCED", "Yes", unsyncedUserRecords.getString(8));
				}
				
				for(int i=0;i<unsyncedJobRecords.getCount(); i++){
					unsyncedJobRecords.moveToPosition(i);
					List<NameValuePair> jobParams = new ArrayList<NameValuePair>();
					jobParams.add(new BasicNameValuePair("job_id", unsyncedJobRecords.getString(0)));
					jobParams.add(new BasicNameValuePair("key", "9c36c7108a73324100bc9305f581979071d45ee9"));
					jobParams.add(new BasicNameValuePair("status", unsyncedJobRecords.getString(4)));
					jobParams.add(new BasicNameValuePair("completed_date", unsyncedJobRecords.getString(5)));
					Log.i("FinishedJob - Params", jobParams.toString());
					JSONObject jsonJobsResult = jsonParser.makeHttpRequest(updateJobUrl + unsyncedJobRecords.getString(0) + ".json", "POST", jobParams);
					Log.i("UpdateJobFinished - JsonJobsResult", jsonJobsResult.toString());
					jobsDB.updateJobRecord("SYNCED", "Yes", unsyncedJobRecords.getString(0));
				}
				
				for(int i=0;i<unsyncedDriverLocationRecords.getCount(); i++){
					unsyncedDriverLocationRecords.moveToPosition(i);
					List<NameValuePair> locationParams = new ArrayList<NameValuePair>();
					locationParams.add(new BasicNameValuePair("key", "9c36c7108a73324100bc9305f581979071d45ee9"));
					locationParams.add(new BasicNameValuePair("driver_id", unsyncedDriverLocationRecords.getString(1)));
					locationParams.add(new BasicNameValuePair("latitude", unsyncedDriverLocationRecords.getString(3)));
					locationParams.add(new BasicNameValuePair("longitude", unsyncedDriverLocationRecords.getString(4)));
					
					JSONObject jsonDriverLocationResult = jsonParser.makeHttpRequest(addDriverLocationUrl, "POST", locationParams);
					driverLocationsDB.updateDriverLocationRecord("SYNCED", "Yes", unsyncedDriverLocationRecords.getString(0));
					Log.i("jsonDriverLocaitonResult", jsonDriverLocationResult.toString());
				}
				driverLocationsDB.close();
				jobsDB.close();
				userDB.close();
			}
			catch (Exception e){
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String unused){
			super.onPostExecute(unused);
			pDialogSync.setOwnerActivity(StartDayActivity.this);
			pDialogSync.dismiss();
			

			userDB.open();
			jobsDB.open();
			driverLocationsDB.open();
			Cursor unsyncedUserRecords = userDB.getUsersNotSynced();
			Cursor unsyncedJobRecords = jobsDB.getJobsNotSynced();
			Cursor unsyncedDriverLocationRecords = driverLocationsDB.getDriverLocationsNotSynced();
			
			int countUnsynced = 0 + unsyncedUserRecords.getCount() + unsyncedJobRecords.getCount() + unsyncedDriverLocationRecords.getCount();
			if(countUnsynced == 0){
				Toast.makeText(getApplicationContext(), "All Data Synced", Toast.LENGTH_SHORT).show();
			}
			else {
				Toast.makeText(getApplicationContext(), "Please Sync Again", Toast.LENGTH_SHORT).show();
			}
			driverLocationsDB.close();
			jobsDB.close();
			userDB.close();
		}
		
	}
	
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
}
