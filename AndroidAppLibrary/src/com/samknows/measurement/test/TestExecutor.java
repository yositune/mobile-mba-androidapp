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

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.samknows.measurement.AppSettings;
import com.samknows.measurement.Constants;
import com.samknows.measurement.Logger;
import com.samknows.measurement.R;
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
	}

	public TestResult execute(ConditionGroup cg, TestDescription td) {
		showNotification(tc.getString(R.string.ntf_checking_conditions));
		TestResult result = new TestResult();
		if (cg != null) {
			try {
				ConditionGroupResult c = (ConditionGroupResult) cg.testBefore(
						tc).get();
				result.add(c);
			} catch (Exception e) {
				Logger.e(this,
						"Error in running a test condition: " + e.getMessage());
			}
		}

		if (result.isSuccess || result.isFailQuiet()) {
			executeTest(td, result);

		}
		Logger.d(TAG, "result test: " + (result.isSuccess ? "OK" : "FAIL"));

		if (result.isSuccess && cg != null) {
			ConditionGroupResult cgr = cg.testAfter(tc);
			result.add(cgr);
			rc.addCondition(result.json_results);
		}

		if (cg != null) {
			cg.release(tc);
		}

		Logger.d(this, rc.getJSON().toString());

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
				Logger.d(TestExecutor.class, "start to execute test: "
						+ td.displayName);
				showNotification(tc.getString(R.string.ntf_running_test)
						+ td.displayName);
				
				//execute the test in a new thread and kill it it it doesn't terminate after
				//Constants.WAIT_TEST_BEFORE_ABORT
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						executingTest.execute();
					}
				});
				t.start();
				t.join(Constants.WAIT_TEST_BEFORE_ABORT);
				if (t.isAlive()) {
					Logger.e(this, "Test is still runnuing after "+Constants.WAIT_TEST_BEFORE_ABORT/1000+" seconds.");
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
								Logger.d(TestExecutor.class, "last_" + key
										+ " " + value);
								AppSettings.getInstance().saveString(
										"last_" + key, value);
							}
						}
					}

					Logger.d(TAG, "finished execution test: " + td.type);
				}
			} else {
				Logger.e(TAG, "Can't find test for: " + td.type,
						new RuntimeException());
				result.isSuccess = false;
			}
		} catch (Throwable e) {
			Logger.e(this, "Error in executing the test. ", e);
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
					Logger.d(TestExecutor.class, "getPartialResult started");
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
						Logger.d(TestExecutor.class, p.toString());
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
				Constants.RC_NOTIFICATION, new Intent(),
				PendingIntent.FLAG_UPDATE_CURRENT);
		n.setLatestEventInfo(tc.getServiceContext(), title, message, intent);
		manager.notify(Constants.NOTIFICATION_ID, n);
	}

	public void cancelNotification() {
		NotificationManager manager = (NotificationManager) tc
				.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(Constants.NOTIFICATION_ID);
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
			try {
				startThread.join(1000);
			} catch (InterruptedException ie) {
				Logger.e(this,
						"Exception while waiting for the start thread to finish");
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
		TestResult result = new TestResult();
		if (cg != null) {
			try {
				ConditionGroupResult c = (ConditionGroupResult) cg.testBefore(
						tc).get();
				result.add(c);
			} catch (Exception e) {
				Logger.e(this,
						"Error in running a test condition: " + e.getMessage());
			}
		}

		for (TestDescription td : tds) {
			executeTest(td, result);
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
			Logger.e(this,
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
			Logger.e(this, "can not find test for id: " + groupId);
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
			Logger.e(this, "can not find test for id: " + testId);
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
