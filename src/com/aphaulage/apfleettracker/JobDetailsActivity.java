package com.aphaulage.apfleettracker;

import java.util.Locale;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class JobDetailsActivity extends FragmentActivity {

	public static String job_id;
	public String vehicle_id;
	public static String dropoff_id;
	public static String collection_id;
	public String packages_count;
	
	JobsDBAdapter jobsDB;
	LocationsDBAdapter locationsDB;
	VehiclesDBAdapter vehiclesDB;
	JobPackagesDBAdapter jobPackagesDB;
	PackagesDBAdapter packagesDB;
	DBAdapter dbAdapter;
	
	Cursor jobsCursor;
	Cursor collectionsCursor;
	Cursor dropoffsCursor;
	Cursor vehiclesCursor;

	
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
		setContentView(R.layout.activity_job_details);

		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		dbAdapter = new DBAdapter(getApplicationContext());



	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.active_job, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		
		case R.id.menu_item_map:
			Intent i = new Intent(this.getApplicationContext(), JobDetailsMapActivity.class);
			i.putExtra("job_id", job_id);
			i.putExtra("dropoff_id", dropoff_id);
			i.putExtra("collection_id", collection_id);
			startActivity(i);
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
		case R.id.menu_quit:
			finish();
            System.exit(0);
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

			Intent intent = getIntent();
			job_id = intent.getStringExtra("job_id");
			vehicle_id = intent.getStringExtra("vehicle_id");
			packages_count = intent.getStringExtra("packages_count");
			
			jobsDB = new JobsDBAdapter(getApplicationContext());
			vehiclesDB = new VehiclesDBAdapter(getApplicationContext());
			locationsDB = new LocationsDBAdapter(getApplicationContext());
			jobPackagesDB = new JobPackagesDBAdapter(getApplicationContext());
			packagesDB = new PackagesDBAdapter(getApplicationContext());
			jobsDB.open();
			Log.i("job id:", job_id);
			jobsCursor = jobsDB.getJob(job_id);
			collection_id = jobsCursor.getString(2);
			dropoff_id = jobsCursor.getString(3);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment = null;
			Bundle args = new Bundle();
			switch(position){
				case 0: 
					fragment = new JobDetailsFragment();
					jobsDB.open();
					args.putString(JobDetailsFragment.JOB_NAME, jobsCursor.getString(1));
					args.putString(JobDetailsFragment.JOB_CREATED_BY, jobsCursor.getString(12));
					args.putString(JobDetailsFragment.JOB_DUE_DATE, jobsCursor.getString(9));
					args.putString(JobDetailsFragment.JOB_ADDITIONAL_DETAILS, jobsCursor.getString(8));
					jobsDB.close();
					fragment.setArguments(args);
					return fragment;

				case 1: 
					fragment = new CollectionDetailsFragment();
					locationsDB.open();
					collectionsCursor = locationsDB.getLocation(collection_id);
					args.putString(CollectionDetailsFragment.COLLECTION_NAME, collectionsCursor.getString(1));
					args.putString(CollectionDetailsFragment.COLLECTION_ADDRESS1, collectionsCursor.getString(2));
					args.putString(CollectionDetailsFragment.COLLECTION_ADDRESS2, collectionsCursor.getString(3));
					args.putString(CollectionDetailsFragment.COLLECTION_ADDRESS3, collectionsCursor.getString(4));
					args.putString(CollectionDetailsFragment.COLLECTION_TOWN, collectionsCursor.getString(5));
					args.putString(CollectionDetailsFragment.COLLECTION_COUNTY, collectionsCursor.getString(6));
					args.putString(CollectionDetailsFragment.COLLECTION_POSTCODE, collectionsCursor.getString(7));
					args.putString(CollectionDetailsFragment.COLLECTION_TELEPHONE, collectionsCursor.getString(9));

					locationsDB.close();
					fragment.setArguments(args);
					return fragment;

				case 2: 
					fragment = new DropoffDetailsFragment();
					locationsDB.open();
					dropoffsCursor = locationsDB.getLocation(dropoff_id);
					args.putString(DropoffDetailsFragment.DROPOFF_NAME, dropoffsCursor.getString(1));
					args.putString(DropoffDetailsFragment.DROPOFF_ADDRESS1, dropoffsCursor.getString(2));
					args.putString(DropoffDetailsFragment.DROPOFF_ADDRESS2, dropoffsCursor.getString(3));
					args.putString(DropoffDetailsFragment.DROPOFF_ADDRESS3, dropoffsCursor.getString(4));
					args.putString(DropoffDetailsFragment.DROPOFF_TOWN, dropoffsCursor.getString(5));
					args.putString(DropoffDetailsFragment.DROPOFF_COUNTY, dropoffsCursor.getString(6));
					args.putString(DropoffDetailsFragment.DROPOFF_POSTCODE, dropoffsCursor.getString(7));
					args.putString(DropoffDetailsFragment.DROPOFF_TELEPHONE, dropoffsCursor.getString(9));

					locationsDB.close();
					fragment.setArguments(args);
					return fragment;

				case 3:
					fragment = new VehicleDetailsFragment();
					vehiclesDB.open();
					vehiclesCursor = vehiclesDB.getVehicle(vehicle_id);
					args.putString(VehicleDetailsFragment.VEHICLE_NAME, vehiclesCursor.getString(1));
					args.putString(VehicleDetailsFragment.VEHICLE_REG_NUMBER, vehiclesCursor.getString(2));
					vehiclesDB.close();
					fragment.setArguments(args);
					return fragment;					
				
				default:
					fragment = new PackageDetailsFragment();
					jobPackagesDB.open();
					packagesDB.open();
					Cursor jobPackagesCursor = jobPackagesDB.getJobPackagesByJob(job_id);
					jobPackagesCursor.moveToPosition(position - 4);
					
					Cursor packagesCursor = packagesDB.getPackage(jobPackagesCursor.getString(2));
					args.putString(PackageDetailsFragment.PACKAGE_NAME, packagesCursor.getString(1));
					args.putString(PackageDetailsFragment.PACKAGE_LENGTH, packagesCursor.getString(2));
					args.putString(PackageDetailsFragment.PACKAGE_WIDTH, packagesCursor.getString(3));
					args.putString(PackageDetailsFragment.PACKAGE_HEIGHT, packagesCursor.getString(4));
					args.putString(PackageDetailsFragment.PACKAGE_WEIGHT, packagesCursor.getString(5));
				    args.putString(PackageDetailsFragment.PACKAGE_SPECIAL_REQS, packagesCursor.getString(6));
					packagesDB.close();
					jobPackagesDB.close();
					fragment.setArguments(args);
					return fragment;			
				}
			
		}

		@Override
		public int getCount() {
			// Show 3 pages + page for each package.
			int packagesCount = Integer.parseInt(packages_count);
			return 4 + packagesCount;
		}
		
		@Override
		public int getItemPosition(Object object){
			return POSITION_NONE;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.job_details_job_title).toUpperCase(l);
			case 1:
				return getString(R.string.job_details_collection_title).toUpperCase(l);
			case 2:
				return getString(R.string.job_details_dropoff_title).toUpperCase(l);
			case 3:
				return getString(R.string.job_details_vehicle_title).toUpperCase(l);
			case 4:
				return "Package 1";
			case 5:
				return "Package 2";
			case 6:
				return "Package 3";
			case 7:
				return "Package 4";
			case 8:
				return "Package 5";
			case 9:
				return "Package 6";
			case 10:
				return "Package 7";
			case 11:
				return "Package 8";
			case 12:
				return "Package 9";
			case 13:
				return "Package 10";
			case 14:
				return "Package 11";
			case 15:
				return "Package 12";
			case 16:
				return "Package 13";
			}
			return null;
		}
	}
	
	public static class JobDetailsFragment extends Fragment {

		public static final String JOB_NAME = "job_name";
		public static final String JOB_CREATED_BY = "job_created_by";
		public static final String JOB_DUE_DATE = "job_due_date";
		public static final String JOB_ADDITIONAL_DETAILS = "job_additional_details";

	    public static JobDetailsFragment newInstance() {
            JobDetailsFragment f = new JobDetailsFragment();
            return f;
        }
		public JobDetailsFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			
			Bundle args = getArguments();
			String jobName = args.getString(JOB_NAME);
			String jobCreatedBy = args.getString(JOB_CREATED_BY);
			String jobDueDate = args.getString(JOB_DUE_DATE);
			String jobAdditionalDetails = args.getString(JOB_ADDITIONAL_DETAILS);
			
			View jobView = inflater.inflate(R.layout.fragment_job_details, container, false);
			
			TextView jobNameTextView = (TextView)jobView.findViewById(R.id.job_details_name);
			TextView jobCreatedByTextView = (TextView)jobView.findViewById(R.id.job_details_created_by);
			TextView jobDueDateTextView = (TextView)jobView.findViewById(R.id.job_details_due_by);
			TextView jobAdditionalDetailsTextView = (TextView)jobView.findViewById(R.id.job_details_additional_details);
			
			jobNameTextView.setText(jobName);
			jobCreatedByTextView.setText(jobCreatedBy);
			jobDueDateTextView.setText(jobDueDate);
			jobAdditionalDetailsTextView.setText(jobAdditionalDetails);
			

			
			return jobView;
		}
	}
	
	public static class CollectionDetailsFragment extends Fragment {

		public static final String COLLECTION_NAME = "collection_name";
		public static final String COLLECTION_ADDRESS1 = "collection_address1";
		public static final String COLLECTION_ADDRESS2 = "collection_address2";
		public static final String COLLECTION_ADDRESS3 = "collection_address3";
		public static final String COLLECTION_TOWN = "collection_town";
		public static final String COLLECTION_COUNTY = "collection_county";
		public static final String COLLECTION_POSTCODE = "collection_postcode";
		public static final String COLLECTION_TELEPHONE = "collection_telephone";
		
	    public static CollectionDetailsFragment newInstance() {
            CollectionDetailsFragment f = new CollectionDetailsFragment();
            return f;
        }
		
		public CollectionDetailsFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			
			Bundle args = getArguments();
			String collectionName = args.getString(COLLECTION_NAME);
			String collectionAddress1 = args.getString(COLLECTION_ADDRESS1);
			String collectionAddress2 = args.getString(COLLECTION_ADDRESS2);
			String collectionAddress3 = args.getString(COLLECTION_ADDRESS3);
			String collectionTown = args.getString(COLLECTION_TOWN);
			String collectionCounty = args.getString(COLLECTION_COUNTY);
			String collectionPostcode = args.getString(COLLECTION_POSTCODE);
			String collectionTelephone = args.getString(COLLECTION_TELEPHONE);
			
			View collectionView = inflater.inflate(R.layout.fragment_collection_details, container, false);
			
			TextView collectionNameTextView = (TextView)collectionView.findViewById(R.id.collection_details_name);
			TextView collectionAddress1TextView = (TextView)collectionView.findViewById(R.id.collection_details_address1);
			TextView collectionAddress2TextView = (TextView)collectionView.findViewById(R.id.collection_details_address2);
			TextView collectionAddress3TextView = (TextView)collectionView.findViewById(R.id.collection_details_address3);
			TextView collectionTownTextView = (TextView)collectionView.findViewById(R.id.collection_details_town);
			TextView collectionCountyTextView = (TextView)collectionView.findViewById(R.id.collection_details_county);
			TextView collectionPostcodeTextView = (TextView)collectionView.findViewById(R.id.collection_details_postcode);
			TextView collectionTelephoneTextView = (TextView)collectionView.findViewById(R.id.collection_details_telephone);
			
			collectionNameTextView.setText(collectionName);
			collectionAddress1TextView.setText(collectionAddress1);
			if(collectionAddress2 != ""){
				collectionAddress2TextView.setText(collectionAddress2);
			}
			else {
				collectionAddress2TextView.setVisibility(0);
			}
			if(collectionAddress2 != ""){
				collectionAddress3TextView.setText(collectionAddress3);
			}
			else {
				collectionAddress3TextView.setVisibility(0);
			}
			collectionTownTextView.setText(collectionTown);
			collectionCountyTextView.setText(collectionCounty);
			collectionPostcodeTextView.setText(collectionPostcode);
			collectionTelephoneTextView.setText(collectionTelephone);
			

			
			return collectionView;
		}
	}
	
	public static class DropoffDetailsFragment extends Fragment {

		public static final String DROPOFF_NAME = "dropoff_name";
		public static final String DROPOFF_ADDRESS1 = "dropoff_address1";
		public static final String DROPOFF_ADDRESS2 = "dropoff_address2";
		public static final String DROPOFF_ADDRESS3 = "dropoff_address3";
		public static final String DROPOFF_TOWN = "dropoff_town";
		public static final String DROPOFF_COUNTY = "dropoff_county";
		public static final String DROPOFF_POSTCODE = "dropoff_postcode";
		public static final String DROPOFF_TELEPHONE = "dropoff_telephone";
		
	    public static DropoffDetailsFragment newInstance() {
            DropoffDetailsFragment f = new DropoffDetailsFragment();
            return f;
        }
		
		public DropoffDetailsFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			Bundle args = getArguments();
			String dropoffName = args.getString(DROPOFF_NAME);
			String dropoffAddress1 = args.getString(DROPOFF_ADDRESS1);
			String dropoffAddress2 = args.getString(DROPOFF_ADDRESS2);
			String dropoffAddress3 = args.getString(DROPOFF_ADDRESS3);
			String dropoffTown = args.getString(DROPOFF_TOWN);
			String dropoffCounty = args.getString(DROPOFF_COUNTY);
			String dropoffPostcode = args.getString(DROPOFF_POSTCODE);
			String dropoffTelephone = args.getString(DROPOFF_TELEPHONE);
			
			View dropoffView = inflater.inflate(R.layout.fragment_dropoff_details, container, false);
			
			TextView dropoffNameTextView = (TextView)dropoffView.findViewById(R.id.dropoff_details_name);
			TextView dropoffAddress1TextView = (TextView)dropoffView.findViewById(R.id.dropoff_details_address1);
			TextView dropoffAddress2TextView = (TextView)dropoffView.findViewById(R.id.dropoff_details_address2);
			TextView dropoffAddress3TextView = (TextView)dropoffView.findViewById(R.id.dropoff_details_address3);
			TextView dropoffTownTextView = (TextView)dropoffView.findViewById(R.id.dropoff_details_town);
			TextView dropoffCountyTextView = (TextView)dropoffView.findViewById(R.id.dropoff_details_county);
			TextView dropoffPostcodeTextView = (TextView)dropoffView.findViewById(R.id.dropoff_details_postcode);
			TextView dropoffTelephoneTextView = (TextView)dropoffView.findViewById(R.id.dropoff_details_telephone);
			
			dropoffNameTextView.setText(dropoffName);
			dropoffAddress1TextView.setText(dropoffAddress1);
			dropoffAddress2TextView.setText(dropoffAddress2);
			dropoffAddress3TextView.setText(dropoffAddress3);
			dropoffTownTextView.setText(dropoffTown);
			dropoffCountyTextView.setText(dropoffCounty);
			dropoffPostcodeTextView.setText(dropoffPostcode);
			dropoffTelephoneTextView.setText(dropoffTelephone);			
			
			return dropoffView;
		}
	}
	
	public static class VehicleDetailsFragment extends Fragment {

		public static final String VEHICLE_NAME = "vehicle_name";
		public static final String VEHICLE_REG_NUMBER = "vehicle_reg";
		
	    public static VehicleDetailsFragment newInstance() {
            VehicleDetailsFragment f = new VehicleDetailsFragment();
            return f;
        }

		public VehicleDetailsFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			Bundle args = getArguments();
			String vehicleName = args.getString(VEHICLE_NAME);
			String regNumber = args.getString(VEHICLE_REG_NUMBER);
			
			
			View vehicleView = inflater.inflate(R.layout.fragment_vehicle_details, container, false);
			TextView vehicleNameTextView = (TextView) vehicleView.findViewById(R.id.vehicle_details_name);
			TextView vehicleRegTextView = (TextView) vehicleView.findViewById(R.id.vehicle_details_reg_num);
			vehicleNameTextView.setText(vehicleName);
			vehicleRegTextView.setText(regNumber);

			return vehicleView;
		}
	}
	
	public static class PackageDetailsFragment extends Fragment {

		public static final String PACKAGE_NAME = "package_name";
		public static final String PACKAGE_LENGTH = "package_length";
		public static final String PACKAGE_WIDTH = "package_width";
		public static final String PACKAGE_HEIGHT = "package_height";
		public static final String PACKAGE_WEIGHT = "package_weight";
		public static final String PACKAGE_SPECIAL_REQS = "package_special_reqs";
		
	    public static PackageDetailsFragment newInstance() {
            PackageDetailsFragment f = new PackageDetailsFragment();
            return f;
        }

		public PackageDetailsFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			Bundle args = getArguments();
			String packageName = args.getString(PACKAGE_NAME);
			String packageLength = args.getString(PACKAGE_LENGTH);
			String packageWidth = args.getString(PACKAGE_WIDTH);
			String packageHeight = args.getString(PACKAGE_HEIGHT);
			String packageWeight = args.getString(PACKAGE_WEIGHT);
			String packageSpecialReqs = args.getString(PACKAGE_SPECIAL_REQS);
			
			View packageView = inflater.inflate(R.layout.fragment_package_details, container, false);
			TextView packageNameTextView = (TextView)packageView.findViewById(R.id.job_details_package_name);
			TextView packageLengthTextView = (TextView)packageView.findViewById(R.id.job_details_package_length);
			TextView packageWidthTextView = (TextView)packageView.findViewById(R.id.job_details_package_width);
			TextView packageHeightTextView = (TextView)packageView.findViewById(R.id.job_details_package_height);
			TextView packageWeightTextView = (TextView)packageView.findViewById(R.id.job_details_package_weight);
			TextView packageSpecialReqsTextView = (TextView)packageView.findViewById(R.id.job_details_package_special_reqs);
			
			packageNameTextView.setText(packageName);
			packageLengthTextView.setText(packageLength);
			packageWidthTextView.setText(packageWidth);
			packageHeightTextView.setText(packageHeight);
			packageWeightTextView.setText(packageWeight);
			packageSpecialReqsTextView.setText(packageSpecialReqs);

			return packageView;
		}
	}

}
