package com.samknows.measurement.activity;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.widget.TextView;

import com.samknows.measurement.R;
import com.samknows.measurement.activity.components.Util;

public class SamKnowsAbout extends BaseLogoutActivity {

	
	@Override
	public void onStart(){
		super.onStart();
		//getActionBar().setDisplayHomeAsUpEnabled(true);
		this.setTitle(getString(R.string.about));
		setContentView(R.layout.about);
		String versionName="";
		try {
			versionName = this.getPackageManager().getPackageInfo(this.getPackageName(), 0 ).versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		TextView tv=(TextView) findViewById(R.id.version);
		tv.setText(getString(R.string.version)+versionName);
		
		Util.initializeFonts(this);
		Util.overrideFonts(this, findViewById(android.R.id.content));
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
			NavUtils.navigateUpTo(this,
					new Intent(this, SamKnowsAggregateStatViewerActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
