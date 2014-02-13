package com.aphaulage.apfleettracker;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class EndDayActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_end_day);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.end_day, menu);
		return true;
	}

}
