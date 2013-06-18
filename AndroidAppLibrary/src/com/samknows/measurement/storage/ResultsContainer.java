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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.samknows.measurement.AppSettings;
import com.samknows.measurement.Logger;

public class ResultsContainer {

	public static final String JSON_TESTS = "tests";
	public static final String JSON_CONDITIONS = "conditions";
	public static final String JSON_METRICS = "metrics";
	private List<JSONObject> mTests = new ArrayList<JSONObject>();
	private List<JSONObject> mConditions = new ArrayList<JSONObject>();
	private List<JSONObject> mMetrics = new ArrayList<JSONObject>();
	private Map<String, String> mExtra = new LinkedHashMap<String, String>();
	
	public ResultsContainer(){
		
	}
	
	public ResultsContainer(Map<String, String> extra){
		mExtra.putAll(extra);
	}
	
	public void addTest(JSONObject test){
		mTests.add(test);
	}
	
	public void addTest(List<JSONObject> tests){
		mTests.addAll(tests);
	}
	
	public void addCondition(JSONObject condition){
		mConditions.add(condition);
	}
	
	public void addCondition(List<JSONObject> conditions){
		mConditions.addAll(conditions);
	}
	
	public void addMetric(JSONObject metric){
		mMetrics.add(metric);
	}
	
	public void addMetric(List<JSONObject> metrics){
		mMetrics.addAll(metrics);
	}
	
	public void addExtra(String key, String value){
		mExtra.put(key, value);
	}
	
	public JSONObject getJSON(){
		mExtra.putAll(AppSettings.getInstance().getJSONExtra());
		JSONObject ret = new JSONObject(mExtra);
		JSONArray tests = new JSONArray();
		JSONArray conditions = new JSONArray();
		JSONArray metrics = new JSONArray();
		
		for(JSONObject t: mTests){
			tests.put(t);
		}
		for(JSONObject c: mConditions){
			conditions.put(c);
		}
		for(JSONObject m: mMetrics){
			metrics.put(m);
		}
		
		try{
			ret.put(JSON_TESTS, tests);
			ret.put(JSON_METRICS, metrics);
			ret.put(JSON_CONDITIONS, conditions);
		}catch(JSONException je){
			Logger.e(this, "Error in creating a JSONObject: " + je.getMessage() );
			ret = null;
		}
		return ret;
	}
	
}

