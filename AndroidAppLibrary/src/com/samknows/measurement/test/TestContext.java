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


package com.samknows.measurement.test;

import java.util.Vector;

import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;

import com.samknows.measurement.CachingStorage;
import com.samknows.measurement.MainService;
import com.samknows.measurement.Storage;
import com.samknows.measurement.TestParamsManager;
import com.samknows.measurement.schedule.ScheduleConfig;
import com.samknows.measurement.schedule.datacollection.BaseDataCollector;
import com.samknows.measurement.schedule.datacollection.LocationDataCollector;
import com.samknows.measurement.storage.Conversions;

public class TestContext {
	private Context ctx;
	public ScheduleConfig config;
	public TestParamsManager paramsManager;
	public ExecutionQueue queue;
	public Vector<JSONObject> test_results = new Vector<JSONObject>();
	public Vector<JSONObject> passive_metrics = new Vector<JSONObject>();
	
	

	
	public static TestContext create(Context ctx) {
		Storage storage = CachingStorage.getInstance();
		ScheduleConfig config = storage.loadScheduleConfig();
		if (config == null) {
			throw new NullPointerException("null schedule config!");
		}
		
		TestParamsManager paramsManager = storage.loadParamsManager();
		if (paramsManager == null) {
			paramsManager = new TestParamsManager();
		}
		return new TestContext(ctx, config, paramsManager);
	}
	
	public void addTestResult(TestResult result){
		for(String t: result.results){
			test_results.addAll(Conversions.testToJSON(t));
		}
	}
	
	public void addTestResult(String testOutput){
		test_results.addAll(Conversions.testToJSON(testOutput));
	}
	
	private TestContext(Context ctx, ScheduleConfig config, TestParamsManager manager) {
		super();
		this.ctx = ctx;
		this.config = config;
		this.paramsManager = manager;
	}
	
	
	
	public Object getSystemService(String name) {
		return ctx.getSystemService(name);
	}

	public String getString(int rid) {
		return ctx.getString(rid);
	}
	
	public Context getServiceContext() {
		return ctx;
	}
	
	public LocationDataCollector findLocationDataCollector() {
		for (BaseDataCollector d : config.dataCollectors) {
			if (d instanceof LocationDataCollector) return (LocationDataCollector) d;
		}
		return null;
	}
	
	public void publish(JSONObject obj){
		if(ctx instanceof MainService){
			((MainService) ctx).publish(obj);
		}
	}

}
