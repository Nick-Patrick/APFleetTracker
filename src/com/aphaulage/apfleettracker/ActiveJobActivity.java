package com.aphaulage.apfleettracker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.app.PendingIntent;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

@SuppressLint("SimpleDateFormat")
public class ActiveJobActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener, LocationListener, com.google.android.gms.location.LocationListener  {

	
	private Intent mIntentService;
	 private PendingIntent mPendingIntent;
	  

	
    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 30;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 20;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    // Define an object that holds accuracy and frequency parameters
    LocationRequest mLocationRequest;
    LocationClient mLocationClient;
    boolean mUpdatesRequested;
	
    Alarm alarm = new Alarm();
	
	protected String jobId;
	protected String jobStatus;
	protected String driverId;
	
	protected String trackingEnabled;
	
	Button completedJobButton;
	TextView moreDetailsTextView;
	TextView jobNameTextView;
	TextView jobStartedTextView;
	TextView milesTravelledTextView;
	TextView timeTravelledTextView;
	
	Switch userDrivingSwitch;
	
	DBAdapter dbAdapter;	
	JobsDBAdapter jobsDB;
	UsersDBAdapter usersDB;
	DriverLocationsDBAdapter driverLocationsDB;
	DriverSettingsDBAdapter driverSettingsDB;
	
	Boolean firstTrack = false;
	
	JsonParser jsonParser = new JsonParser();
	private static final String updateJobUrl = "http://aphaulage.co.uk/apTracker/jobs/updateActiveJob/";
	private static final String updateDriverUrl = "http://aphaulage.co.uk/apTracker/drivers/update/";
	private static final String updateVehicleUrl = "http://aphaulage.co.uk/apTracker/vehicles/updateVehicleById.json";
	private static final String addDriverLocationUrl = "http://aphaulage.co.uk/apTracker/driverLocations/addDriverLocation.json";
	
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	
	protected double previousLat = 0;
	protected double previousLng = 0;
	//protected PowerManager.WakeLock mWakeLock; 
	LocationManager locationManager;
	
    @Override
    protected void onStart() {
    	super.onStart();
    	final PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
    //	this.mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Wakelock");
    	
    	//mWakeLock.acquire();


    }
    
    /* Prevent app from being killed on back */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        // Back?
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Back
            moveTaskToBack(true);
            return true;
        }
        else {
            // Return
            return super.onKeyDown(keyCode, event);
        }
    }
    
    @Override
    protected void onStop() {

        super.onStop();
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_active_job);
		
		
	       if(mLocationClient == null){
	    	   mLocationClient = new LocationClient(this, this, this);
	    	   //mUpdatesRequested = true;
	    	   Log.i("ActiveJobActivity", "mLocationClient created");
	       }
	       if(!mLocationClient.isConnected()){
	    	   mLocationClient.connect();
	    	   Log.i("ActiveJobActivity", "mLocationClient connecting");
	       }
	       //mUpdatesRequested = true;
	       
	      if(mLocationRequest == null){
				//Create LocationRequest object.
		        mLocationRequest = LocationRequest.create();
		        // Use high accuracy
		       // mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		        // Set the update interval to 5 seconds
		        mLocationRequest.setInterval(UPDATE_INTERVAL);
		        // Set the fastest update interval to 1 second
		        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
			}
			
		
		mIntentService = new Intent(this,LocationTrackingService.class);
		mPendingIntent = PendingIntent.getService(this, 1, mIntentService, 0);
		
		
		// Show the Up button in the action bar.
		setupActionBar();
		usersDB = new UsersDBAdapter(getApplicationContext());
		dbAdapter = new DBAdapter(getApplicationContext());
		jobsDB = new JobsDBAdapter(getApplicationContext());
		driverLocationsDB = new DriverLocationsDBAdapter(getApplicationContext());
		driverSettingsDB = new DriverSettingsDBAdapter(getApplicationContext());
		Intent intent = getIntent();
		jobId = intent.getStringExtra("job_id");
		driverId = intent.getStringExtra("driver_id");
		if(intent.getStringExtra("job_id") == null){
			usersDB.open();
			jobsDB.open();
			Cursor driverCursor = usersDB.getAllUsers();
			driverCursor.moveToFirst();
			driverId = driverCursor.getString(8);
			Cursor jobCursor = jobsDB.getJobByParam("STATUS", "Active");
			jobCursor.moveToFirst();
			jobId = jobCursor.getString(0);
			jobsDB.close();
			usersDB.close();
		}
		
		
		new UpdateJobStatus().execute();
		
		
       
       
       completedJobButton = (Button)findViewById(R.id.active_job_complete_job_button);
       completedJobButton.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View v) {
			
			AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

		    builder.setTitle("Confirm Job Complete");
		    builder.setMessage("Finish job?");

		    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

		        public void onClick(DialogInterface dialog, int which) {
		        	// If the client is connected
			        if (mLocationClient.isConnected()) {
			            /*
			             * Remove location updates for a listener.
			             * The current Activity is the listener, so
			             * the argument is "this".
			             */
			        	mLocationClient.removeLocationUpdates(mPendingIntent);
    			        mLocationClient.disconnect();	
			        } 
		            mUpdatesRequested = false;

			        
			        driverSettingsDB.open();
			        driverSettingsDB.updateDriverSettingRecord("TRACKING_STATUS", "No");
			        driverSettingsDB.close();

					String completedNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		            jobsDB.open();
		            jobsDB.updateJobRecord("STATUS", "Complete", jobId);
		            jobsDB.updateJobRecord("COMPLETED_DATE", completedNow, jobId);
		            jobsDB.updateJobRecord("SYNCED", "No", jobId);
		            jobsDB.close();
		            usersDB.open();
		            usersDB.updateUserRecord("AVAILABLE", "Available", driverId);
		            usersDB.updateUserRecord("SYNCED", "No", driverId);
		            usersDB.close();
		    		new UpdateJobFinished().execute();	
			        
		            dialog.dismiss();
		        }

		    });

		    builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

		        @Override
		        public void onClick(DialogInterface dialog, int which) {
		            // Do nothing
		            dialog.dismiss();
		        }
		    });

		    AlertDialog alert = builder.create();
		    alert.show();
			}
		
       });
      
		
       
       moreDetailsTextView = (TextView)findViewById(R.id.active_job_job_name);
       moreDetailsTextView.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View v) {
			Intent moreDetailsIntent = new Intent(getApplicationContext(), ActiveJobDetailsActivity.class);
			moreDetailsIntent.putExtra("driver_id", driverId);
			moreDetailsIntent.putExtra("job_id", jobId);
			startActivity(moreDetailsIntent);
		}
    	   
       });
       jobsDB.open();
       Cursor jobDetailsCursor = jobsDB.getJob(jobId);
       jobsDB.close();
       jobNameTextView = (TextView)findViewById(R.id.active_job_job_name);
       jobNameTextView.setText(jobDetailsCursor.getString(1));
       jobStartedTextView = (TextView)findViewById(R.id.active_job_job_start);
       jobStartedTextView.append(jobDetailsCursor.getString(13));
       timeTravelledTextView = (TextView)findViewById(R.id.active_job_job_time_driving);
       timeTravelledTextView.setText("01:02*");

	}
	
	   /*
     * Handle results returned to the FragmentActivity
     * by Google Play services
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST :
            /*
             * If the result code is Activity.RESULT_OK, try
             * to connect again
             */
                switch (resultCode) {
                    case Activity.RESULT_OK :
                    /*
                     * Try the request again
                     */
                    break;
                }
        }
    }
    

    



	
	
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

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
			Intent mapIntent = new Intent(this.getApplicationContext(), ActiveJobMapActivity.class);
			mapIntent.putExtra("job_id", jobId);
			startActivity(mapIntent);
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

	class UpdateJobStatus extends AsyncTask<String, String, String>{

		
		@Override
	        protected void onPreExecute() {
	            super.onPreExecute();
	        }
		
		@Override
		protected String doInBackground(String... args) {
				try{
					jobsDB.open();
					Cursor jobStarted = jobsDB.getJobByParam("ID", jobId);
					List<NameValuePair> jobParams = new ArrayList<NameValuePair>();
					jobParams.add(new BasicNameValuePair("job_id", jobId));
					jobParams.add(new BasicNameValuePair("key", "9c36c7108a73324100bc9305f581979071d45ee9"));
					jobParams.add(new BasicNameValuePair("status", "Active"));
					jobParams.add(new BasicNameValuePair("modified", jobStarted.getString(13)));
					JSONObject jsonJobsResult = jsonParser.makeHttpRequest(updateJobUrl + jobId + ".json", "POST", jobParams);
					
					jobsDB.updateJobRecord("SYNCED", "Yes", jobId);
					
				}
				catch(Exception e){
					e.printStackTrace();
				}
				finally {
					jobsDB.close();
				}
				try {
					List<NameValuePair> driverParams = new ArrayList<NameValuePair>();
					driverParams.add(new BasicNameValuePair("id", driverId));
					driverParams.add(new BasicNameValuePair("key", "9c36c7108a73324100bc9305f581979071d45ee9"));
					driverParams.add(new BasicNameValuePair("available", "Active"));
					JSONObject jsonDriversResult = jsonParser.makeHttpRequest(updateDriverUrl + driverId + ".json", "POST", driverParams);
					usersDB.open();
					usersDB.updateUserRecord("SYNCED", "Yes", driverId);
				}
				catch(Exception e){
					e.printStackTrace();
				}
				finally {
					usersDB.close();
				}
				
				return null;
			}
		
		@Override
		protected void onPostExecute(String unused){
			super.onPostExecute(unused);
		}
		
	}
	
	class AddDriverLocation extends AsyncTask<String, String, String>{

		
		@Override
	        protected void onPreExecute() {
	            super.onPreExecute();
	        }
		
		@Override
		protected String doInBackground(String... args) {
				try{
					driverLocationsDB.open();
					//Cursor driverLocationsCursor = driverLocationsDB.getDriverLocationsByParam("SYNCED", "No");
					//Cursor allDriverLocations = driverLocationsDB.getAllDriverLocations();
					Cursor driverLocationsCursor = driverLocationsDB.getDriverLocationsNotSynced();
					//Log.i("cursorAll: ", "count: " + allDriverLocations.getCount());
					Log.i("cursor", "count: " + driverLocationsCursor.getCount());
					for(int i=0;i<=driverLocationsCursor.getCount()-1;i++){
						JSONObject jsonDriverLocationResult = null;
						driverLocationsCursor.moveToPosition(i);
						List<NameValuePair> locationParams = new ArrayList<NameValuePair>();
						locationParams.add(new BasicNameValuePair("key", "9c36c7108a73324100bc9305f581979071d45ee9"));
						locationParams.add(new BasicNameValuePair("driver_id", driverLocationsCursor.getString(1)));
						locationParams.add(new BasicNameValuePair("date_time_stamp", driverLocationsCursor.getString(2)));
						locationParams.add(new BasicNameValuePair("latitude", driverLocationsCursor.getString(3)));
						locationParams.add(new BasicNameValuePair("longitude", driverLocationsCursor.getString(4)));
						Log.i("DRIVER_ID", driverLocationsCursor.getString(1));
						driverLocationsDB.close();
						if(isNetworkAvailable()==true){
							try {
								jsonDriverLocationResult = jsonParser.makeHttpRequest(addDriverLocationUrl, "POST", locationParams);
								Log.i("jsonDriverLocationResultNew",jsonDriverLocationResult.toString());
								
								if(jsonDriverLocationResult.toString().contains("Driver Location Added")){
									Log.i("Message", "MATCHED");
									driverLocationsDB.open();
									driverLocationsDB.updateDriverLocationRecord("SYNCED", "Yes", driverLocationsCursor.getString(0));
									driverLocationsDB.close();
								}
								jsonDriverLocationResult = null;
							}
							catch (Exception e){
								e.printStackTrace();
							}
						}

					}
					
					
					
				}
				catch(Exception e){
					e.printStackTrace();
				}
				finally {
				}
				return null;
			}
		
		@Override
		protected void onPostExecute(String unused){
			super.onPostExecute(unused);
		}
		
	}
	
	class UpdateJobFinished extends AsyncTask<String, String, String>{

		@Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
		
		@Override
		protected String doInBackground(String... arg0) {
			try{
				//String now = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
				String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
				List<NameValuePair> jobParams = new ArrayList<NameValuePair>();
				jobParams.add(new BasicNameValuePair("job_id", jobId));
				jobParams.add(new BasicNameValuePair("key", "9c36c7108a73324100bc9305f581979071d45ee9"));
				jobParams.add(new BasicNameValuePair("status", "Complete"));
				jobParams.add(new BasicNameValuePair("completed_date", now));
				JSONObject jsonJobsResult = jsonParser.makeHttpRequest(updateJobUrl + jobId + ".json", "POST", jobParams);
				jobsDB.open();
				jobsDB.updateJobRecord("SYNCED", "Yes", jobId);
				
			}
			catch(Exception e){
				e.printStackTrace();
			}
			finally {
				jobsDB.close();
			}
			
			try {
				List<NameValuePair> driverParams = new ArrayList<NameValuePair>();
				driverParams.add(new BasicNameValuePair("id", driverId));
				driverParams.add(new BasicNameValuePair("key", "9c36c7108a73324100bc9305f581979071d45ee9"));
				driverParams.add(new BasicNameValuePair("available", "Available"));
				JSONObject jsonDriversResult = jsonParser.makeHttpRequest(updateDriverUrl + driverId + ".json", "POST", driverParams);
				usersDB.open();
				usersDB.updateUserRecord("SYNCED", "Yes", driverId);
			}
			catch(Exception e){
				e.printStackTrace();
			}
			finally {
				usersDB.close();
			}

			return null;
		}
		
		@Override
		protected void onPostExecute(String unused){
			super.onPostExecute(unused);
			Intent startDayIntent = new Intent(getApplicationContext(), StartDayActivity.class);
			startActivity(startDayIntent);
			finish();
		}

	}
	

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                * Thrown if Google Play services cancelled the original
                * PendingIntent
                */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
Log.i("error", "236");
}		
	}



	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        //if (mUpdatesRequested) {
    		mLocationClient.requestLocationUpdates(mLocationRequest, mPendingIntent);
        //}
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onLocationChanged(Location location) {
		float distanceInMeters = 0;
		Toast.makeText(getApplicationContext(), "Location Changed", Toast.LENGTH_SHORT).show();
			/*(Location prevLocation = new Location("");
			prevLocation.setLatitude(previousLat);
			prevLocation.setLongitude(previousLng);
			distanceInMeters = location.distanceTo(prevLocation);
			//44 meters per 20 seconds = 5mph. 
			if(distanceInMeters>20){ 			// as the crow flies distance
				driverLocationsDB.open();
				SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String dateFormatted = s.format(new Date());
				driverLocationsDB.createDriverLocation(driverId, dateFormatted, location.getLatitude(), location.getLongitude(), "No");
				//driverLocationsDB.close();
				try {
					new AddDriverLocation().execute();
				}
				catch(Exception e){
					e.printStackTrace();
				}
				finally {
					driverLocationsDB.close();
				}
			}
		
		// Log.i("Location changed", "Location changed");
		 previousLat = location.getLatitude();
		 previousLng = location.getLongitude();
		// Log.i("provider: ", location.getProvider());
		// Log.i("lat", "lat " + location.getLatitude());
		// Log.i("lng", "lng " + location.getLongitude());
		*/
		
		// driverLocationsDB.open();
		/*Cursor nonSync = driverLocationsDB.getDriverLocationsNotSynced();
		for (int i = 0; i<nonSync.getCount(); i++){
			Log.i("nonSync", "Record: " + i);
			nonSync.moveToPosition(i);
			Log.i("nonSync", "id: " + nonSync.getString(0));
			Log.i("nonSync", "driverId: " + nonSync.getString(1));
			Log.i("nonSync", "timestamp: " + nonSync.getString(2));
			Log.i("nonSync", "lat: " + nonSync.getString(3));
			Log.i("nonSync", "lng: " + nonSync.getString(4));

		}
		driverLocationsDB.close();
		driverLocationsDB.open();
		Cursor driverLocationsNonSynced = driverLocationsDB.getDriverLocationsNotSynced();
		Log.i("Opening", "opening");
		for(int i=0; i<driverLocationsNonSynced.getCount();i++){
			driverLocationsNonSynced.moveToPosition(i);
			Log.i("DriverLocationsNonSynced", driverLocationsNonSynced.getString(0) + " | " + driverLocationsNonSynced.getString(1) + " | " + driverLocationsNonSynced.getString(2) + " | " + driverLocationsNonSynced.getString(3)); 
			Log.i("DriverLocationsNonSynced", driverLocationsNonSynced.getString(4) + " | " + driverLocationsNonSynced.getString(5));
		}
		driverLocationsDB.close();
		*/
		/*
				driverLocationsDB.open();
				Cursor allDriverLocations = driverLocationsDB.getAllDriverLocations();
				for(int i=0; i<allDriverLocations.getCount(); i++){
					allDriverLocations.moveToPosition(i);
					Log.i("allLocations", allDriverLocations.getString(0) + " | " + allDriverLocations.getString(5));
				}
				driverLocationsDB.close();
		*/
	}

	
	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		Log.i("disabled", provider);
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		Log.i("enabled", provider);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();

	}
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	

}


