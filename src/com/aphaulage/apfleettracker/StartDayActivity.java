package com.aphaulage.apfleettracker;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
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
	UsersDBAdapter userDB;
	Cursor c;
	JsonParser jsonParser = new JsonParser();
	private static final String driversUpdateUrl = "http://aphaulage.co.uk/apTracker/drivers/update.json";

	TextView userFirstName;
	TextView userLastName;
	Button startWorkingDay;
	
	String mEmail;
	String mAvailable;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_day);
		
		getActionBar().setTitle("Driver Details");
		
		userDB = new UsersDBAdapter(getApplicationContext());
		userDB.open();
		c = userDB.getAllUsers();
		c.moveToNext();
		userFirstName = (TextView)findViewById(R.id.user_first_name);
		userLastName = (TextView)findViewById(R.id.user_last_name);
		userFirstName.setText(c.getString(1) + " ");
		userLastName.setText(c.getString(2));
		
		startWorkingDay = (Button)findViewById(R.id.startDayButton);
		
		if(c.getString(6) == "Available"){
			startWorkingDay.setText("End Working Day");
		}
		
		userDB.close();

		startWorkingDay.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				userDB.open();
				c = userDB.getAllUsers();
				c.moveToNext();
				Log.i("c availablity - ", c.getString(6).toString());
				if(c.getString(6).equals("Available")){
					userDB.updateUserAvailability(c.getString(3), "Unavailable");
					Log.i("StartDayActivity", "Day ended");
				}
				else {
					Log.i("StartDayActivity", "Day started");
					userDB.updateUserAvailability(c.getString(3), "Available");
					Intent intent = new Intent(StartDayActivity.this, PendingJobsActivity.class);
					startActivity(intent);
				}

				mEmail = c.getString(3);
				mAvailable = c.getString(6);
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
			//Delete user from local db. Send back to login screen.
			userDB.open();
			//Cursor c = userDB.getAllUsers();
			//c.moveToNext();
			userDB.deleteUser("email", c.getString(3)); //Temporary hard coded.
			userDB.close();
			finish();
			Intent intent = new Intent(StartDayActivity.this, LoginActivity.class);
			startActivity(intent);
			break;
		default:
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	class UpdateAvailability extends AsyncTask<String, String, String>{

		@Override
		protected String doInBackground(String... args) {


				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("email", "stu@email.com"));
				params.add(new BasicNameValuePair("available", "mnbbbn"));
				Log.i("doInBackground", "Starting");
				Log.i("params: ", params.toString());
				JSONObject json = jsonParser.makeHttpRequest(driversUpdateUrl, "POST", params);
				Log.i("json", json.toString());
				finish();
			

			
			return null;
		}
		
	}
}
