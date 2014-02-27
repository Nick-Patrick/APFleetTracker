package com.aphaulage.apfleettracker;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class JobDetailsMapActivity extends Activity {

	LocationsDBAdapter locationsDB;
	private GoogleMap mMap;
	public String job_id;
	public String collection_id;
	public String dropoff_id;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_job_details_map);
		locationsDB = new LocationsDBAdapter(getApplicationContext());
		Intent i = getIntent();
		job_id = i.getStringExtra("job_id");
		collection_id = i.getStringExtra("collection_id");
		dropoff_id = i.getStringExtra("dropoff_id");
		setUpMap();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.collection_details_map, menu);
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
			finish();
			return true;
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
	    }
	}

}
