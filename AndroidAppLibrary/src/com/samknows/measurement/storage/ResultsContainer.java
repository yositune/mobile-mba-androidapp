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

