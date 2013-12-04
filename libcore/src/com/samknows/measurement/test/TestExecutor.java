package com.samknows.measurement.test;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.samknows.libcore.SKLogger;
import com.samknows.libcore.SKConstants;
import com.samknows.measurement.SK2AppSettings;
import com.samknows.libcore.R;
import com.samknows.measurement.activity.components.UIUpdate;
import com.samknows.measurement.schedule.TestDescription;
import com.samknows.measurement.schedule.TestGroup;
import com.samknows.measurement.schedule.condition.ConditionGroup;
import com.samknows.measurement.schedule.condition.ConditionGroupResult;
import com.samknows.measurement.schedule.datacollection.BaseDataCollector;
import com.samknows.measurement.storage.DBHelper;

import com.samknows.measurement.storage.ResultsContainer;
import com.samknows.measurement.storage.TestBatch;
import com.samknows.measurement.util.DCSStringBuilder;
import com.samknows.tests.ClosestTarget;
import com.samknows.tests.Param;
import com.samknows.tests.Test;
import com.samknows.tests.TestFactory;

public class TestExecutor {
	private static final String JSON_SUBMISSION_TYPE = "submission_type";
	private static final String TAG = TestExecutor.class.getName();
	private TestContext tc;
	private Test executingTest;
	private long lastTestBytes;
	private Thread startThread = null;
	public ResultsContainer rc;

	public TestExecutor(TestContext tc) {
		super();
		this.tc = tc;
		rc = new ResultsContainer();
		
		// This is required to be stored in the test Context; otherwise,
		// we cannot capture information on failed Conditions!
		tc.resultsContainer = rc;
		
	}
	
	public void addRequestedTest(TestDescription td){
		rc.addRequestedTest(td);
	}
	
	public TestResult execute(ConditionGroup cg){ 
		TestResult ret = new TestResult();
		if(cg == null){
			return ret;
		}
		try{
			ConditionGroupResult c = (ConditionGroupResult ) cg.testBefore(tc).get(); 
			ret.add(c);
		//Treat exceptions as failures	
		} catch(ExecutionException ee){
			SKLogger.e(this, "Error in running test condition: " + ee.getMessage());
			ret.isSuccess=false;
		} catch(InterruptedException ie){
			SKLogger.e(this, "Error in running test condition: " + ie.getMessage());
			ret.isSuccess=false;
		}
		return ret;
	
	}
	
	public TestResult execute(ConditionGroup cg, TestDescription td) {
		showNotification(tc.getString(R.string.ntf_checking_conditions));
		TestResult result = execute(cg);

		if (result.isSuccess || result.isFailQuiet()) {
			executeTest(td, result);
		}
		SKLogger.d(TAG, "result test: " + (result.isSuccess ? "OK" : "FAIL"));

		if (result.isSuccess && cg != null) {
			ConditionGroupResult cgr = cg.testAfter(tc);
			result.add(cgr);
			rc.addCondition(result.json_results);
		}

		if (cg != null) {
			cg.release(tc);
		}

		SKLogger.d(this, rc.getJSON().toString());

		// TestResultsManager.saveResult(tc.getServiceContext(),
		// result.results);
		cancelNotification();
		return result;
	}

	public void executeTest(TestDescription td, TestResult result) {
		try {
			List<Param> params = tc.paramsManager.prepareParams(td.params);

			executingTest = TestFactory.create(td.type, params);
			if (executingTest != null) {
				getPartialResult();
				SKLogger.d(TestExecutor.class, "start to execute test: "
						+ td.displayName);
				showNotification(tc.getString(R.string.ntf_running_test)
						+ td.displayName);
				
				//execute the test in a new thread and kill it if it doesn't terminate after
				//Constants.WAIT_TEST_BEFORE_ABORT
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						executingTest.execute();
					}
				});
				t.start();
				t.join(SKConstants.WAIT_TEST_BEFORE_ABORT);
				if (t.isAlive()) {
					SKLogger.e(this, "Test is still runnuing after "+SKConstants.WAIT_TEST_BEFORE_ABORT/1000+" seconds.");
					t.interrupt();
					t = null;
				} else {
					lastTestBytes = executingTest.getNetUsage();
					result.isSuccess = executingTest.isSuccessful();
					String out = executingTest.getOutputString();
					result.addTestString(out);
					rc.addTest(executingTest.getJSONResult());
					// HACK TO INCLUDE THE JUDPJITTER RESULTS
					if (td.type.equalsIgnoreCase("latency")) {
						String[] judp = executingTest.getOutputFields();
						DCSStringBuilder jjitter = new DCSStringBuilder();
						String jitter = ""+ (Integer.parseInt(judp[5]) - Integer.parseInt(judp[6]));
						String sent = ""+ (Integer.parseInt(judp[9]) + Integer.parseInt(judp[10]));
						String received = ""+ (Integer.parseInt(judp[9]) - Integer.parseInt(judp[10]));
						jjitter.append("JUDPJITTER");
						jjitter.append(judp[1]); // TIMESTAMP
						jjitter.append(judp[2]); // STATUS
						jjitter.append(judp[3]); // TARGET
						jjitter.append(judp[4]); // TARGET IP ADDRESS
						jjitter.append(128); // PACKETSIZE
						jjitter.append(0); // BITRATE
						jjitter.append(0); // DURATION
						jjitter.append(sent); // PACKETS SENT UP
						jjitter.append(sent); // PACKETS SENT DOWN
						jjitter.append(received); // PACKETS RECEIVED UP
						jjitter.append(received); // PACKETS RECEIVED DOWN
						jjitter.append(jitter); // JITTER UP
						jjitter.append(jitter); // JITTER DOWN
						jjitter.append(judp[5]); // AVERAGE RTT
						result.addTestString(jjitter.build());
					}

					if (result.isSuccess) {
						tc.paramsManager.processOutParams(out,
								td.outParamsDescription);
						if (executingTest.getHumanReadable() != null) {
							HashMap<String, String> last_values = executingTest
									.getHumanReadable().getValues();
							for (String key : last_values.keySet()) {
								String value = last_values.get(key);
								SKLogger.d(TestExecutor.class, "last_" + key
										+ " " + value);
								SK2AppSettings.getInstance().saveString(
										"last_" + key, value);
							}
						}
					}

					SKLogger.d(TAG, "finished execution test: " + td.type);
				}
			} else {
				SKLogger.e(TAG, "Can't find test for: " + td.type,
						new RuntimeException());
				result.isSuccess = false;
			}
		} catch (Throwable e) {
			SKLogger.e(this, "Error in executing the test. ", e);
			result.isSuccess = false;
		} finally {
			cancelNotification();
		}
	}

	public int getProgress() {
		if (executingTest != null) {
			return executingTest.getProgress();
		}
		return -1;
	}

	public void getPartialResult() {
		if (executingTest instanceof ClosestTarget) {
			final ClosestTarget ct = (ClosestTarget) executingTest;
			Runnable r = new Runnable() {
				public void run() {
					SKLogger.d(TestExecutor.class, "getPartialResult started");
					while (true) {
						ClosestTarget.Result r = ct.getPartialResults();
						if (r == null) {
							break;
						}
						String target = tc.config
								.findHostName(r.currbest_target);
						r.currbest_target = target;
						JSONObject p = UIUpdate
								.getClosestTargetPartialResult(r);
						tc.publish(p);
						SKLogger.d(TestExecutor.class, p.toString());
					}
				}
			};
			new Thread(r).start();
		}

	}

	public Test.HumanReadable getHumanReadable() {
		if (executingTest == null) {
			return null;
		}
		return executingTest.getHumanReadable();
	}

	public String getHumanReadableResult() {
		if (executingTest != null) {
			return executingTest.getHumanReadableResult();
		} else {
			return "failed to find test!";
		}
	}

	@SuppressWarnings("deprecation")
	public void showNotification(String message) {
		String title = tc.getString(R.string.ntf_title);

		NotificationManager manager = (NotificationManager) tc
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification n = new Notification(R.drawable.icon, message,
				System.currentTimeMillis());
		PendingIntent intent = PendingIntent.getService(tc.getServiceContext(),
				SKConstants.RC_NOTIFICATION, new Intent(),
				PendingIntent.FLAG_UPDATE_CURRENT);
		n.setLatestEventInfo(tc.getServiceContext(), title, message, intent);
		manager.notify(SKConstants.NOTIFICATION_ID, n);
	}

	public void cancelNotification() {
		NotificationManager manager = (NotificationManager) tc
				.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(SKConstants.NOTIFICATION_ID);
	}

	public void startInBackGround() {
		startThread = new Thread(new Runnable() {
			public void run() {
				start();
			}
		});
		startThread.start();
	}

	public void start() {
		for (BaseDataCollector collector : tc.config.dataCollectors) {
			if (collector.isEnabled)
				collector.start(tc);
		}
	}

	public void stop() {
		if (startThread != null) {
			for (;;) {
				
				try {
					if (!startThread.isAlive()) {
						break;
					}
					
					startThread.join(100);
					
				} catch (InterruptedException ie) {
					SKLogger.e(this, "Ignore InterruptedException while waiting for the start thread to finish");
					SKLogger.sAssert(getClass(), false);
				}
			}
		}
		
		for (BaseDataCollector collector : tc.config.dataCollectors) {
			if (collector.isEnabled) {
				collector.stop(tc);
				// TestResultsManager.saveResult(tc.getServiceContext(),
				// collector.getOutput());
				rc.addMetric(collector.getJSONOutput());
			}
		}
		
	}

	public TestResult executeGroup(TestGroup tg) {
		long startTime = System.currentTimeMillis();
		List<TestDescription> tds = new ArrayList<TestDescription>();
		for (int test_id : tg.testIds) {
			tds.add(tc.config.findTestById(test_id));
		}
		ConditionGroup cg = tc.config.getConditionGroup(tg.conditionGroupId);
		showNotification(tc.getString(R.string.ntf_checking_conditions));
		TestResult result = execute(cg);
		
		
		//Run tests only if the conditions are met
		if(result.isSuccess){
			for (TestDescription td : tds) {
				executeTest(td, result);
			}
		}
		List<JSONObject> testsResults = new ArrayList<JSONObject>();
		for (String out : result.results) {
			testsResults.addAll(com.samknows.measurement.storage.TestResult
					.testOutput(out));
		}

		if (cg != null) {
			ConditionGroupResult cgr = cg.testAfter(tc);
			result.add(cgr);
			rc.addCondition(result.json_results);
			cg.release(tc);
		}
		List<JSONObject> passiveMetrics = new ArrayList<JSONObject>();
		for (BaseDataCollector c : tc.config.dataCollectors) {
			if (c.isEnabled) {
				for (JSONObject o : c.getPassiveMetric()) {
					passiveMetrics.add(o);
				}
			}
		}
		JSONObject batch = new JSONObject();
		try {
			batch.put(TestBatch.JSON_DTIME, startTime);
			batch.put(TestBatch.JSON_RUNMANUALLY, "0");
		} catch (JSONException je) {
			SKLogger.e(this,
					"Error in creating test batch object: " + je.getMessage());
		}
		DBHelper db = new DBHelper(tc.getServiceContext());
		
		db.insertTestBatch(batch, testsResults, passiveMetrics);
		
		cancelNotification();
		return result;

	}

	public TestResult executeGroup(long groupId) {
		TestGroup tg = tc.config.findTestGroup(groupId);
		if (tg == null) {
			SKLogger.e(this, "can not find test for id: " + groupId);
		} else {
			return executeGroup(tg);
		}

		return new TestResult();
	}

	public TestResult execute(long testId) {
		TestDescription td = tc.config.findTest(testId);
		if (td != null) {
			ConditionGroup cg = tc.config
					.getConditionGroup(td.conditionGroupId);
			return execute(cg, td);
		} else {
			SKLogger.e(this, "can not find test for id: " + testId);
		}
		return new TestResult();
	}

	public void save(String type) {
		rc.addExtra(JSON_SUBMISSION_TYPE, type);
		TestResultsManager.saveResult(tc.getServiceContext(), rc);
	}

	public long getLastTestByte() {
		return lastTestBytes;
	}

}
