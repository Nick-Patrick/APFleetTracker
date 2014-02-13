package com.aphaulage.apfleettracker;

import java.util.Locale;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PendingJobsActivity extends FragmentActivity {
	
	Cursor c;
	
	Cursor jobsCursor;
	int jobsCount = 1;
	
	Cursor collectionsCursor;
	Cursor dropoffsCursor;
	Cursor vehiclesCursor;
	Cursor packagesCursor;
	Cursor jobPackagesCursor;
	
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
		userDB.close();
		mEmail = c.getString(3);
		mFirstName = c.getString(1);


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.pending_jobs, menu);
		return true;
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
			
			jobNameTextView.setOnClickListener(new View.OnClickListener() {
				
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
			
			return rootView;
		}
	}

}
