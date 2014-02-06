package com.aphaulage.apfleettracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;

public class SplashActivity extends Activity {

	//Time to show splash screen
	private static int SPASH_SCREEN_TIME = 1000;
	
	private Boolean alreadyLoggedIn;
	
	private DBAdapter db;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_splash);
		
		//Startup Database and Create Tables.
		db = new DBAdapter(this);
		db.open();
		db.close();
		
		// Open 'users' db table.
		UsersDBAdapter userDB = new UsersDBAdapter(getApplicationContext());
		userDB.open();
		
		//Check if any user records already exist.
		if(userDB.getAllUsers().getCount() > 0){
			//User already exists so log straight in.
			alreadyLoggedIn = true;
		}
		else {
			//First time user.
			alreadyLoggedIn = false;
		}
		
		new Handler().postDelayed(new Runnable(){
			
			@Override
			public void run(){
				//Start login activity if user hasn't logged in before.
				if(alreadyLoggedIn == false){
					Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
					startActivity(intent);
				}
				//Skip login activity if user HAS logged in before.
				else {
					Intent intent = new Intent(SplashActivity.this, StartDayActivity.class);
					startActivity(intent);
				}
				finish();
			}
			
		}, SPASH_SCREEN_TIME);
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.splash, menu);
		return true;
	}

}
