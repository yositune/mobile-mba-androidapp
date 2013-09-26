package com.samknows.measurement.storage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.samknows.libcore.SKLogger;
import com.samknows.measurement.environment.TrafficData;

//Helper class for accessing the data stored in the SQLite DB
//It Exposes only the methods to populate the Interface
//and to insert new data in the db
public class DBHelper {
	// grapdata JSONObject keys
	public static final String GRAPHDATA_TYPE = "type";
	public static final String GRAPHDATA_YLABEL = "y_label";
	public static final String GRAPHDATA_STARTDATE = "start_date";
	public static final String GRAPHDATA_ENDDATE = "end_date";
	public static final String GRAPHDATA_RESULTS = "results";
	public static final String GRAPHDATA_RESULTS_DATETIME = "datetime";
	public static final String GRAPHDATA_RESULTS_VALUE = "value";
	public static final String[] GRAPHDATA_JSON_KEYS = { GRAPHDATA_TYPE,
			GRAPHDATA_YLABEL, GRAPHDATA_STARTDATE, GRAPHDATA_ENDDATE,
			GRAPHDATA_RESULTS, GRAPHDATA_RESULTS_DATETIME,
			GRAPHDATA_RESULTS_VALUE };

	// gridtata JSONObject keys
	public static final String GRIDDATA_TYPE = "type";
	public static final String GRIDDATA_RESULTS = "results";
	public static final String GRIDDATA_RESULTS_ARCHIVEINDEX = "archiveindex";
	public static final String GRIDDATA_RESULTS_DTIME = "dtime";
	public static final String GRIDDATA_RESULTS_DATETIME = "datetime";
	public static final String GRIDDATA_RESULTS_LOCATION = "location";
	public static final String GRIDDATA_RESULTS_RESULT = "result";
	public static final String GRIDDATA_RESULTS_SUCCESS = "success";
	public static final String GRIDDATA_RESULTS_HRRESULT = "hrresult";
	public static final String[] GRIDDATA_JSON_KEYS = { GRIDDATA_TYPE,
			GRIDDATA_RESULTS };

	// averagedata JSONObject keys
	public static final String AVERAGEDATA_TYPE = "type";
	public static final String AVERAGEDATA_VALUE = "value";
	public static final String[] AVERAGEDATA_JSON_KEYS = { AVERAGEDATA_TYPE,
			AVERAGEDATA_VALUE };

	// archivedata JSONObject keys
	public static final String ARCHIVEDATA_INDEX = "index";
	public static final String ARCHIVEDATA_DTIME = "dtime";
	public static final String ARCHIVEDATA_DATETIME = "datetime";
	public static final String ARCHIVEDATA_ACTIVEMETRICS = "activemetrics";
	public static final String ARCHIVEDATA_ACTIVEMETRICS_TEST = "test";
	public static final String ARCHIVEDATA_ACTIVEMETRICS_DTIME = "dtime";
	public static final String ARCHIVEDATA_ACTIVEMETRICS_DATETIME = "datetime";
	public static final String ARCHIVEDATA_ACTIVEMETRICS_LOCATION = "location";
	public static final String ARCHIVEDATA_ACTIVEMETRICS_RESULT = "result";
	public static final String ARCHIVEDATA_ACTIVEMETRICS_SUCCESS = "success";
	public static final String ARCHIVEDATA_ACTIVEMETRICS_HRRESULT = "hrresult";
	public static final String ARCHIVEDATA_PASSIVEMETRICS_METRIC = "metric";
	public static final String ARCHIVEDATA_PASSIVEMETRICS_TYPE = "type";
	public static final String ARCHIVEDATA_PASSIVEMETRICS_VALUE = "value";
	public static final String ARCHIVEDATA_PASSIVEMETRICS = "passivemetrics";
	public static final String[] ARCHIVEDATA_JSON_KEYS = { ARCHIVEDATA_INDEX,
			ARCHIVEDATA_DTIME, ARCHIVEDATA_DATETIME, ARCHIVEDATA_ACTIVEMETRICS,
			ARCHIVEDATA_PASSIVEMETRICS };

	// archivedatasummary JSONObject keys
	public static final String ARCHIVEDATASUMMARY_COUNTER = "counter";
	public static final String ARCHIVEDATASUMMARY_STARTDATE = "startdate";
	public static final String ARCHIVEDATASUMMARY_ENDDATE = "enddate";
	public static final String ARCHIVEDATASUMMARY_TESTCOUNTER = "test_counter";
	public static final String[] ARCHIVEDATASUMMARY_JSON_KEYS = {
			ARCHIVEDATASUMMARY_COUNTER, ARCHIVEDATASUMMARY_STARTDATE,
			ARCHIVEDATASUMMARY_ENDDATE };

	// members
	private SQLiteDatabase database;
	private SKSQLiteHelper dbhelper;
	private static Object sync = new Object();

	// Constructor used to set the context
	public DBHelper(Context context) {
		dbhelper = new SKSQLiteHelper(context);
	}

	private boolean open() {
		boolean ret = false;
		try {
			database = dbhelper.getWritableDatabase();
			ret = true;
		} catch (SQLException sqle) {
			SKLogger.e(this, "Error in opening the database.", sqle);
		}
		return ret;
	}

	private void close() {
		database.close();
	}

	public synchronized boolean isEmpty() {
		synchronized (sync) {
			open();
			boolean ret = false;
			Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM "
					+ SKSQLiteHelper.TABLE_TESTRESULT, null);
			cursor.moveToFirst();
			ret = cursor.getInt(0) == 0;
			cursor.close();
			close();
			return ret;
		}
	}

	// converter
	private static String testValueToGraph(int test_type_id, double value) {
		String ret = value + "";
		switch (test_type_id) {
		case TestResult.UPLOAD_TEST_ID:
		case TestResult.DOWNLOAD_TEST_ID:
			ret = ((double) value / 1000000) + "";
			break;
		case TestResult.LATENCY_TEST_ID:
		case TestResult.JITTER_TEST_ID:
			ret = ((long) value / 1000) + "";
			break;
		case TestResult.PACKETLOSS_TEST_ID:
			ret = String.format("%.2f", value);
			break;
		}
		return ret;
	}

	// Translate an entry in the test result table results entry if a JSONObject
	// for archivedata
	private static JSONObject testResultToArchiveData(JSONObject tr) {
		JSONObject ret = new JSONObject();
		try {
			String test_type = tr.getString(SKSQLiteHelper.TR_COLUMN_TYPE);
			int test_type_id = TestResult.testStringToId(test_type);
			long dtime = tr.getLong(SKSQLiteHelper.TR_COLUMN_DTIME);
			String location = tr.getString(SKSQLiteHelper.TR_COLUMN_LOCATION);
			int success = tr.getInt(SKSQLiteHelper.TR_COLUMN_SUCCESS);
			double result = tr.getDouble(SKSQLiteHelper.TR_COLUMN_RESULT);
			String hrresult = TestResult.hrResult(test_type_id, result);
			ret.put(ARCHIVEDATA_ACTIVEMETRICS_TEST, test_type_id);
			ret.put(ARCHIVEDATA_ACTIVEMETRICS_DTIME, dtime);
			ret.put(ARCHIVEDATA_ACTIVEMETRICS_SUCCESS, success + "");
			ret.put(ARCHIVEDATA_ACTIVEMETRICS_LOCATION, location);
			ret.put(ARCHIVEDATA_ACTIVEMETRICS_RESULT, result);
			ret.put(ARCHIVEDATA_ACTIVEMETRICS_HRRESULT, hrresult);
		} catch (JSONException je) {

		}
		return ret;
	}

	// Translate an entry in the test result table results entry if a JSONObject
	// for griddata
	private static JSONObject testResultToGridData(JSONObject tr) {
		JSONObject ret = new JSONObject();
		try {
			String test_type = tr.getString(SKSQLiteHelper.TR_COLUMN_TYPE);
			int test_type_id = TestResult.testStringToId(test_type);
			long dtime = tr.getLong(SKSQLiteHelper.TR_COLUMN_DTIME);
			String location = tr.getString(SKSQLiteHelper.TR_COLUMN_LOCATION);
			int success = tr.getInt(SKSQLiteHelper.TR_COLUMN_SUCCESS);
			double result = tr.getDouble(SKSQLiteHelper.TR_COLUMN_RESULT);
			String hrresult = TestResult.hrResult(test_type_id, result);
			ret.put(GRIDDATA_RESULTS_DTIME, dtime);
			ret.put(GRIDDATA_RESULTS_SUCCESS, success + "");
			ret.put(GRIDDATA_RESULTS_LOCATION, location);
			ret.put(GRIDDATA_RESULTS_RESULT, result);
			ret.put(GRIDDATA_RESULTS_HRRESULT, hrresult);
		} catch (JSONException je) {

		}
		return ret;
	}

	// Translate an entry in the test result table results entry if a JSONObject
	// for graphdata
	private static JSONObject testResultToGraphData(int test_type_id,
			JSONObject tr) {
		JSONObject ret = new JSONObject();
		try {
			String value = testValueToGraph(test_type_id,
					tr.getDouble(SKSQLiteHelper.TR_COLUMN_RESULT));
			long dtime = tr.getLong(SKSQLiteHelper.TR_COLUMN_DTIME);
			ret.put(GRAPHDATA_RESULTS_DATETIME, "" + dtime);
			ret.put(GRAPHDATA_RESULTS_VALUE, value);
		} catch (JSONException je) {

		}
		return ret;
	}

	private static JSONObject passiveMetricToArchiveData(JSONObject pm) {
		JSONObject ret = new JSONObject();
		try {
			String metric = pm.getString(SKSQLiteHelper.PM_COLUMN_METRIC);
			String type = pm.getString(SKSQLiteHelper.PM_COLUMN_TYPE);
			String value = pm.getString(SKSQLiteHelper.PM_COLUMN_VALUE);
			ret.put(ARCHIVEDATA_PASSIVEMETRICS_METRIC, metric);
			ret.put(ARCHIVEDATA_PASSIVEMETRICS_TYPE, type);
			ret.put(ARCHIVEDATA_PASSIVEMETRICS_VALUE, value);
		} catch (JSONException je) {
			SKLogger.e(DBHelper.class, "error creating json object", je);
		}
		return ret;
	}

	// Translatror
	private static String testIdToGraphLabel(int test_type_id) {
		String ret = "";
		switch (test_type_id) {
		case TestResult.UPLOAD_TEST_ID:
		case TestResult.DOWNLOAD_TEST_ID:
			ret = "Mbps";
			break;
		case TestResult.LATENCY_TEST_ID:
		case TestResult.JITTER_TEST_ID:
			ret = "ms";
			break;
		case TestResult.PACKETLOSS_TEST_ID:
			ret = "%";
			break;
		}
		return ret;
	}

	// Returns the JSONObject containing the data to draw a graph for one test
	// of type test_type_id between startdtime and enddtime
	public JSONObject getGraphData(int test_type_id, long startdtime,
			long enddtime) {
		JSONObject ret = new JSONObject();
		String test_type = TestResult.testIdToString(test_type_id);
		try {
			ret.put(GRAPHDATA_TYPE, test_type_id);
			ret.put(GRAPHDATA_YLABEL, testIdToGraphLabel(test_type_id));
			ret.put(GRAPHDATA_STARTDATE, startdtime + "");
			ret.put(GRAPHDATA_ENDDATE, enddtime + "");
			List<JSONObject> entries = getTestResultByTypeAndInterval(
					test_type, startdtime, enddtime);
			JSONArray results = new JSONArray();
			for (JSONObject jo : entries) {
				results.put(testResultToGraphData(test_type_id, jo));

			}
			ret.put(GRAPHDATA_RESULTS, results);
		} catch (JSONException je) {

		}
		return ret;
	}

	// Returns the JSONObject containing the data to populate a grid for a
	// specific test
	// with id test_type_id, returns offset entry starting from index
	public JSONObject getGridData(int test_type_id, int index, int offset) {
		JSONObject ret = new JSONObject();
		String test_type = TestResult.testIdToString(test_type_id);
		List<JSONObject> entries = getFilteredTestResults(test_type, index,
				offset);
		try {
			ret.put(GRIDDATA_TYPE, test_type_id);
			JSONArray results = new JSONArray();
			for (JSONObject jo : entries) {
				results.put(testResultToGridData(jo));
			}
			ret.put(GRIDDATA_RESULTS, results);
		} catch (JSONException je) {
			SKLogger.e(DBHelper.class, "Error in creating data for the grid");
		}
		return ret;
	}

	// Return the JSONObject to populate an archive view
	// index is the position of the archive data in the database
	public JSONObject getArchiveData(int index) {
		synchronized (sync) {
			open();
			JSONObject ret = new JSONObject();
			String limit = index + "," + 1;
			Cursor cursor = database.query(SKSQLiteHelper.TABLE_TESTBATCH,
					SKSQLiteHelper.TABLE_TESTBATCH_ALLCOLUMNS, null, null,
					null, null, SKSQLiteHelper.TEST_BATCH_ORDER, limit);
			if (cursor == null) {
				close();
				return null;
			}
			if (!cursor.moveToFirst()) {
				cursor.close();
				close();
				return null;
			}
			long test_batch_id = cursor.getLong(0);
			long test_batch_time = cursor.getLong(1);
			cursor.close();
			String selection = SKSQLiteHelper.TR_COLUMN_BATCH_ID + " = "
					+ test_batch_id;
			List<JSONObject> tests = getTestResults(selection);
			List<JSONObject> passive_metrics = getPassiveMetrics(test_batch_id);
			JSONArray j_tests = new JSONArray();
			JSONArray j_pm = new JSONArray();
			try {
				ret.put(ARCHIVEDATA_INDEX, index + "");
				ret.put(ARCHIVEDATA_DTIME, test_batch_time + "");
				for (JSONObject jo : tests) {
					j_tests.put(testResultToArchiveData(jo));
				}
				for (JSONObject jo : passive_metrics) {
					j_pm.put(passiveMetricToArchiveData(jo));
				}
				ret.put(ARCHIVEDATA_ACTIVEMETRICS, j_tests);
				ret.put(ARCHIVEDATA_PASSIVEMETRICS, j_pm);
			} catch (JSONException je) {
				SKLogger.e(DBHelper.class,
						"Error in converting tests and passive metrics for archive data"
								+ je.getMessage());
			}
			close();
			return ret;
		}
	}

	// Return a summary of the archive data
	public JSONObject getArchiveDataSummary() {
		synchronized (sync) {
			List<Integer> batches = getTestBatchesByPassiveMetric(getPassiveMetricsFilter());
			
			open();
			JSONObject ret = new JSONObject();
			// test batch counter
			String counterColumn = "COUNT(*)";
			String minDate = "MIN(" + SKSQLiteHelper.TB_COLUMN_DTIME + ")";
			String maxDate = "MAX(" + SKSQLiteHelper.TB_COLUMN_DTIME + ")";
			String[] columns = { counterColumn, minDate, maxDate };
			Cursor cursor = database.query(SKSQLiteHelper.TABLE_TESTBATCH,
					columns, null, null, null, null, null);
			cursor.moveToFirst();
			
			String counter = cursor.getLong(0) + "";
			String min = cursor.getLong(1) + "";
			String max = cursor.getLong(2) + "";
			cursor.close();
			// test results counter
			columns = new String[] { SKSQLiteHelper.TR_COLUMN_TYPE,
					counterColumn };
			String groupBy = SKSQLiteHelper.TR_COLUMN_TYPE;
			String selection = getInClause(SKSQLiteHelper.TR_COLUMN_BATCH_ID, batches);
			cursor = database.query(SKSQLiteHelper.TABLE_TESTRESULT, columns,
					selection, null, groupBy, null, null);
			cursor.moveToFirst();
			JSONObject test_counter = new JSONObject();
			while (!cursor.isAfterLast()) {
				try {
					test_counter
							.put(TestResult.testStringToId(cursor.getString(0))
									+ "", cursor.getInt(1) + "");
				} catch (JSONException je) {
					SKLogger.e(
							this,
							"Error in creating a JSONObject: "
									+ je.getMessage());
				}
				cursor.moveToNext();
			}
			cursor.close();
			try {
				ret.put(ARCHIVEDATASUMMARY_COUNTER, counter);
				ret.put(ARCHIVEDATASUMMARY_STARTDATE, min);
				ret.put(ARCHIVEDATASUMMARY_ENDDATE, max);
				ret.put(ARCHIVEDATASUMMARY_TESTCOUNTER, test_counter);
			} catch (JSONException je) {
				SKLogger.e(this,
						"Error in creating a JSONObject: " + je.getMessage());
			}
			close();
			return ret;
		}
	}

	public void insertTestBatch(JSONObject test_batch, JSONArray tests,
			JSONArray passive_metrics) {
		long test_batch_id;
		test_batch_id = insertTestBatch(test_batch);
		insertTestResult(tests, test_batch_id);
		insertPassiveMetric(passive_metrics, test_batch_id);
	}

	public void insertTestBatch(JSONObject test_batch, List<JSONObject> tests,
			List<JSONObject> passive_metrics) {
		long test_batch_id = insertTestBatch(test_batch);
		insertTestResult(tests, test_batch_id);
		insertPassiveMetric(passive_metrics, test_batch_id);
	}

	public long insertTestBatch(JSONObject test_batch) {
		long start_time;
		int run_manually;
		long ret = -1;
		try {
			start_time = test_batch.getLong(TestBatch.JSON_DTIME);
			run_manually = Integer.parseInt(test_batch
					.getString(TestBatch.JSON_RUNMANUALLY));
			ret = insertTestBatch(start_time, run_manually);
		} catch (JSONException je) {
			SKLogger.e(this, "Error in creating json object.", je);
		}
		return ret;
	}

	public long insertTestBatch(long start_time, int run_manually) {
		synchronized (sync) {
			open();
			ContentValues values = new ContentValues();
			values.put(SKSQLiteHelper.TB_COLUMN_DTIME, start_time);
			values.put(SKSQLiteHelper.TB_COLUMN_MANUAL, run_manually);
			long insertId = database.insert(SKSQLiteHelper.TABLE_TESTBATCH,
					null, values);
			close();
			return insertId;
		}
	}

	public void insertTestResult(List<JSONObject> tests, long test_batch_id) {
		for (JSONObject t : tests) {
			insertTestResult(t, test_batch_id);
		}
	}

	public void insertTestResult(JSONArray tests, long test_batch_id) {
		for (int i = 0; i < tests.length(); i++) {
			try {
				insertTestResult(tests.getJSONObject(i), test_batch_id);
			} catch (JSONException je) {
				SKLogger.e(DBHelper.class, "Error in converting JSONArray.", je);
			}
		}
	}

	public void insertTestResult(JSONObject test, long test_batch_id) {
		try {
			String type_name = test.getString(TestResult.JSON_TYPE_NAME);
			long dtime = test.getLong(TestResult.JSON_DTIME);
			long success = test.getLong(TestResult.JSON_SUCCESS);
			double result = test.getDouble(TestResult.JSON_RESULT);
			String location = test.getString(TestResult.JSON_LOCATION);
			insertTestResult(type_name, dtime, success, result, location,
					test_batch_id);
		} catch (JSONException je) {
			SKLogger.e(
					DBHelper.class,
					"Error in converting TestResult JSONObject in database entry.",
					je);
		}

	}

	private void insertTestResult(String type_name, long dtime, long success,
			double result, String location, long test_batch_id) {
		synchronized (sync) {
			open();
			ContentValues values = new ContentValues();
			values.put(SKSQLiteHelper.TR_COLUMN_DTIME, dtime);
			values.put(SKSQLiteHelper.TR_COLUMN_TYPE, type_name);
			values.put(SKSQLiteHelper.TR_COLUMN_LOCATION, location);
			values.put(SKSQLiteHelper.TR_COLUMN_SUCCESS, success);
			values.put(SKSQLiteHelper.TR_COLUMN_RESULT, result);
			values.put(SKSQLiteHelper.TR_COLUMN_BATCH_ID, test_batch_id);
			long id = database.insert(SKSQLiteHelper.TABLE_TESTRESULT, null,
					values);
			close();
		}
	}

	private void insertPassiveMetric(JSONArray metrics, long test_batch_id) {
		for (int i = 0; i < metrics.length(); i++) {
			try {
				insertPassiveMetric(metrics.getJSONObject(i), test_batch_id);
			} catch (JSONException je) {
				SKLogger.e(DBHelper.class,
						"Error in converting JSONArray: " + je.getMessage());
			}
		}
	}

	public void insertPassiveMetric(List<JSONObject> metrics, long test_batch_id) {
		for (JSONObject pm : metrics) {
			insertPassiveMetric(pm, test_batch_id);
		}
	}

	private void insertPassiveMetric(JSONObject metric, long test_batch_id) {
		String metric_type;
		long dtime;
		String value;
		String type;
		try {
			metric_type = metric.getString(PassiveMetric.JSON_METRIC_NAME);
			dtime = metric.getLong(PassiveMetric.JSON_DTIME);
			value = metric.getString(PassiveMetric.JSON_VALUE);
			type = metric.getString(PassiveMetric.JSON_TYPE);
			insertPassiveMetric(metric_type, type, dtime, value, test_batch_id);
		} catch (JSONException je) {
			SKLogger.e(
					DBHelper.class,
					"Error in converting JSONObject ot passive metric: "
							+ je.getMessage());
		}
	}

	private void insertPassiveMetric(String metric_type, String type,
			long dtime, String value, long test_batch_id) {
		synchronized (sync) {
			ContentValues values = new ContentValues();
			open();
			values.put(SKSQLiteHelper.PM_COLUMN_METRIC, metric_type);
			values.put(SKSQLiteHelper.PM_COLUMN_TYPE, type);
			values.put(SKSQLiteHelper.PM_COLUMN_DTIME, dtime);
			values.put(SKSQLiteHelper.PM_COLUMN_VALUE, value);
			values.put(SKSQLiteHelper.PM_COLUMN_BATCH_ID, test_batch_id);
			long id = database.insert(SKSQLiteHelper.TABLE_PASSIVEMETRIC, null,
					values);
			close();
		}
	}

	public void insertDataConsumption(TrafficData data){
		synchronized(sync){
			open();
			ContentValues values = new ContentValues();
			values.put(SKSQLiteHelper.DC_COLUMN_DTIME, data.time);
			values.put(SKSQLiteHelper.DC_COLUMN_MOBILERXBYTES, data.mobileRxBytes);
			values.put(SKSQLiteHelper.DC_COLUMN_MOBILETXBYTES, data.mobileTxBytes);
			values.put(SKSQLiteHelper.DC_COLUMN_TOTALRXBYTES, data.totalRxBytes);
			values.put(SKSQLiteHelper.DC_COLUMN_TOTALTXBYTES, data.totalTxBytes);
			values.put(SKSQLiteHelper.DC_COLUMN_APPRXBYTES, data.appRxBytes);
			values.put(SKSQLiteHelper.DC_COLUMN_APPTXBYTES, data.appTxBytes);
			long id = database.insert(SKSQLiteHelper.TABLE_DATACONSUMPTION, null, values);
			close();
		}
	}
	
	public boolean isTrafficDataAvailable(long millis){
		boolean ret = false;
		long start = System.currentTimeMillis() - millis;
		String selection = String.format("%s < %d", SKSQLiteHelper.DC_COLUMN_DTIME, start);
		synchronized(sync){
			open();
			Cursor cursor = database.query(SKSQLiteHelper.TABLE_DATACONSUMPTION, SKSQLiteHelper.TABLE_DATA_CONSUMPTION_ALLCOLUMNS, selection, null, null, null, null);
			if(cursor.getCount() > 0){
				ret = true;
			}
			cursor.close();
			close();
		}
		return ret;
		
	}
	
	public List<TrafficData> getTrafficData(){
		List<TrafficData> ret = new ArrayList<TrafficData>();
		synchronized(sync){
			open();
			Cursor cursor = database.query(SKSQLiteHelper.TABLE_DATACONSUMPTION, SKSQLiteHelper.TABLE_DATA_CONSUMPTION_ALLCOLUMNS, null, null, null, null, SKSQLiteHelper.DATACONSUMPTION_ORDER);
			cursor.moveToFirst();
			while(!cursor.isAfterLast()){
				TrafficData curr = new TrafficData();
				curr.time = cursor.getLong(1);
				curr.mobileRxBytes = cursor.getLong(2);
				curr.mobileTxBytes = cursor.getLong(3);
				curr.totalRxBytes = cursor.getLong(4);
				curr.totalTxBytes = cursor.getLong(5);
				curr.appRxBytes = cursor.getLong(6);
				curr.appTxBytes = cursor.getLong(7);
				cursor.moveToNext();
				ret.add(curr);
			}
			cursor.close();
			close();
		}
		return ret;
	}
	
	
	// Returns all the TestResult stored in the db
	public List<JSONObject> getAllTestResults() {
		return getTestResults();
	}

	// Returns all the TestResult stored in the db for a given test type
	public List<JSONObject> getAllTestResultsByType(String type) {
		String selection = String.format("%s = '%s'",
				SKSQLiteHelper.TR_COLUMN_TYPE, type);
		return getTestResults(selection);
	}

	public List<JSONObject> getTestResultByTypeAndInterval(String type,
			long starttime, long endtime) {
		String selection = String.format("%s = '%s' AND %s BETWEEN %d AND %d",
				SKSQLiteHelper.TR_COLUMN_TYPE, type,
				SKSQLiteHelper.TR_COLUMN_DTIME, starttime, endtime);
		List<Integer> batches = getTestBatchesByPassiveMetric(getPassiveMetricsFilter());
		if (batches == null || batches.size() == 0) {
			return new ArrayList<JSONObject>();
		}
		selection += " AND "
				+ getInClause(SKSQLiteHelper.TR_COLUMN_BATCH_ID, batches);
		return getTestResults(selection);
	}

	// Returns all the TestResult stored in the db run in an interval
	public List<JSONObject> getAllTestResultsInterval(long starttime,
			long endtime) {
		String selection = String.format("%s BETWEEN %d AND %d",
				SKSQLiteHelper.TR_COLUMN_DTIME, starttime, endtime);
		return getTestResults(selection);
	}

	// Returns n TestResult from the database for a given type after a specific
	// time
	/*
	 * public List<JSONObject> getTestResults(String type, long starttime, int
	 * n) { String selection = String.format("%s = '%s' AND %s >= %d",
	 * SKSQLiteHelper.TR_COLUMN_TYPE, type, SKSQLiteHelper.TR_COLUMN_DTIME,
	 * starttime); return getTestResults(selection, n + ""); }
	 */

	// Returns n TestResult the i-th result for a given type
	public List<JSONObject> getFilteredTestResults(String type, int startindex,
			int n) {
		String selection = String.format("%s = '%s'",
				SKSQLiteHelper.TR_COLUMN_TYPE, type);
		String limit = String.format("%d,%d", startindex, n);
		List<Integer> batches = getTestBatchesByPassiveMetric(getPassiveMetricsFilter());
		if (batches == null || batches.size() == 0) {
			return new ArrayList<JSONObject>();
		}
		selection += " AND "
				+ getInClause(SKSQLiteHelper.TR_COLUMN_BATCH_ID, batches);
		return getTestResults(selection, limit);
	}

	// Returns a JSONArray with the averages for the tests in the interval
	// between starttime and endtime
	// the average is computed only on successful tests
	public JSONArray getAverageResults(long starttime, long endtime,
			List<Integer> test_batches) {
		synchronized (sync) {
			open();
			JSONArray ret = new JSONArray();
			String selection = String.format(
					"dtime BETWEEN %d AND %d AND success <> 0", starttime,
					endtime);
			if (test_batches != null && test_batches.size() == 0) {
				return ret;
			}
			if (test_batches != null) {
				selection += " AND "
						+ getInClause(SKSQLiteHelper.TR_COLUMN_BATCH_ID,
								test_batches);
			}
			String averageColumn = String.format("AVG(%s)",
					SKSQLiteHelper.TR_COLUMN_RESULT);

			String[] columns = { SKSQLiteHelper.TR_COLUMN_TYPE, averageColumn,
					"COUNT(*)" };
			String groupBy = SKSQLiteHelper.TR_COLUMN_TYPE;
			Cursor cursor = database.query(SKSQLiteHelper.TABLE_TESTRESULT,
					columns, selection, null, groupBy, null, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {

				JSONObject curr = new JSONObject();
				try {
					int test_type_id = TestResult.testStringToId(cursor
							.getString(0));
					curr.put(AVERAGEDATA_TYPE, test_type_id + "");
					curr.put(
							AVERAGEDATA_VALUE,
							TestResult.hrResult(test_type_id,
									cursor.getDouble(1)));
				} catch (JSONException je) {

				}
				ret.put(curr);
				cursor.moveToNext();
			}
			cursor.close();
			close();
			return ret;
		}
	}

	//
	public JSONArray getAverageResults(long starttime, long endtime) {
		List<Integer> batches = getTestBatchesByPassiveMetric(starttime,
				endtime);
		return getAverageResults(starttime, endtime, batches);
	}

	private String getInClause(String field, List<Integer> values) {
		StringBuilder sb = new StringBuilder();
		sb.append(field).append(" IN (");
		for (Iterator<Integer> it = values.iterator(); it.hasNext();) {
			sb.append(it.next());
			if (it.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append(" )");
		return sb.toString();
	}

	// When retrieving averages, graph and grid data we have to filter by
	// Passive metric
	private String getPassiveMetricsFilter() {
		StringBuilder sb = new StringBuilder();
		sb.append(SKSQLiteHelper.PM_COLUMN_METRIC);
		sb.append("= '")
				.append(PassiveMetric.METRIC_TYPE.ACTIVENETWORKTYPE.metric_name)
				.append("'");
		sb.append(" AND ").append(SKSQLiteHelper.PM_COLUMN_VALUE)
				.append("= 'mobile' ");
		return sb.toString();
	}

	// Return a list of test batch ids with a passive metric value equal to
	// value in the specified period
	public List<Integer> getTestBatchesByPassiveMetric(String selection) {
		synchronized (sync) {
			open();
			List<Integer> ret = new ArrayList<Integer>();
			String[] columns = { SKSQLiteHelper.PM_COLUMN_BATCH_ID };
			Cursor cursor = database.query(SKSQLiteHelper.TABLE_PASSIVEMETRIC,
					columns, selection, null, SKSQLiteHelper.PM_COLUMN_BATCH_ID, null, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				ret.add(cursor.getInt(0));
				cursor.moveToNext();
			}
			cursor.close();
			close();
			return ret;
		}
	}

	public List<Integer> getTestBatchesByPassiveMetric(long start_time,
			long end_time) {

		String selection = String.format(SKSQLiteHelper.PM_COLUMN_DTIME
				+ " BETWEEN %d AND %d", start_time, end_time);
		selection += " AND " + getPassiveMetricsFilter();
		return getTestBatchesByPassiveMetric(selection);
	}

	private List<JSONObject> getTestResults() {
		return getTestResults(null, null);
	}

	private List<JSONObject> getTestResults(String selection) {
		return getTestResults(selection, null);
	}

	private List<JSONObject> getTestResults(String selection, String limit) {
		synchronized (sync) {
			open();
			List<JSONObject> ret = new ArrayList<JSONObject>();
			Cursor cursor = database.query(SKSQLiteHelper.TABLE_TESTRESULT,
					SKSQLiteHelper.TABLE_TESTRESULT_ALLCOLUMNS, selection,
					null, null, null, SKSQLiteHelper.TEST_RESULT_ORDER, limit);

			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				ret.add(cursorTestResultToJSONObject(cursor));
				cursor.moveToNext();
			}
			cursor.close();
			close();
			return ret;
		}
	}

	private List<JSONObject> getPassiveMetrics(long test_batch_id) {
		synchronized (sync) {
			List<JSONObject> ret = new ArrayList<JSONObject>();
			open();
			String selection = SKSQLiteHelper.PM_COLUMN_BATCH_ID + " = "
					+ test_batch_id;
			Cursor cursor = database.query(SKSQLiteHelper.TABLE_PASSIVEMETRIC,
					SKSQLiteHelper.TABLE_PASSIVEMETRIC_ALLCOLUMNS, selection,
					null, null, null, null, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				ret.add(cursorPassiveMetricToJSONObject(cursor));
				cursor.moveToNext();
			}
			cursor.close();
			close();
			return ret;
		}
	}

	// Translate a cursor to a TestResult JSONObject ready to be sent to the UI
	private JSONObject cursorTestResultToJSONObject(Cursor c) {
		JSONObject ret = new JSONObject();
		try {
			ret.put(SKSQLiteHelper.TR_COLUMN_ID, c.getLong(0));
			ret.put(SKSQLiteHelper.TR_COLUMN_TYPE, c.getString(1));
			ret.put(SKSQLiteHelper.TR_COLUMN_DTIME, c.getLong(2));
			ret.put(SKSQLiteHelper.TR_COLUMN_LOCATION, c.getString(3));
			ret.put(SKSQLiteHelper.TR_COLUMN_SUCCESS, c.getInt(4));
			ret.put(SKSQLiteHelper.TR_COLUMN_RESULT, c.getDouble(5));
			ret.put(SKSQLiteHelper.TR_COLUMN_BATCH_ID, c.getLong(6));
		} catch (JSONException je) {

		}
		return ret;
	}

	// Translate a cursor to a PassiveMetric JSONObject ready to be sent to the
	// UI
	private JSONObject cursorPassiveMetricToJSONObject(Cursor c) {
		JSONObject ret = new JSONObject();
		// PM_COLUMN_ID, PM_COLUMN_METRIC, PM_COLUMN_DTIME, PM_COLUMN_VALUE,
		// PM_COLUMN_TYPE, PM_COLUMN_BATCH_ID
		try {
			ret.put(SKSQLiteHelper.PM_COLUMN_ID, c.getLong(0));
			ret.put(SKSQLiteHelper.PM_COLUMN_METRIC, c.getString(1));
			ret.put(SKSQLiteHelper.PM_COLUMN_DTIME, c.getLong(2));
			ret.put(SKSQLiteHelper.PM_COLUMN_VALUE, c.getString(3));
			ret.put(SKSQLiteHelper.PM_COLUMN_TYPE, c.getString(4));
			ret.put(SKSQLiteHelper.PM_COLUMN_BATCH_ID, c.getLong(5));
		} catch (JSONException je) {
			SKLogger.e(DBHelper.class,
					"Error in converting passive metric entry into JSONObject"
							+ je.getMessage());
		}
		return ret;
	}

}
