package com.aphaulage.apfleettracker;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class ActiveJobMapActivity extends Activity  {
	LocationsDBAdapter locationsDB;
	DriverLocationsDBAdapter driverLocationsDB;
	JobsDBAdapter jobsDB;
	DBAdapter dbAdapter;
	private GoogleMap mMap;
	public String job_id;
	public String collection_id;
	public String dropoff_id;
	
	Timer timer;
	RefreshDriverLocationTask refreshDriverLocation;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_job_details_map);
		locationsDB = new LocationsDBAdapter(getApplicationContext());
		driverLocationsDB = new DriverLocationsDBAdapter(getApplicationContext());
		Intent i = getIntent();
		job_id = i.getStringExtra("job_id");
		dbAdapter = new DBAdapter(getApplicationContext());
		jobsDB = new JobsDBAdapter(getApplicationContext());
		jobsDB.open();
		Cursor jobsCursor = jobsDB.getJob(job_id);
		jobsDB.close();
		collection_id = jobsCursor.getString(2);
		dropoff_id = jobsCursor.getString(3);
		setUpMap();
		
		timer = new Timer();
		refreshDriverLocation = new RefreshDriverLocationTask();
		timer.scheduleAtFixedRate(refreshDriverLocation, 0, 60000); //(delay,period)

	}
	
	@Override
	protected void onStop(){
		super.onStop();
		timer.cancel();
	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.active_job_map, menu);
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
	
	private void setUpMap() {
	    // Do a null check to confirm that we have not already instantiated the map.
	    if (mMap == null) {
	    	locationsDB.open();
	    	Cursor dropoffCursor = locationsDB.getLocation(dropoff_id);
	    	Cursor collectionCursor = locationsDB.getLocation(collection_id);
	    	float dropoffLng = Float.parseFloat(dropoffCursor.getString(12));
	    	float dropoffLat = Float.parseFloat(dropoffCursor.getString(13));
	    	float collectionLng = Float.parseFloat(collectionCursor.getString(12));
	    	float collectionLat = Float.parseFloat(collectionCursor.getString(13));
	        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.collection_details_map)).getMap();
	        // Check if we were successful in obtaining the map.
	        if (mMap != null) {
	        	try{
	        	Log.i("lat", "lat: " + dropoffLat);
	        	Log.i("lng", "lng: " + dropoffLng);
	        	mMap.addMarker(new MarkerOptions()
	            .position(new LatLng(dropoffLat,dropoffLng))
	            .title("Dropoff: " + dropoffCursor.getString(1))
	            .snippet(dropoffCursor.getString(5) + ", " + dropoffCursor.getString(7))
	        	);
	        	
	        	mMap.addMarker(new MarkerOptions()
	            .position(new LatLng(collectionLat,collectionLng))
	            .title("Collection: " + collectionCursor.getString(1))
	            .snippet(collectionCursor.getString(5) + ", " + collectionCursor.getString(7))
	        	);
	        	}
	        	catch (Exception e){
	        		e.printStackTrace();
	        	}
	            
	        }
	    }
	}
	
	
	private class RefreshDriverLocationTask extends TimerTask {
	    @Override
	    public void run() {
	        runOnUiThread(new Runnable() {
	            @Override
	            public void run() {
	            	try{
		            	Log.i("Timer", "timer running");
		            	driverLocationsDB.open();
		            	Cursor driverLocation = driverLocationsDB.getAllDriverLocations();
		            	if(driverLocation.getCount() > 0){
			            	driverLocation.moveToLast();
			            	driverLocationsDB.close();
			            	float driverLat = Float.parseFloat(driverLocation.getString(2));
			            	float driverLng = Float.parseFloat(driverLocation.getString(3));
			            	Log.i("running", "lat: " + driverLat);
			            	Log.i("running", "lng: " + driverLng);
			            	mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(driverLat, driverLng), 9));
		
			            	mMap.addMarker(new MarkerOptions()
				            .position(new LatLng(driverLat,driverLng))
				            .title("You are here!")
				        	);
		            	}
	            	}
	            	catch(Exception e){
	            		e.printStackTrace();
	            	}
	            }
	        });
	    }
	}	

}


