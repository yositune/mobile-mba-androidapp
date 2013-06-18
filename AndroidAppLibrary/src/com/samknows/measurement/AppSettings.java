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


package com.samknows.measurement;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.samknows.measurement.schedule.ScheduleConfig;
import com.samknows.measurement.schedule.ScheduleConfig.LocationType;
import com.samknows.measurement.statemachine.State;
import com.samknows.measurement.util.OtherUtils;
import com.samknows.measurement.util.TimeUtils;

public class AppSettings {
	private static final String TAG = AppSettings.class.getName();
	private Context ctx;
	
	//json fields to be included to each submission
	public static final String JSON_UNIT_ID 				= "unit_id";
	public static final String JSON_APP_VERSION_CODE 		= "app_version_code";
	public static final String JSON_APP_VERSION_NAME 		= "app_version_name";
	public static final String JSON_SCHEDULE_CONFIG_VERSION = "schedule_config_version";
	public static final String JSON_TIMEZONE 				= "timezone";
	public static final String JSON_TIMESTAMP 				= "timestamp";
	public static final String JSON_DATETIME 				= "datetime";
	public static final String JSON_ENTERPRISE_ID 			= "enterprise_id";
	public static final String JSON_SIMOPERATORCODE 		= "sim_operator_code";
	public static final String JSON_USER_SELF_ID 			= "user_self_id";
	
	//used for first step to obtain dcs base url
	public String dCSInitUrl;
	
	public String reportingServerPath;
	public long rescheduleTime;
	
	//used to restart service after being killed by manager
	public long rescheduleServiceTime;
	public long testStartWindow;
	public long testStartWindowWakeup;

	//used to deploy different brand of the application
	public String brand;
	
	//Used to know if the app needs to collect identifiers 
	public boolean anonymous;
	
	//protocol scheme used for comunicating with the dcs
	public String protocol_scheme;
	
	//submit path used to send the results to the dcs
	public String submit_path;
	
	//true if the config file has to be downloaded and the schedule updated 
	public boolean force_download = false;
	
	//download config file path
	public String download_config_path;

	//application version code and application version name read from the androidmanifest.xml file
	public String app_version_code;
	public String app_version_name;
	
	//Enterprise id read from the properties file
	public String enterprise_id;
	
	//User self identifier preference enabled
	public boolean user_self_id;
	
	//static instance and initialisation
	private static AppSettings instance;
	
	public static AppSettings getInstance() {
		return instance;
	}
	
	
	//to be called when the app starts
	public static void create(Context c) {
		instance = new AppSettings(c);
	}
	
	//Initialise the AppSettings reading from the properties file located in res/raw
	private AppSettings(Context c) {
		ctx = c;
		InputStream is = c.getResources().openRawResource(R.raw.properties);
		Properties p = new Properties();
		try {
			p.load(is);
			dCSInitUrl 				= p.getProperty(Constants.PROP_DCS_URL);
			reportingServerPath 	= p.getProperty(Constants.PROP_REPORTING_PATH);
			rescheduleTime 			= Long.valueOf(p.getProperty(Constants.PROP_RESCHEDULE_TIME));
			testStartWindow 		= Long.valueOf(p.getProperty(Constants.PROP_TEST_START_WINDOW_RTC));
			testStartWindowWakeup 	= Long.valueOf(p.getProperty(Constants.PROP_TEST_START_WINDOW_RTC_WAKEUP));
			rescheduleServiceTime 	= Long.valueOf(p.getProperty(Constants.PROP_KILLED_SERVICE_RESTART_TIME_IN_MILLIS));
			brand 					= p.getProperty(Constants.PROP_BRAND);
			anonymous 				= Boolean.valueOf(p.getProperty(Constants.PROP_ANONYMOUS));
			protocol_scheme 		= p.getProperty(Constants.PROP_PROTOCOL_SCHEME);
			submit_path 			= p.getProperty(Constants.PROP_SUBMIT_PATH);
			download_config_path 	= p.getProperty(Constants.PROP_DOWNLOAD_CONFIG_PATH);
			PackageInfo pInfo 		= c.getPackageManager().getPackageInfo(c.getPackageName(),0);
			app_version_code 		= pInfo.versionCode+"";
			app_version_name 		= pInfo.versionName;
			enterprise_id 			= p.getProperty(Constants.PROP_ENTERPRISE_ID);
			user_self_id 			= Boolean.parseBoolean(p.getProperty(Constants.PROP_USER_SELF_IDENTIFIER));
			
		} catch (IOException e) {
			Logger.e(TAG, "failed to load properies!");
		} catch (NameNotFoundException nnfe) {
			Logger.e(TAG, "failed to read manifest file: "+ nnfe.getMessage());
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
	
	//Methods for managing the testStartWindow
	public long getTestStartWindow(){
		return isWakeUpEnabled() ? testStartWindowWakeup : testStartWindow; 
	}
	
	//methods for managing the shared preferences
	
	public void saveString(String key, String value) {
		Editor editor = ctx.getSharedPreferences(Constants.PREF_FILE_NAME, Context.MODE_PRIVATE).edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	public String getString(String key) {
		return ctx.getSharedPreferences(Constants.PREF_FILE_NAME, Context.MODE_PRIVATE).getString(key, null);
	}
	
	public String getString(String key, String default_value) {
		return ctx.getSharedPreferences(Constants.PREF_FILE_NAME, Context.MODE_PRIVATE).getString(key, default_value);
	}
	
	public void saveBoolean(String key, boolean value) {
		Editor editor = ctx.getSharedPreferences(Constants.PREF_FILE_NAME, Context.MODE_PRIVATE).edit();
		editor.putBoolean(key, value);
		editor.commit();
	}
	
	public boolean getBoolean(String key, boolean def) {
		return ctx.getSharedPreferences(Constants.PREF_FILE_NAME, Context.MODE_PRIVATE).getBoolean(key, def);
	}
	
	public void saveLong(String key, long value) {
		Editor editor = ctx.getSharedPreferences(Constants.PREF_FILE_NAME, Context.MODE_PRIVATE).edit();
		editor.putLong(key, value);
		editor.commit();
	}
	
	public long getLong(String key, long defValue) {
		return ctx.getSharedPreferences(Constants.PREF_FILE_NAME, Context.MODE_PRIVATE).getLong(key, defValue);
	}
	
	public String getUnitId() {
		return getString(Constants.PREF_KEY_UNIT_ID);
	}
	
	public void saveUnitId(String unitId) {
		saveString(Constants.PREF_KEY_UNIT_ID, unitId);
	}
	
	public String getServerBaseUrl() {
		return getString(Constants.PREF_KEY_SERVER_BASE_URL);
	}
	
	public void saveServerBaseUrl(String url) {
		saveString(Constants.PREF_KEY_SERVER_BASE_URL, url);
	}
	
	public State getState() {
		State ret = State.NONE;
		String state = getString(Constants.PREF_KEY_STATE);
		if (state != null) {
			for(State s:State.values()){
				if(state.equalsIgnoreCase(String.valueOf(s))){
					ret = s;
					break;
				}
			}
		}
		return ret;
	}
	
	public boolean isServiceActivated() {
		return getBoolean(Constants.PREF_KEY_SERVICE_ACTIVATED, false);
	}
	
	public void setServiceActivated(boolean activated) {
		saveBoolean(Constants.PREF_KEY_SERVICE_ACTIVATED, activated);
	}
	
	public void saveState(State state) {
		saveString(Constants.PREF_KEY_STATE, String.valueOf(state));
	}

	public boolean isServiceEnabled() {
		return PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean(Constants.PREF_SERVICE_ENABLED, true);
	}
	
	public void setServiceEnabled(boolean enabled) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
		editor.putBoolean(Constants.PREF_SERVICE_ENABLED, enabled);
		editor.commit();
	}

	//if the state machine fails and the user want to run some test we ask for the activation
	public void stateMachineFailure(){
		saveBoolean(Constants.STATE_MACHINE_STATUS, false);
		
	}
	
	public void stateMachineSuccess(){
		saveBoolean(Constants.STATE_MACHINE_STATUS, true);
		
	}
	
	public boolean stateMachineStatus(){
		return getBoolean(Constants.STATE_MACHINE_STATUS, true);
	}
	
	
	public void appendUsedBytes(long bytes) {
		if(OtherUtils.isWifi(ctx)){
			return;
		}
		resetDataUsageIfTime();
		long newBytes = getUsedBytes() + bytes;
		saveLong(Constants.PREF_KEY_USED_BYTES,newBytes);
		long newTime = System.currentTimeMillis();
		saveLong(Constants.PREF_KEY_USED_BYTES_LAST_TIME, newTime);
		Logger.d(TAG, "saved used bytes " + newBytes + " at time "+ newTime);
	}
	
	public long getUsedBytes() {
		return getLong(Constants.PREF_KEY_USED_BYTES, 0);
	}
	
	public void saveDataCapFromConfig(long bytes){
		saveLong(Constants.PREF_DATA_CAP, bytes);
	}
	/**
	 * data cap in bytes
	 * if preference has been defined use it
	 * otherwise use the datacap from config file
	 * if none of them is defined use Long.MAX_VALUE
	 * 	  
	 */
	public long getDataCap() {
		long ret = Long.MAX_VALUE;
		long configDataCap = getLong(Constants.PREF_DATA_CAP,-1);
		
		long preferenceDataCap = Long.valueOf(PreferenceManager.getDefaultSharedPreferences(ctx).getString(Constants.PREF_DATA_CAP, "-1")); //in megs
		if(preferenceDataCap>0){
			ret = preferenceDataCap * 1024 * 1024;
		}else if(configDataCap > 0){
			ret = configDataCap * 1024 * 1024;
		}
		return ret;
	}
	

	public void resetDataUsage(){
		saveLong(Constants.PREF_KEY_USED_BYTES,0);
	}
	
	private void resetDataUsageIfTime(){
		Calendar c = GregorianCalendar.getInstance();
		long startToday = TimeUtils.getStartDayTime();
		long startDayLastTime =TimeUtils.getStartDayTime(getLong(Constants.PREF_KEY_USED_BYTES_LAST_TIME, System.currentTimeMillis()));
		int currDayReset = PreferenceManager.getDefaultSharedPreferences(ctx).getInt(Constants.PREF_KEY_DATA_CAP_DAY_IN_MONTH_RESET, 1);
		long timeReset = TimeUtils.getPreviousDayInMonth(currDayReset);
		if( startDayLastTime < timeReset && startToday >= timeReset){
			Logger.d(this, "Data usage has been reset to 0 for the month useage. Reset time: "+timeReset+" last time: "+ startDayLastTime+" now: "+startToday);
			resetDataUsage();
		}
	}
	
	public boolean isDataCapReached(long bytesToBeUsed){
		if(OtherUtils.isWifi(ctx)){	
			return false;
		}
		resetDataUsageIfTime();
		long usedBytes = getUsedBytes();
		long dataCap = getDataCap();
		return usedBytes + bytesToBeUsed >= dataCap;
		
	}
	
	public boolean isDataCapReached() {
		return isDataCapReached(0);
	}
	
	public boolean isWakeUpEnabled() {
		return PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean(Constants.PREF_ENABLE_WAKEUP, true);
	}
	
	public void setWakeUpEnabledIfNull(boolean enabled) {
		if (!PreferenceManager.getDefaultSharedPreferences(ctx).contains(Constants.PREF_ENABLE_WAKEUP)) {
			PreferenceManager.getDefaultSharedPreferences(ctx).edit().putBoolean(Constants.PREF_ENABLE_WAKEUP, enabled).commit();
		}
	}
	
	public LocationType getLocationServiceType() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		if (prefs.contains(Constants.PREF_LOCATION_TYPE)) {
			String pref = prefs.getString(Constants.PREF_LOCATION_TYPE, null);
			if(pref == null){
				return null;
			}if(pref == ctx.getString(R.string.GPS)){
				return LocationType.gps;
			}return LocationType.network;
		}
		return LocationType.gps;
	}
	
	public void setLocationTypeIfNull(LocationType type) {
		if (!PreferenceManager.getDefaultSharedPreferences(ctx).contains(Constants.PREF_LOCATION_TYPE)) {
			String value = type == LocationType.gps ? ctx.getString(R.string.GPS) : ctx.getString(R.string.MobileNetwork);
			PreferenceManager.getDefaultSharedPreferences(ctx).edit().putString(Constants.PREF_LOCATION_TYPE, value).commit();
		}
	}
	
	public void saveNextRunTime(long time) {
		Editor editor = ctx.getSharedPreferences(Constants.PREF_FILE_NAME, Context.MODE_PRIVATE).edit();
		editor.putLong(Constants.PREF_NEXT_RUN_TIME, time);
		editor.commit();
	}
	
	public long getNextRunTime() {
		return getLong(Constants.PREF_NEXT_RUN_TIME, Constants.NO_NEXT_RUN_TIME);
	}
	
	public String getConfigVersion() {
		return getString(Constants.PREF_KEY_CONFIG_VERSION);
	}
	
	public void saveConfigVersion(String v) {
		saveString(Constants.PREF_KEY_CONFIG_VERSION, v);
	}
	
	public String getConfigPath() {
		return getString(Constants.PREF_KEY_CONFIG_PATH);
	}
	
	public void setForceDownload(){
		force_download = true;
	}
	
	public boolean forceDownload(){
		boolean ret = force_download;
		force_download = false;
		return ret;
	}
	
	public boolean updateConfig(ScheduleConfig newConfig){
		boolean ret = false;
		ScheduleConfig savedConfig = CachingStorage.getInstance().loadScheduleConfig();
		if(savedConfig == null){
			Logger.d(this, "Saved Config is null");
			ret = true;
		}else if(savedConfig.toUpdate(newConfig)){
			Logger.d(this, "Config versions don't match");
			ret = true;
		}else if(forceDownload()){
			Logger.d(this, "Force update config");
			ret = true;
		}
		return ret;
	}
	

	public void saveConfigPath(String path) {
		saveString(Constants.PREF_KEY_CONFIG_PATH, path);
	}
	
	public boolean wasIntroShown() {
		return getBoolean(Constants.PREF_WAS_INTRO_SHOWN, false);
	}
	
	public void saveIntroShown(boolean wasShown) {
		saveBoolean(Constants.PREF_FILE_NAME, wasShown);
	}
	
	public String getUsername() {
		return getString(Constants.PREF_KEY_USERNAME);
	}
	
	public String getPassword() {
		return getString(Constants.PREF_KEY_PASSWORD);
	}
	
	public List<DeviceDescription> getDevices() {
		String devices = getString(Constants.PREF_KEY_DEVICES); 
		return DeviceDescription.parce(devices);
	}
	
	public void saveUsername(String username) {
		saveString(Constants.PREF_KEY_USERNAME, username);
	}
	
	public void savePassword(String password) {
		saveString(Constants.PREF_KEY_PASSWORD, password);
	}
	
	public void saveDevices(String device) {
		saveString(Constants.PREF_KEY_DEVICES, device);
	}
	
	public void clearAll() {
		Editor editor = ctx.getSharedPreferences(Constants.PREF_FILE_NAME, Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();
	}
	
	//Returns a Map containing all the json entry to be added when submitting the results
	public Map<String,String> getJSONExtra(){
		Map<String, String> ret= new HashMap<String,String>();
		if(!anonymous){
			ret.put(JSON_UNIT_ID, getUnitId());
		}
		ret.put(JSON_APP_VERSION_NAME, app_version_name);
		ret.put(JSON_APP_VERSION_CODE, app_version_code);
		ScheduleConfig config = CachingStorage.getInstance().loadScheduleConfig();
		if( config !=null){
			ret.put(JSON_SCHEDULE_CONFIG_VERSION, config.version );
		}else{
			ret.put(JSON_SCHEDULE_CONFIG_VERSION, "no_schedule_config" );
		}
		long time = System.currentTimeMillis();
		ret.put(JSON_TIMESTAMP, (time/1000)+"");
		ret.put(JSON_DATETIME, new java.util.Date(time).toString());
		ret.put(JSON_TIMEZONE, TimeUtils.millisToHours(TimeZone.getDefault().getRawOffset())+"");
		if(enterprise_id != null){
			ret.put(JSON_ENTERPRISE_ID, enterprise_id);
		}
		String user_self_id = PreferenceManager.getDefaultSharedPreferences(ctx).getString(Constants.PREF_USER_SELF_ID, null);
		if(user_self_id != null ){
			ret.put(JSON_USER_SELF_ID, user_self_id);
		}
		TelephonyManager manager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
		String simOperatorCode = "";
		if(manager != null){
			simOperatorCode = manager.getSimOperator();
		}
		ret.put(JSON_SIMOPERATORCODE, simOperatorCode);
		
		return ret;
	}
	
	
	public String getResourceString(int id){
		return ctx.getString(id);
	}
}