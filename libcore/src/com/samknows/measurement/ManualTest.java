package com.samknows.measurement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.JSONException;
import org.json.JSONObject;


import com.samknows.libcore.R;
import com.samknows.libcore.SKLogger;
import com.samknows.measurement.net.SubmitTestResultsAction;
import com.samknows.measurement.net.SubmitTestResultsAnonymousAction;
import com.samknows.measurement.schedule.ScheduleConfig;
import com.samknows.measurement.schedule.TestDescription;
import com.samknows.measurement.schedule.datacollection.BaseDataCollector;
import com.samknows.measurement.storage.DBHelper;
import com.samknows.measurement.storage.PassiveMetric;
import com.samknows.measurement.storage.TestBatch;
import com.samknows.measurement.storage.TestResult;
import com.samknows.measurement.test.TestContext;
import com.samknows.measurement.test.TestExecutor;
import com.samknows.tests.TestFactory;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

/*  
 * This class is used to run the the tests when they are executed manually
 * implements a runnable interface and and uses an Handler in order to publish
 * the tests results to the interface
 */
public class ManualTest implements Runnable {
	private Handler mHandler;
	private List<TestDescription> mTestDescription;
	private Context ctx;
	private AtomicBoolean run = new AtomicBoolean(true);
	public static boolean isExecuting = false;

	ManualTest(Context ctx, Handler handler, List<TestDescription> td) {
		mHandler = handler;
		mTestDescription = td;
		this.ctx = ctx;
	}

	/*
	 * Returns a ManualTest object that runs only the test with id test_id
	 */

	public static ManualTest create(Context ctx, Handler handler, int test_id, StringBuilder errorDescription) {
		ManualTest ret = create(ctx, handler, errorDescription);
		if (ret == null) {
			return ret;
		}
		for (TestDescription td : ret.mTestDescription) {
			if (td.testId == test_id) {
				ret.mTestDescription = new ArrayList<TestDescription>();
				ret.mTestDescription.add(td);
				return ret;
			}
		}
		SKLogger.e(ManualTest.class,
				"ManualTest cannot be initialized because there is no manual test with id: "
						+ test_id);
		return null;
	}

	/*
	 * Returns a ManualTest object if the manual_tests list of the schedule
	 * config is not empty and the MainService is not executing
	 */
	public static ManualTest create(Context ctx, Handler handler, StringBuilder RErrorDescription) {
		Storage storage = CachingStorage.getInstance();
		ScheduleConfig config = storage.loadScheduleConfig();
		if (config == null) {
			RErrorDescription.append(ctx.getString(R.string.manual_test_create_failed_1));
			SKLogger.e( ManualTest.class, RErrorDescription.toString());
			return null;
		}
		if (config.manual_tests.size() == 0) {
			RErrorDescription.append(ctx.getString(R.string.manual_test_create_failed_2));
			SKLogger.e( ManualTest.class, RErrorDescription.toString());
			return null;
		}
		if (MainService.isExecuting()) {
			RErrorDescription.append(ctx.getString(R.string.manual_test_create_failed_3));
			SKLogger.e(ManualTest.class, RErrorDescription.toString());
			return null;
		}
		/*
	   <string name="manual_test_create_failed_1">ManualTest cannot be initialized because storage does not contain the ScheduleConfig</string>
	   <string name="manual_test_create_failed_1">ManualTest cannot be initialized because storage does not contain the ScheduleConfig</string>
	   <string name="manual_test_create_failed_1">ManualTest cannot be initialized because storage does not contain the ScheduleConfig</string>
		*/
		
		return new ManualTest(ctx, handler, config.manual_tests);
	}

	// returns the maximum amount of bytes used by the manual test
	public long getNetUsage() {
		long ret = 0;
		for (TestDescription td : mTestDescription) {
			ret += td.maxUsageBytes;
		}
		return ret;
	}

	/*
	 * Runs all the test in manual tests
	 */

	@Override
	public void run() {
		DBHelper db = new DBHelper(ctx);
	
		
		sSetIsExecuting(true);
		
		// Start collectors for the passive metrics
		// Start tests
		long startTime = System.currentTimeMillis();
		JSONObject batch = new JSONObject();
		TestContext tc = TestContext.createManualTestContext(ctx);
		TestExecutor manualTestExecutor = new TestExecutor(tc);
		List<JSONObject> testsResults = new ArrayList<JSONObject>();
		List<JSONObject> passiveMetrics = new ArrayList<JSONObject>();
		manualTestExecutor.startInBackGround();
		Message msg;
		long testsBytes = 0;
		for (TestDescription td : mTestDescription) {
			manualTestExecutor.addRequestedTest(td);
			// check if a stop command has been received
			if (!run.get()) {
				manualTestExecutor.cancelNotification();
				SKLogger.d(this, "Manual test interrupted by the user.");
				break;
			}
			com.samknows.measurement.test.TestResult tr = new com.samknows.measurement.test.TestResult();
			ObservableExecutor oe = new ObservableExecutor(manualTestExecutor, td, tr);
			Thread t = new Thread(oe);
			t.start();
			while (true) {
				try {
					t.join(100);
					if (!t.isAlive())
						break;
				} catch (InterruptedException ie) {
					SKLogger.e(this, ie.getMessage());
				}
				for (JSONObject pm : progressMessage(td, manualTestExecutor)) {
					msg = new Message();
					msg.obj = pm;
					mHandler.sendMessage(msg);
				}

			}
			testsBytes += manualTestExecutor.getLastTestByte();

			List<JSONObject> currResults = new ArrayList<JSONObject>();
			for (String out : tr.results) {
				currResults.addAll(TestResult.testOutput(out));
			}
			for (JSONObject cr : currResults) {
				// publish results
				msg = new Message();
				msg.obj = cr;
				mHandler.sendMessage(msg);
			}
			testsResults.addAll(currResults);
		}
		SKLogger.d(this, "bytes used by the tests: " + testsBytes);
		SK2AppSettings.getInstance().appendUsedBytes(testsBytes);
		// stops collectors
		manualTestExecutor.stop();

		manualTestExecutor.save("manual_test");

		// Gather data from collectors
		for (BaseDataCollector collector : tc.config.dataCollectors) {
			if (collector.isEnabled) {
				for (JSONObject o : collector.getPassiveMetric()) {
					// update interface
					msg = new Message();
					msg.obj = PassiveMetric.passiveMetricToCurrentTest(o);
					mHandler.sendMessage(msg);
					// save metric
					passiveMetrics.add(o);
				}
			}
		}
		// insert batch in the database
		try {
			batch.put(TestBatch.JSON_DTIME, startTime);
			batch.put(TestBatch.JSON_RUNMANUALLY, "1");
		} catch (JSONException je) {
			SKLogger.e(this,
					"Error in creating test batch object: " + je.getMessage());
		}

		// insert the results in the database only if we didn't receive a stop
		// command
		if (run.get()) {
			db.insertTestBatch(batch, testsResults, passiveMetrics);
		}
		// Send completed message to the interface
		msg = new Message();
		JSONObject jtc = new JSONObject();
		try {
			Thread.sleep(1000);
			jtc.put(TestResult.JSON_TYPE_ID, "completed");
			msg.obj = jtc;

		} catch (JSONException je) {
			SKLogger.e(this, "Error in creating json object: " + je.getMessage());
		} catch (InterruptedException e) {
			SKLogger.e(
					this,
					"Sleep interrupted in the manual test view: "
							+ e.getMessage());
		}
		mHandler.sendMessage(msg);

		try {
			// Submitting test results
			if (SK2AppSettings.getSK2AppSettingsInstance().anonymous) {
				new SubmitTestResultsAnonymousAction(ctx).execute();
			} else {
				new SubmitTestResultsAction(ctx).execute();
			}
		} catch (Throwable t) {
			SKLogger.e(this, "Submit result. ", t);
		}
	
		if(!SK2AppSettings.getInstance().isServiceEnabled()){
			MainService.force_poke(ctx);
		}
		SKLogger.d(this, "Exiting manual test");
		
		sSetIsExecuting(false);
	}
	
	private static void sSetIsExecuting(boolean bValue) {
		isExecuting = bValue;
	}

	private class ObservableExecutor implements Runnable {
		public TestExecutor te;
		TestDescription td;
		com.samknows.measurement.test.TestResult tr;

		public ObservableExecutor(TestExecutor te, TestDescription td,
				com.samknows.measurement.test.TestResult tr) {
			this.te = te;
			this.td = td;
			this.tr = tr;
		}

		@Override
		public void run() {
			te.executeTest(td, tr);
		}

	}

	static private List<JSONObject> progressMessage(TestDescription td,
			TestExecutor te) {
		List<JSONObject> ret = new ArrayList<JSONObject>();
		List<String> tests = new ArrayList<String>();

		if (td.type.equals(TestFactory.DOWNSTREAMTHROUGHPUT)) {
			tests.add("" + TestResult.DOWNLOAD_TEST_ID);
		} else if (td.type.equals(TestFactory.UPSTREAMTHROUGHPUT)) {
			tests.add("" + TestResult.UPLOAD_TEST_ID);
		} else if (td.type.equals(TestFactory.LATENCY)) {
			tests.add("" + TestResult.LATENCY_TEST_ID);
			tests.add("" + TestResult.PACKETLOSS_TEST_ID);
			tests.add("" + TestResult.JITTER_TEST_ID);
		}
		try {
			for (String t : tests) {
				JSONObject c = new JSONObject();
				c.put(TestResult.JSON_TYPE_ID, "test");
				c.put(TestResult.JSON_TESTNUMBER, t);
				c.put(TestResult.JSON_STATUS_COMPLETE, te.getProgress());
				c.put(TestResult.JSON_HRRESULT, "");
				ret.add(c);
			}
		} catch (JSONException je) {
			SKLogger.e(
					ManualTest.class,
					"Error in creating JSON progress object: "
							+ je.getMessage());
		}
		return ret;
	}

	// It stops the test from being executed
	public void stop() {
		
		run.set(false);
	}
}
