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

import java.util.Arrays;
import java.util.List;

import com.samknows.measurement.AppSettings;
import com.samknows.measurement.CachingStorage;
import com.samknows.measurement.Constants;
import com.samknows.measurement.Logger;
import com.samknows.measurement.MainService;
import com.samknows.measurement.R;
import com.samknows.measurement.Storage;
import com.samknows.measurement.activity.components.StatModel;
import com.samknows.measurement.schedule.ScheduleConfig;
import com.samknows.measurement.schedule.TestDescription;
import com.samknows.measurement.util.OtherUtils;
import com.samknows.measurement.util.TimeUtils;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;

import android.widget.Toast;

public class SKPreferenceActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener{

    protected void setListPreferenceData(ListPreference lp) {
    	Storage storage;
    	ScheduleConfig config;

    	List<TestDescription> testList;
    	String array_spinner[];
    	String array_spinner_int[];
    	int defaultIndex = -1;
    	
		storage = CachingStorage.getInstance();
		config = storage.loadScheduleConfig();
		// if config == null the app is not been activate and
		// no test can be run
		if (config == null) {
			// TODO Add an alert that the app has not been init yet
			config = new ScheduleConfig();
		}
		testList = config.manual_tests;
		array_spinner = new String[testList.size() + 1];
		array_spinner_int = new String[testList.size() + 1];

		for (int i = 0; i < testList.size(); i++) {
			TestDescription td = testList.get(i);
			array_spinner[i] = td.displayName;
			array_spinner_int[i] = td.testId + "";
			if (Integer.parseInt(array_spinner_int[i]) == StatModel.JITTER_TEST) {
				defaultIndex = i;
			}
		}
		array_spinner[testList.size()] = getString(R.string.all);
		array_spinner_int[testList.size()] = "-1";
		
        lp.setEntries(array_spinner);
        lp.setEntryValues(array_spinner_int);
        if (lp.getValue() == null && defaultIndex >= 0) { // only if not selected
        	lp.setValueIndex(defaultIndex);
        }
}
	
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
		
		ListPreference listPreference = (ListPreference) findPreference("continuous_test_id");
		setListPreferenceData(listPreference);
		
		String versionName="";
		try {
			versionName = this.getPackageManager().getPackageInfo(this.getPackageName(), 0 ).versionName;
		} catch (NameNotFoundException e) {
			Logger.e(this, e.getMessage());
		}
		PreferenceCategory mCategory = (PreferenceCategory) findPreference("first_category");
		mCategory.setTitle(mCategory.getTitle()+" "+"("+versionName+")");

		long testsScheduled = AppSettings.getInstance().getLong("number_of_tests_schedueld", -1);
		CheckBoxPreference mCheckBoxPref = (CheckBoxPreference) findPreference(Constants.PREF_SERVICE_ENABLED);
		if(testsScheduled <= 0){
			mCheckBoxPref.setChecked(false);
			mCheckBoxPref.setEnabled(false);
		}else{
			mCheckBoxPref.setChecked(AppSettings.getInstance().isServiceEnabled());
		}
		
		updateLabels();
	
	}
	
	protected void updateLabels(){
		AppSettings app = AppSettings.getInstance();
		long configDataCap = app.getLong(Constants.PREF_DATA_CAP, -1l );
		String s_configDataCap = configDataCap == -1l ? "": configDataCap + "";
		
		long configContinuousInterval = app.getLong(Constants.PREF_CONTINUOUS_INTERVAL, -1l );
		String s_continuousInterval = configContinuousInterval == -1l ? "" : configContinuousInterval + "";
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SKPreferenceActivity.this);
		Preference p;
		
		String data_cap = preferences.getString(Constants.PREF_DATA_CAP, s_configDataCap);
		p = (Preference) findPreference(Constants.PREF_DATA_CAP);
		p.setTitle(getString(R.string.data_cap_title)+ " "+data_cap+getString(R.string.mb));	
		
		String continuous_interval = preferences.getString(Constants.PREF_CONTINUOUS_INTERVAL, s_continuousInterval);
		p = (Preference) findPreference(Constants.PREF_CONTINUOUS_INTERVAL);
		p.setTitle(getString(R.string.continuous_interval_pref)+ ": "+continuous_interval+getString(R.string.sec));
		
		ListPreference lp = (ListPreference) findPreference(Constants.PREF_CONTINUOUS_ID);
		CharSequence continuous_test_name = lp.getEntry();
		lp.setTitle(getString(R.string.continuous_test_id_title)+ ": "+continuous_test_name);
		
		int data_cap_day = preferences.getInt(Constants.PREF_DATA_CAP_RESET_DAY, 1);
		p = (Preference) findPreference(Constants.PREF_DATA_CAP_RESET_DAY);
		p.setTitle(getString(R.string.data_cap_day_title)+" "+ TimeUtils.getDayOfMonthSuffix(data_cap_day));
		
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
			}else{
				OtherUtils.cancelAlarm(this);
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
