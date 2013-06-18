/*
2013 Measuring Broadband America Program
Mobile Measurement Android Application
Copyright (C) 2012  SamKnows Ltd.

The FCC Measuring Broadband America (MBA) Program's Mobile Measurement Effort developed in cooperation with SamKnows Ltd. and diverse stakeholders employs an client-server based anonymized data collection approach to gather broadband performance data in an open and transparent manner with the highest commitment to protecting participants privacy.  All data collected is thoroughly analyzed and processed prior to public release to ensure that subscribers’ privacy interests are protected.

Data related to the radio characteristics of the handset, information about the handset type and operating system (OS) version, the GPS coordinates available from the handset at the time each test is run, the date and time of the observation, and the results of active test results are recorded on the handset in JSON(JavaScript Object Notation) nested data elements within flat files.  These JSON files are then transmitted to storage servers at periodic intervals after the completion of active test measurements.

This Android application source code is made available under the GNU GPL2 for testing purposes only and intended for participants in the SamKnows/FCC Measuring Broadband American program.  It is not intended for general release and this repository may be disabled at any time.


This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/


package com.samknows.measurement.activity;

import com.samknows.measurement.AppSettings;
import com.samknows.measurement.Constants;
import com.samknows.measurement.Logger;
import com.samknows.measurement.MainService;
import com.samknows.measurement.R;
import com.samknows.measurement.util.TimeUtils;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;

import android.widget.Toast;

public class SKPreferenceActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		//Hide preference user_self_id for beta version of the app
		if(!AppSettings.getInstance().user_self_id){
			Preference user_tag = findPreference("user_self_id");
			PreferenceCategory pc = (PreferenceCategory) findPreference("first_category");
			pc.removePreference(user_tag);
		}
		
		
		String versionName="";
		try {
			versionName = this.getPackageManager().getPackageInfo(this.getPackageName(), 0 ).versionName;
		} catch (NameNotFoundException e) {
			Logger.e(this, e.getMessage());
		}
		PreferenceCategory mCategory = (PreferenceCategory) findPreference("first_category");
		mCategory.setTitle(mCategory.getTitle()+" "+"("+versionName+")");

		updateLabels();
	
	}
	
	protected void updateLabels(){
		AppSettings app = AppSettings.getInstance();
		long configDataCap = app.getLong(Constants.PREF_DATA_CAP, -1l );
		String s_configDataCap = configDataCap == -1l ? "": configDataCap +"";
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SKPreferenceActivity.this);
		Preference p;
		
		String data_cap = preferences.getString(Constants.PREF_DATA_CAP, s_configDataCap);
		p = (Preference) findPreference(Constants.PREF_DATA_CAP);
		p.setTitle(getString(R.string.data_cap_title)+ " "+data_cap+getString(R.string.mb));	
		
		
		int data_cap_day = preferences.getInt(Constants.PREF_DATA_CAP_RESET_DAY, 1);
		p = (Preference) findPreference(Constants.PREF_DATA_CAP_RESET_DAY);
		p.setTitle(getString(R.string.data_cap_day_title)+ TimeUtils.getDayOfMonthSuffix(data_cap_day));
		
		
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		long testsScheduled = AppSettings.getInstance().getLong("number_of_tests_schedueld", -1);
		CheckBoxPreference mCheckBoxPref = (CheckBoxPreference) findPreference("enable_testing");
		mCheckBoxPref.setEnabled(testsScheduled > 0);
		
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).unregisterOnSharedPreferenceChangeListener(this);
	}
	
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(Constants.PREF_SERVICE_ENABLED)) {
			if (AppSettings.getInstance().isServiceEnabled()) {
				MainService.poke(SKPreferenceActivity.this);
			}				
		}
		if (key.equals(Constants.PREF_DATA_CAP)) {

		    String valueString = sharedPreferences.getString(key, "");
		    int value;
		    try{
		    	value = Integer.parseInt(valueString);
		    }catch(NumberFormatException nfe){
		    	value = 0;
		    }
		    
		    if(value > Constants.DATA_CAP_MAX_VALUE){
		  
		    	EditTextPreference p = (EditTextPreference) findPreference(key);
		    	p.setText(""+Constants.DATA_CAP_MAX_VALUE);
		    	Toast t = Toast.makeText(SKPreferenceActivity.this,getString(R.string.max_data_cap_message)+" "+Constants.DATA_CAP_MAX_VALUE, Toast.LENGTH_SHORT);
		    	t.show();
		    }else if(value < Constants.DATA_CAP_MIN_VALUE){
		    	EditTextPreference p = (EditTextPreference) findPreference(key);
		    	p.setText(""+Constants.DATA_CAP_MIN_VALUE);
		    	Toast t = Toast.makeText(SKPreferenceActivity.this,getString(R.string.min_data_cap_message)+" "+Constants.DATA_CAP_MIN_VALUE, Toast.LENGTH_SHORT);
		    	t.show();
		    }
		}
		updateLabels();
	}
	
}
