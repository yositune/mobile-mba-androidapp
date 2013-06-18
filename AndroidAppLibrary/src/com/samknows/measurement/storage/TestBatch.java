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


package com.samknows.measurement.storage;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.samknows.measurement.Logger;

public class TestBatch extends JSONObject {
	
	public static final String JSON_DTIME = "dtime";
	public static final String JSON_RUNMANUALLY = "run_manually";
	private long _starttime;
	private boolean _run_manually;
	
	private Vector<JSONObject> tests = new Vector<JSONObject>();
	private Vector<JSONObject> metrics = new Vector<JSONObject>();

	public TestBatch(){
		_starttime = System.currentTimeMillis();
	}

	public TestBatch(boolean run_manually){
		_starttime = System.currentTimeMillis();
		_run_manually = run_manually;
	}

	public TestBatch(long starttime, boolean run_manually){
		_starttime = starttime;
		_run_manually = run_manually;
	}
	
	public void setRunManually(boolean manually){
		_run_manually = manually;
	}
	
	public void addTest(JSONObject test){
		tests.add(test);
	}
	
	public void addMetric(JSONObject metric){
		metrics.add(metric);
	}
	
	public void insert(Context ctx){
		JSONObject test_batch = new JSONObject();
		try{
		test_batch.put(JSON_DTIME, _starttime);
		test_batch.put(JSON_RUNMANUALLY, _run_manually ? "1" : "0");
		}catch(JSONException je){
			Logger.e(TestBatch.class, "Error in creating the JSONObject for creating a new test batch in the DB: " + je.getMessage());
		}
		DBHelper db = new DBHelper(ctx);
		
		db.insertTestBatch(test_batch, getTests(), getMetrics());
		
	}
	
	private JSONArray getTests(){
		JSONArray json_tests = new JSONArray();
		for(JSONObject test: tests){
			json_tests.put(test);
		}
		return json_tests;
	}
	
	private JSONArray getMetrics(){
		JSONArray json_metrics = new JSONArray();
		for(JSONObject metric: metrics){
			json_metrics.put(metric);
		}
		return json_metrics;
	}

}
