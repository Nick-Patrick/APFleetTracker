package com.aphaulage.apfleettracker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

import com.google.android.gms.location.LocationClient;

public class LocationTrackingService extends IntentService {

    private static final String TAG = LocationTrackingService.class.getSimpleName();
    
    protected String driverId;
    
    DBAdapter dbAdapter;
	UsersDBAdapter usersDB;
	DriverLocationsDBAdapter driverLocationsDB;
	
	JsonParser jsonParser = new JsonParser();
	private static final String addDriverLocationUrl = "http://aphaulage.co.uk/apTracker/driverLocations/addDriverLocation.json";


   
    public LocationTrackingService() {
        super("Fused Location");
        Log.i(TAG, "Location Tracking Service");
    }

    public LocationTrackingService(String name) {
    	super("Fused Location");
        Log.i(TAG, "Location Tracking Service: " + name);

    }
    
    @Override   
    public void onCreate() {
    	super.onCreate();
    	usersDB = new UsersDBAdapter(getApplicationContext());
		dbAdapter = new DBAdapter(getApplicationContext());
		driverLocationsDB = new DriverLocationsDBAdapter(getApplicationContext());
		usersDB.open();
		Cursor driverCursor = usersDB.getAllUsers();
		driverCursor.moveToFirst();
		driverId = driverCursor.getString(8);
		usersDB.close();
		
		
    }
    
    @Override
    protected void onHandleIntent(Intent intent){
    	Location location = intent.getParcelableExtra(LocationClient.KEY_LOCATION_CHANGED);
    	if(location != null){
    		Log.i(TAG, "onHandleIntent" + location.getLatitude() + " | " + location.getLongitude());
    		
    		NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    		Builder noti = new NotificationCompat.Builder(this);
    		noti.setContentTitle("AP Tracking");
    		SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String dateFormatted = s.format(new Date());
    		noti.setContentText("Last Tracked at " + dateFormatted);
    		noti.setSmallIcon(R.drawable.ic_launcher);
    		
    		driverLocationsDB.open();
			driverLocationsDB.createDriverLocation(driverId, dateFormatted, location.getLatitude(), location.getLongitude(), "No");
			try {
				new AddDriverLocation().execute();
			}
			catch(Exception e){
				e.printStackTrace();
			}
			finally {
				driverLocationsDB.close();
			}
			
    		notificationManager.notify(1234, noti.build());
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
    
    private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
    
}