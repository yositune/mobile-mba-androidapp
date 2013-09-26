package com.samknows.measurement.test;

import java.util.Vector;

import org.json.JSONObject;

import android.content.Context;
import com.samknows.measurement.CachingStorage;
import com.samknows.measurement.MainService;
import com.samknows.measurement.Storage;
import com.samknows.measurement.TestParamsManager;
import com.samknows.measurement.schedule.ScheduleConfig;
import com.samknows.measurement.schedule.datacollection.BaseDataCollector;
import com.samknows.measurement.schedule.datacollection.LocationDataCollector;
import com.samknows.measurement.storage.Conversions;
import com.samknows.measurement.storage.ResultsContainer;

public class TestContext {
	private Context ctx;
	public ScheduleConfig config;
	public TestParamsManager paramsManager;
	public ScheduledTestExecutionQueue queue;
	public Vector<JSONObject> test_results = new Vector<JSONObject>();
	public Vector<JSONObject> passive_metrics = new Vector<JSONObject>();
	
	public ResultsContainer resultsContainer = null;
	
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
