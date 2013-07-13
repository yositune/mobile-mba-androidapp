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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.samknows.measurement.ServiceDataCache.CachedValue;
import com.samknows.measurement.activity.TestResultsTabActivity;
import com.samknows.measurement.net.SamKnowsClient;
import com.samknows.measurement.test.outparcer.ParcerDataType;


/*
 * Service that does all the remoting bits with the server of SamKnows
 */

public class SamKnowsLoginService {
	private static final String TAG = SamKnowsLoginService.class.getSimpleName();
	
	private SamKnowsClient client;
	private AppSettings appSettings = AppSettings.getInstance();
	private boolean FORCE = false;
	
	public static final ServiceDataCache cache = new ServiceDataCache();
	public static final int RECENT = 0;
	public static final int WEEK = 1;
	public static final int MONTH = 2;
	public static final int THREE_MONTHS = 3;
	public static final int SIX_MONTHS = 4;
	public static final int YEAR = 5;
	
//	public class SamKnowsBinder extends Binder {
//		public SamKnowsLoginService getService(){
//			return SamKnowsLoginService.this;
//		}
//	}
//	private final IBinder mBinder = new SamKnowsBinder();
//	@Override
//	public IBinder onBind(Intent intent){
//		return mBinder;
//	}
	
	public void createClient(String username, String password, String device){
		client = new SamKnowsClient(appSettings.getUsername(), 
				appSettings.getPassword(),
				device);
	}	
	

	private JsonHttpResponseHandler getHandler(final SamKnowsResponseHandler handler){
		return new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(JSONObject devices){
				handler.onSuccess(devices);
			}
			@Override
			public void onFailure(Throwable error){
				handler.onFailure(error);
			}
		};		
	}
	
	private AsyncHttpResponseHandler getHandler(final SamKnowsResponseHandler handler, final int TYPE){
		return new AsyncHttpResponseHandler(){
			@Override
			public void onSuccess(String responce){
				try{
					JSONObject jsonResponse = new JSONObject(responce);
					Date cachedTime = new Date();
					Calendar startDate = getStartDate(TYPE);
					String startDateStr = client.dateToString(startDate);
					cache.put(client.getDevice(), TYPE, responce, startDateStr);
					handler.onSuccess(jsonResponse, cachedTime, startDateStr);
				} catch(JSONException e){
					handler.onFailure(e);
				} catch(Exception e){
					e.printStackTrace();
				}
			}
			@Override
			public void onFailure(Throwable error){
				handler.onFailure(error);
			}
		};
	}
	
	public void checkLogin(String username, String password, SamKnowsResponseHandler handler){
		SamKnowsClient c = new SamKnowsClient(username, password);
		c.getDevices(getHandler(handler));
	}
	
	public void force(){
		FORCE = true;
	}
	
	public void unforce(){
		FORCE = false;
	}
	
	/**
	 * Get the data. This either returns stuff from the cache, if expiry is not
	 * reached yet - or returns stuff from the webservice if the cache is empty,
	 * expired or a FORCE is set.
	 * 
	 */
	public void get(int type, SamKnowsResponseHandler handler){
		CachedValue cached = cache.get(client.getDevice(), type);
		
		if (cached == null || cached.isExpired() || FORCE){
			AsyncHttpResponseHandler _handler = getHandler(handler, type);
			switch (type){
			case RECENT: client.getRecent(_handler); break;
			case WEEK: client.getWeek(_handler); break;
			case MONTH: client.getMonth(_handler); break;
			case THREE_MONTHS: client.getThreeMonths(_handler); break;
			case SIX_MONTHS: client.getSixMonths(_handler); break;
			case YEAR: client.getYear(_handler); break;
			}
		}else{
			try{
				handler.onSuccess(new JSONObject(cached.responce), new Date(cached.cachedTime), cached.cachedStart);
			}catch (JSONException e){
				handler.onFailure(e);
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Get the start date for the data. I'm using this to fill in the blanks if
	 * not enough date is available.
	 */
	private Calendar getStartDate(int TYPE){
		Calendar date = null;
		switch(TYPE){
		case RECENT: date = client.getStartDate(1); break;
		case WEEK: date = client.getStartDate(7); break;
		case MONTH: date = client.getStartDate(30); break;
		case THREE_MONTHS: date = client.getStartDate(3*30); break;
		case SIX_MONTHS: date = client.getStartDate(6*30); break;
		case YEAR: date = client.getStartDate(365); break;		
		}
		return date;
	}
}
