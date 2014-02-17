package com.aphaulage.apfleettracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class PendingJobsActivity extends FragmentActivity {
	
	Cursor c;
	
	Cursor jobsCursor;
	int jobsCount = 1;
	
	//Refresh Jobs Vars
	JsonParser jsonParser = new JsonParser();
	private static final String getDriverJobsUrl = "http://aphaulage.co.uk/apTracker/jobs/assignedJobsByDriverId.json";
	private static final String getLocationsUrl = "http://aphaulage.co.uk/apTracker/locations/getAllLocations.json";
	private static final String getVehiclesUrl = "http://aphaulage.co.uk/apTracker/vehicles/getAllVehicles.json";
	private static final String getPackagesUrl = "http://aphaulage.co.uk/apTracker/packages/getAllPackages.json";
	String mDriverId;
	String mDriverEmail;
	
	
	Cursor collectionsCursor;
	Cursor dropoffsCursor;
	Cursor vehiclesCursor;
	Cursor packagesCursor;
	Cursor jobPackagesCursor;
	
	DBAdapter dbAdapter;
	UsersDBAdapter userDB;
	JobsDBAdapter jobsDB;
	LocationsDBAdapter locationsDB;
	VehiclesDBAdapter vehiclesDB;
	PackagesDBAdapter packagesDB;
	JobPackagesDBAdapter jobPackagesDB;
	
	public static String mEmail;
	public static String mFirstName;

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pending_jobs);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		userDB = new UsersDBAdapter(getApplicationContext());
		userDB.open();
		c = userDB.getAllUsers();
		c.moveToNext();
		mDriverEmail = c.getString(3);
		mDriverId = c.getString(8);
		userDB.close();
		mEmail = c.getString(3);
		mFirstName = c.getString(1);
		dbAdapter = new DBAdapter(getApplicationContext());


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
		case R.id.menu_item_refresh:
			new RefreshJobs().execute();
			break;
		default:
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
			jobsDB = new JobsDBAdapter(getApplicationContext());
			jobsDB.open();
			jobsCursor = jobsDB.getAllJobs();
			locationsDB = new LocationsDBAdapter(getApplicationContext());
			vehiclesDB = new VehiclesDBAdapter(getApplicationContext());
			packagesDB = new PackagesDBAdapter(getApplicationContext());
			jobPackagesDB = new JobPackagesDBAdapter(getApplicationContext());

		}

		@Override
		public Fragment getItem(int position) {
			locationsDB.open();
			vehiclesDB.open();
			jobPackagesDB.open();

			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) 
			Fragment fragment = new DummySectionFragment();
			Bundle args = new Bundle();
			
			jobsCursor.moveToPosition(position);

				args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
				args.putString(DummySectionFragment.DRIVER_NAME, mFirstName);
				args.putString(DummySectionFragment.JOB_NAME, jobsCursor.getString(1));
				args.putString(DummySectionFragment.JOB_ADDITIONAL_DETAILS, jobsCursor.getString(8));
				args.putString(DummySectionFragment.DUE_BY, jobsCursor.getString(9));
				if(jobsCursor.getString(7) == ""){
					args.putString(DummySectionFragment.ASSIGNED_AT,jobsCursor.getString(6));
				}
				else {
					args.putString(DummySectionFragment.ASSIGNED_AT, jobsCursor.getString(7));
				}
				args.putString(DummySectionFragment.ASSIGNED_BY, jobsCursor.getString(12));
			
			collectionsCursor = locationsDB.getLocation(jobsCursor.getString(2));
			dropoffsCursor = locationsDB.getLocation(jobsCursor.getString(3));
			Log.i("vehicle id: ", jobsCursor.getString(10));
			vehiclesCursor = vehiclesDB.getVehicle(jobsCursor.getString(10));
			jobPackagesCursor = jobPackagesDB.getJobPackagesByJob(jobsCursor.getString(0));
			args.putString(DummySectionFragment.COLLECTION_NAME, collectionsCursor.getString(1));
			args.putString(DummySectionFragment.DROPOFF_NAME, dropoffsCursor.getString(1));
			
			args.putString(DummySectionFragment.VEHICLE_NAME, vehiclesCursor.getString(1));
			args.putString(DummySectionFragment.PACKAGE_COUNT, ""+jobPackagesCursor.getCount());
			
			args.putString(DummySectionFragment.JOB_ID, jobsCursor.getString(0));
			args.putString(DummySectionFragment.VEHICLE_ID, vehiclesCursor.getString(0));
			
			vehiclesDB.close();
			locationsDB.close();
			jobsDB.close();
			jobPackagesDB.close();
			
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			jobsCount = jobsCursor.getCount();
			// Default - 1 Page if no jobs.
			Log.i("he", "count" + jobsCount);
			return jobsCount;
		}
		
		@Override
		public int getItemPosition(Object object){
			return POSITION_NONE;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			int titlePosition = position+1; //So no position = 0.
			return getString(R.string.pending_jobs_available_jobs).toUpperCase(l) + " " + titlePosition;
		}
	}


	public static class DummySectionFragment extends Fragment {
		public static final String JOB_NAME = "job_name";
		public static final String JOB_ADDITIONAL_DETAILS = "job_additional_details";
		public static final String DUE_BY = "due_by";
		public static final String ASSIGNED_AT = "assigned_at";
		public static final String ASSIGNED_BY = "assigned_by";
		
		public static final String COLLECTION_NAME = "collection_name";
		public static final String DROPOFF_NAME = "dropoff_name";
		
		public static final String VEHICLE_NAME = "vehicle_name";
		
		public static final String PACKAGE_COUNT = "package_count";

		public static final String ARG_SECTION_NUMBER = "section_number";
		public static final String DRIVER_NAME = "driver_name";
		
		public static final String JOB_ID = "job_id";
		public static final String VEHICLE_ID = "vehicle_id";

		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

			Bundle args = getArguments();
			String dueBy = args.getString(DUE_BY);
			String driverName = args.getString(DRIVER_NAME);
			String jobName = args.getString(JOB_NAME);
			String collectionName = args.getString(COLLECTION_NAME);
			String dropoffName = args.getString(DROPOFF_NAME);
			String vehicleName = args.getString(VEHICLE_NAME);
			final String packagesCount = args.getString(PACKAGE_COUNT);
			String additionalDetails = args.getString(JOB_ADDITIONAL_DETAILS);
			String assignedBy = args.getString(ASSIGNED_BY);
			String assignedAt = args.getString(ASSIGNED_AT);
			final String jobId = args.getString(JOB_ID);
			final String vehicleId = args.getString(VEHICLE_ID);
			
			
			View rootView = inflater.inflate(R.layout.fragment_pending_jobs_dummy, container, false);
			
			TextView dueByTextView = (TextView) rootView.findViewById(R.id.due_by);
			TextView jobNameTextView = (TextView) rootView.findViewById(R.id.job_name);
			TextView driverJobsTextView = (TextView) rootView.findViewById(R.id.pending_jobs_user);
			TextView collectionNameTextView = (TextView) rootView.findViewById(R.id.collection_name);
			TextView dropoffNameTextView = (TextView) rootView.findViewById(R.id.dropoff_name);
			TextView vehicleNameTextView = (TextView) rootView.findViewById(R.id.vehicle_name);
			TextView packagesCountTextView = (TextView) rootView.findViewById(R.id.packages_count);
			TextView additionalDetailsTextView = (TextView) rootView.findViewById(R.id.additional_details);
			TextView assignedByTextView = (TextView) rootView.findViewById(R.id.assigned_by);
			TextView assignedAtTextView = (TextView) rootView.findViewById(R.id.assigned_at);
			
			driverJobsTextView.setText("Assigned To: " + driverName);			
			
				jobNameTextView.setText(jobName);
				collectionNameTextView.setText(collectionName);
				dueByTextView.setText("Due by: " + dueBy);
				dropoffNameTextView.setText(dropoffName);
				vehicleNameTextView.setText(vehicleName);
				packagesCountTextView.setText(packagesCount);
			
			if(additionalDetails.length() > 1){
				additionalDetailsTextView.setText(additionalDetails);
			}
			else {
				TextView additionalDetailsLabel = (TextView) rootView.findViewById(R.id.additional_details_label);
				additionalDetailsLabel.setText("");
			}
			assignedAtTextView.setText("At: " + assignedAt);
			assignedByTextView.setText("Assigned By: " + assignedBy);
			
			
			Button viewMoreButton = (Button)rootView.findViewById(R.id.pending_jobs_more_details);
			viewMoreButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent i = new Intent(v.getContext(), JobDetailsActivity.class);
					i.putExtra("job_id", jobId);
					i.putExtra("vehicle_id", vehicleId);
					i.putExtra("packages_count", packagesCount);
					startActivity(i);
				}
			});
			
			Button startJobButton = (Button)rootView.findViewById(R.id.start_job_button);
			
		
			return rootView;
		}
	}

	class RefreshJobs extends AsyncTask<String, String, String>{

	
		
		// private static final ProgressDialog progDailog = null;

		@Override
	        protected void onPreExecute() {
	            super.onPreExecute();
	            ProgressDialog pDialog = ProgressDialog.show(PendingJobsActivity.this, "Finding Jobs", "Searching..");
	            pDialog.show();
	        }
		
		@Override
		protected String doInBackground(String... args) {


				
				try {
					List<NameValuePair> jobParams = new ArrayList<NameValuePair>();
					jobParams.add(new BasicNameValuePair("driver_id", mDriverId));
					jobParams.add(new BasicNameValuePair("key", "9c36c7108a73324100bc9305f581979071d45ee9"));
					JSONObject jsonJobs = jsonParser.makeHttpRequest(getDriverJobsUrl, "POST", jobParams);
					Log.i("beforeJsonJobs", "hello");
					Log.i("jsonJobs", jsonJobs.toString());
					List<NameValuePair> locationParams = new ArrayList<NameValuePair>();
					locationParams.add(new BasicNameValuePair("key", "9c36c7108a73324100bc9305f581979071d45ee9"));
					JSONObject jsonLocations = jsonParser.makeHttpRequest(getLocationsUrl, "POST", locationParams);
					try {
							jobsDB.open();
							jobPackagesDB.open();
							Log.i("DB Open", "Yes");
							for(int i=0; i<jsonJobs.getJSONArray("message").length(); i++){
								Log.i("for", "for started");
								Log.i("1: ", jsonJobs.getJSONArray("message").getJSONObject(i).getJSONObject("Job").toString());
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
						
						
					 catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		
		@Override
		protected void onPostExecute(String unused){
			super.onPostExecute(unused);
			finish();
			startActivity(getIntent());
		}
		
	}
}

