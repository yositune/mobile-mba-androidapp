package com.samknows.measurement.statemachine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import com.samknows.libcore.SKLogger;
import com.samknows.measurement.CachingStorage;
import com.samknows.measurement.MainService;
import com.samknows.measurement.SK2AppSettings;
import com.samknows.measurement.Storage;
import com.samknows.measurement.environment.CellTowersDataCollector;
import com.samknows.measurement.environment.DCSData;
import com.samknows.measurement.environment.NetworkDataCollector;
import com.samknows.measurement.environment.PhoneIdentityData;
import com.samknows.measurement.environment.PhoneIdentityDataCollector;
import com.samknows.measurement.schedule.ScheduleConfig;
import com.samknows.measurement.schedule.TestDescription;
import com.samknows.measurement.environment.BaseDataCollector;
import com.samknows.measurement.schedule.datacollection.LocationDataCollector;
import com.samknows.measurement.storage.DBHelper;
import com.samknows.measurement.storage.ResultsContainer;
import com.samknows.measurement.storage.TestBatch;
import com.samknows.measurement.test.TestContext;
import com.samknows.measurement.test.TestExecutor;
import com.samknows.measurement.test.TestResult;


public class ContinuousTesting {
	private static final String JSON_SUBMISSION_TYPE = "continuous_testing";
	private MainService mContext;
	private State mPreviousState;
	List<BaseDataCollector> mCollectors;
	LocationDataCollector mLocationDataCollector;
	List<DCSData> mListDCSData;
	TestContext mTestContext ;
	ScheduleConfig mConfig;
	ResultsContainer mResultsContainer;
	DBHelper mDBHelper;
	public ContinuousTesting(MainService ctx){
		mContext = ctx;
		mListDCSData = new ArrayList<DCSData>();
		mTestContext = TestContext.createBackgroundTestContext(mContext);
		mResultsContainer = new ResultsContainer();
		mDBHelper = new DBHelper(mContext);
	}
	/*
	 * execute the continuous testing
	 * these steps are followed
	 * check for config file
	 * start data collectors
	 * run init tests
	 * while(not stopped)
	 * 		run tests
	 * 		submit data
	 * 		
	 */
	public void execute(){
		StateResponseCode response;
		SK2AppSettings appSettings = SK2AppSettings.getSK2AppSettingsInstance();
		mPreviousState = appSettings.getState();
		State state  = State.DOWNLOAD_CONFIG_ANONYMOUS;;
		appSettings.saveState(state);
		try {
			response = Transition.createState(state, mContext).executeState();
			if(response == StateResponseCode.FAIL){
				return;
			}
			state = State.RUN_INIT_TESTS;
			response = Transition.createState(state, mContext).executeState();
			if(response == StateResponseCode.FAIL){
				return;
			}
		} catch (Exception e) {
			SKLogger.e(this, "fail to execute " + state + " ");
		}
		Storage storage = CachingStorage.getInstance();
		mConfig = storage.loadScheduleConfig();
		if( mConfig == null){
			onEnd();
			throw new NullPointerException("null schedule config!");
		}
		
		
		startCollectors();

		
		while(mContext.isExecutingContinuous()){
		
			executeBatch();
			try {
				state = State.SUBMIT_RESULTS_ANONYMOUS;
				response = Transition.createState(state, mContext).executeState();
			} catch (Exception e) {
				SKLogger.e(this, "fail to execute " + state + " ");
			}
		}
		stopCollectors();
			
		
	}
	
	private void executeBatch(){
		long batchTime = System.currentTimeMillis();
		List<JSONObject> testsResults = new ArrayList<JSONObject>();
		List<JSONObject> passiveMetrics = new ArrayList<JSONObject>();
		HashMap<String, Object> batch = new HashMap<String, Object>();
		TestExecutor te = new TestExecutor(mTestContext);
		batch.put(TestBatch.JSON_DTIME, batchTime);
		batch.put(TestBatch.JSON_RUNMANUALLY, "0");
		TestResult tr = new TestResult();
		for(TestDescription td: mConfig.continuous_tests){
			te.executeTest(td, tr);
		}
		for(String out: tr.results){
			testsResults.addAll(com.samknows.measurement.storage.TestResult.testOutput(out));
		}
		collectData();
		for(DCSData d:mListDCSData){
			passiveMetrics.addAll(d.getPassiveMetric());
			te.rc.addMetric(d.convertToJSON());
		}
		mListDCSData.clear();
		te.save(JSON_SUBMISSION_TYPE);
		
		mDBHelper.insertTestBatch(new JSONObject(batch), testsResults, passiveMetrics);
		
	}
	
	
	
	private void startCollectors(){
		mCollectors = new  ArrayList<BaseDataCollector>();
		mCollectors.add(new NetworkDataCollector(mContext));
		mCollectors.add(new CellTowersDataCollector(mContext));
		
		for(com.samknows.measurement.schedule.datacollection.BaseDataCollector c: mConfig.dataCollectors){
			if( c instanceof LocationDataCollector){
				mLocationDataCollector = (LocationDataCollector)c;
			}
		}
		
		for(BaseDataCollector c: mCollectors){
			c.start();
		}
		mLocationDataCollector.start(mTestContext);
	}
	
	private void stopCollectors(){
		for(BaseDataCollector c:mCollectors){
			c.stop();
		}
		if( mLocationDataCollector != null){
			mLocationDataCollector.stop(mTestContext);
		}
	}
	
	private void collectData(){
		
		mListDCSData.add(new PhoneIdentityDataCollector(mContext).collect());
		for(BaseDataCollector c: mCollectors){
			mListDCSData.addAll(c.collectPartialData());
		}
		mListDCSData.addAll(mLocationDataCollector.getPartialData());
	}
	
	private void onEnd(){
		SK2AppSettings appSettings = SK2AppSettings.getSK2AppSettingsInstance();
		appSettings.saveState(mPreviousState)
;	}
}
