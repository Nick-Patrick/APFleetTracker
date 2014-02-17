package com.aphaulage.apfleettracker;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class StartDayActivity extends Activity {
	DBAdapter dbAdapter;
	UsersDBAdapter userDB;
	JobsDBAdapter jobsDB;
	LocationsDBAdapter locationsDB;
	VehiclesDBAdapter vehiclesDB;
	PackagesDBAdapter packagesDB;
	JobPackagesDBAdapter jobPackagesDB;
	
	Cursor usersCursor;
	JsonParser jsonParser = new JsonParser();
	private static final String driversUpdateUrl = "http://aphaulage.co.uk/apTracker/drivers/update";
	private static final String getDriverJobsUrl = "http://aphaulage.co.uk/apTracker/jobs/assignedJobsByDriverId.json";
	private static final String getLocationsUrl = "http://aphaulage.co.uk/apTracker/locations/getAllLocations.json";
	private static final String getVehiclesUrl = "http://aphaulage.co.uk/apTracker/vehicles/getAllVehicles.json";
	private static final String getPackagesUrl = "http://aphaulage.co.uk/apTracker/packages/getAllPackages.json";

	
	
	TextView userFirstName;
	TextView userLastName;
	Button pendingJobs;
	
	String mDriverId;
	String mDriverEmail;
	String mAvailable;
	
	Intent intent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_day);
		dbAdapter = new DBAdapter(getApplicationContext());

		getActionBar().setTitle("Driver Details");
		userDB = new UsersDBAdapter(getApplicationContext());
		jobsDB = new JobsDBAdapter(getApplicationContext());
		locationsDB = new LocationsDBAdapter(getApplicationContext());
		vehiclesDB = new VehiclesDBAdapter(getApplicationContext());
		packagesDB = new PackagesDBAdapter(getApplicationContext());
		jobPackagesDB = new JobPackagesDBAdapter(getApplicationContext());
		

		userDB.open();
		usersCursor = userDB.getAllUsers();
		usersCursor.moveToNext();
		userFirstName = (TextView)findViewById(R.id.user_first_name);
		userLastName = (TextView)findViewById(R.id.user_last_name);
		userFirstName.setText(usersCursor.getString(1) + " ");
		userLastName.setText(usersCursor.getString(2));
		
		pendingJobs = (Button)findViewById(R.id.pending_jobs_button);
		
		if(usersCursor.getString(6) == "Available"){
			pendingJobs.setText("End Working Day");
		}
		
		userDB.close();

		pendingJobs.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				userDB.open();
				usersCursor = userDB.getAllUsers();
				usersCursor.moveToNext();
				mDriverEmail = usersCursor.getString(3);
				mDriverId = usersCursor.getString(8);
				Log.i("mDriverId", mDriverId);
				Log.i("c availablity - ", usersCursor.getString(6).toString());
				if(usersCursor.getString(6).equals("Available")){
					userDB.updateUserRecord("available", "Unavailable", mDriverId);
					Log.i("StartDayActivity", "Day ended");
					mAvailable = "Unavailable";
					intent = new Intent(StartDayActivity.this, EndDayActivity.class);
				}
				else {
					Log.i("StartDayActivity", "Day started");
					userDB.updateUserRecord("available", "Available", mDriverId);
					mAvailable = "Available";
					intent = new Intent(StartDayActivity.this, PendingJobsActivity.class);
				}


				userDB.close();
				new UpdateAvailability().execute();
			}
			
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.start_day, menu);
	    return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
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

	
		
		// private static final ProgressDialog progDailog = null;

		@Override
	        protected void onPreExecute() {
	            super.onPreExecute();
	            ProgressDialog pDialog = ProgressDialog.show(StartDayActivity.this, "Finding Jobs", "Searching..");
	            pDialog.show();
	        }
		
		@Override
		protected String doInBackground(String... args) {


				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("email", mDriverEmail));
				params.add(new BasicNameValuePair("available", mAvailable));
				params.add(new BasicNameValuePair("key", "9c36c7108a73324100bc9305f581979071d45ee9"));
				Log.i("doInBackground", "Starting");
				Log.i("params: ", params.toString());
				try {
					JSONObject json = jsonParser.makeHttpRequest(driversUpdateUrl + "/" + mDriverEmail + ".json", "POST", params);
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
						if(json.getString("message").equals("Driver Updated")){
							Log.i("Well done", "Well done");
							userDB.open();
							userDB.updateUserRecord("synced", "Yes", mDriverId);
							userDB.close();
							jobsDB.open();
							jobPackagesDB.open();
							Log.i("DB Open", "Yes");
							for(int i=0; i<jsonJobs.getJSONArray("message").length(); i++){
								Log.i("for", "for started");
								Log.i("1: ", jsonJobs.getJSONArray("message").getJSONObject(i).getJSONObject("Job").toString());
								//Log.i("1: ", jsonJobs.getJSONArray("message").getJSONObject(i).getJSONArray("DriverVehicleJob").toString());
								//Log.i("1: ", jsonJobs.getJSONArray("message").getJSONObject(i).getJSONObject("DriverVehicleJob").getString("driver_id"));
								//Log.i("2: ", jsonJobs.getJSONArray("message").getJSONObject(i).getJSONArray("DriverVehicleJob").getJSONObject(0).getString("vehicle_id"));
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
										jsonJobs.getJSONArray("message").getJSONObject(i).getJSONObject("Job").getString("created_by")
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
						}
						else {
							Log.i("No", "no");
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
				finish();
				return null;
			}
		
		@Override
		protected void onPostExecute(String unused){
			super.onPostExecute(unused);
		}
		
	}
}
