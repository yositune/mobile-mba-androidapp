package com.samknows.measurement.activity;

import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.TableLayout;
import android.widget.TextView;

import com.samknows.libcore.SKLogger;
import com.samknows.measurement.FCCAppSettings;
import com.samknows.measurement.CachingStorage;
import com.samknows.measurement.ManualTest;
import com.samknows.measurement.Storage;
import com.samknows.measurement.R;
import com.samknows.measurement.activity.components.ProgressWheel;
import com.samknows.measurement.activity.components.Util;
import com.samknows.measurement.schedule.ScheduleConfig;
import com.samknows.measurement.schedule.TestDescription;
import com.samknows.measurement.storage.TestResult;

public class FCCRunningTestActivity extends BaseLogoutActivity {

	private Context cxt;
	public Handler handler;
	private ProgressWheel pw;
	private ManualTest mt;
	int page;
	int result = 0;

	Storage storage;
	ScheduleConfig config;

	List<TestDescription> testList;
	String array_spinner[];
	int array_spinner_int[];

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		cxt = this;

		Bundle b = getIntent().getExtras();
		int testID = -1;

		if (b != null) {
			testID = b.getInt("testID");
		}

		storage = CachingStorage.getInstance();
		config = storage.loadScheduleConfig();
		if (config == null) {
			config = new ScheduleConfig();
		}
		testList = config.manual_tests;
		array_spinner = new String[testList.size() + 1];
		array_spinner_int = new int[testList.size() + 1];

		this.setTitle(R.string.running_test);

		// choose which test to run
		setContentView(R.layout.fcc_running_test_activity);

		Util.initializeFonts(this);
		Util.overrideFonts(this, findViewById(android.R.id.content));
		try{
		
			handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				TextView tv = null;

				JSONObject message_json;
				message_json = (JSONObject) msg.obj;
				String value;
				int success;
				int testname;
				int status_complete;
				int metric;
				
				try {

					String type = message_json
							.getString(TestResult.JSON_TYPE_ID);

					if (type == "completed") {

						result = 1;
						FCCRunningTestActivity.this.finish();
						overridePendingTransition(0, 0);
					}

					if (type == "test") {
						testname = message_json
								.getInt(TestResult.JSON_TESTNUMBER);
						status_complete = message_json
								.getInt(TestResult.JSON_STATUS_COMPLETE);
						value = message_json
								.getString(TestResult.JSON_HRRESULT);
						if (status_complete == 100 && message_json.has(TestResult.JSON_SUCCESS)) {
							
							success = message_json
									.getInt(TestResult.JSON_SUCCESS);
							if (success == 0) {
								value = getString(R.string.failed);
							}
						}

						switch (testname) {
						// active metrics
						case TestResult.DOWNLOAD_TEST_ID:
							pw = (ProgressWheel) findViewById(R.id.ProgressWheel1);
							tv = (TextView) findViewById(R.id.download_result);
							pw.setProgress((int) (status_complete * 3.6));
							pw.setContentDescription("Status "
									+ status_complete + "%");
							if (status_complete == 100) {
								pw.setVisibility(View.GONE);
								tv.setText(value);
								tv.setContentDescription(getString(R.string.download)
										+ " " + value);
								tv.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
							} else {
								pw.setVisibility(View.VISIBLE);
								tv.setText("");
							}
							break;
						case TestResult.UPLOAD_TEST_ID:
							pw = (ProgressWheel) findViewById(R.id.ProgressWheel2);
							tv = (TextView) findViewById(R.id.upload_result);
							pw.setProgress((int) (status_complete * 3.6));
							pw.setContentDescription("Status "
									+ status_complete + "%");
							if (status_complete == 100) {
								pw.setVisibility(View.GONE);

								tv.setText(value);
								tv.setContentDescription(getString(R.string.upload)
										+ " " + value);
								tv.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
							} else {
								pw.setVisibility(View.VISIBLE);
								tv.setText("");
							}
							break;
						case TestResult.PACKETLOSS_TEST_ID:
							pw = (ProgressWheel) findViewById(R.id.ProgressWheel3);
							tv = (TextView) findViewById(R.id.packetloss_result);
							pw.setProgress((int) (status_complete * 3.6));
							pw.setContentDescription("Status "
									+ status_complete + "%");
							if (status_complete == 100) {
								pw.setVisibility(View.GONE);
								tv.setText(value);
								tv.setContentDescription(getString(R.string.packet_loss)
										+ " " + value);
								tv.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
							} else {
								pw.setVisibility(View.VISIBLE);
								tv.setText("");
							}
							break;
						case TestResult.LATENCY_TEST_ID:
							pw = (ProgressWheel) findViewById(R.id.ProgressWheel4);
							tv = (TextView) findViewById(R.id.latency_result);
							pw.setProgress((int) (status_complete * 3.6));
							pw.setContentDescription("Status "
									+ status_complete + "%");
							if (status_complete == 100) {
								pw.setVisibility(View.GONE);
								tv.setText(value);
								tv.setContentDescription(getString(R.string.latency)
										+ " " + value);
								tv.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
							} else {
								pw.setVisibility(View.VISIBLE);
								tv.setText("");
							}
							break;
						case TestResult.JITTER_TEST_ID:
							pw = (ProgressWheel) findViewById(R.id.ProgressWheel5);
							tv = (TextView) findViewById(R.id.jitter_result);
							pw.setProgress((int) (status_complete * 3.6));
							pw.setContentDescription("Status "
									+ status_complete + "%");
							if (status_complete == 100) {
								pw.setVisibility(View.GONE);
								tv.setText(value);
								tv.setContentDescription(getString(R.string.jitter)
										+ " " + value);
								tv.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
							} else {
								pw.setVisibility(View.VISIBLE);
								tv.setText("");
							}
							break;

						}
					}

					if (type == "passivemetric") {
						metric = message_json.getInt("metric");
						String metricString = message_json.getString("metricString");
						value = message_json.getString("value");
						
						if (metricString.equals("invisible")) {
						} else {
							// There is a complete disconnect between the integer metric value
							// returned from the PassiveMetric class, and the layout "passive metric" identifiers
							// such as R.id.passivemetric20.
							// The only safe thing to do, is to look at the metricString value,
							// to determine which resource id to use.
							Log.d(this.getClass().getName(), "metric=" + metric + ", metricString=" + metricString + ", value=" + value);
							if (metricString.equals("connected")) { // connected
								metric = 1;
							} else if (metricString.equals("connectivitytype")) { // connectivity
								metric = 2;
							} else if (metricString.equals("gsmcelltowerid")) { // cell tower id
								metric = 3;
							} else if (metricString.equals("gsmlocationareacode")) { // cell tower
								metric = 4;
							} else if (metricString.equals("gsmsignalstrength")) { // signal strength
								metric = 5;
							} else if (metricString.equals("networktype")) { // bearer
								metric = 6;
							} else if (metricString.equals("networkoperatorname")) { // network
								metric = 7;
							} else if (metricString.equals("latitude")) { // latitude
								metric = 8;
							} else if (metricString.equals("longitude")) { // longitude
								metric = 9;
							} else if (metricString.equals("accuracy")) { // accuracy
								metric = 10;
							} else if (metricString.equals("locationprovider")) { // location
								metric = 11;
							} else if (metricString.equals("simoperatorcode")) { // sim operator code
								metric = 12;
							} else if (metricString.equals("simoperatorname")) { // sim operator name
								metric = 13;
							} else if (metricString.equals("imei")) { // imei
								metric = 14;
							} else if (metricString.equals("imsi")) { // imsi
								metric = 15;
							} else if (metricString.equals("manufactor")) { // manufacter
								metric = 16;
							} else if (metricString.equals("model")) { // model
								metric = 17;
							} else if (metricString.equals("ostype")) { // os type
								metric = 18;
							} else if (metricString.equals("osversion")) { // os version
								metric = 19;
							} else if (metricString.equals("gsmbiterrorrate")) { // gsmbiterrorrate
								metric = 20;
							} else if (metricString.equals("cdmaecio")) { // cdmaecio
								metric = 21;
							} else if (metricString.equals("phonetype")) { // phone type
								metric = 22;
							} else if (metricString.equals("activenetworktype")) { // active network
								metric = 23;
							} else if (metricString.equals("connectionstatus")) { // connection
								metric = 24;
							} else if (metricString.equals("roamingstatus")) { // roaming status
								metric = 25;
							} else if (metricString.equals("networkoperatorcode")) { // network
								metric = 26;
							} else if (metricString.equals("cdmasignalstrength")) { // cdmasignalstrength
								metric = 27;
							} else if (metricString.equals("cdmabasestationid")) { // cdmabasestationid
								metric = 28;
							} else if (metricString.equals("cdmabasestationlatitude")) { // cdmabasestationlatitude
								metric = 29;
							} else if (metricString.equals("cdmabasestationlongitude")) { // cdmabasestationlongitude
								metric = 30;
							} else if (metricString.equals("cdmanetworkid")) { // cdmanetworkid
								metric = 31;
							} else if (metricString.equals("cdmasystemid")) { // cdmasystemid
								metric = 32;
							} else {
								Log.d(this.getClass().getName(), "WARNING - unsupported metric (" + metric +")");
							}
						
							// Prevent them display at ALL in the test view; to prevent
							// weird flickering!
							metric = -99;
						}


						switch (metric) {

						// passive metrics
						case 1:
							tv = (TextView) findViewById(R.id.passivemetric1);
							tv.setText(value);
							break;

						case 2:
							tv = (TextView) findViewById(R.id.passivemetric2);
							tv.setText(value);
							break;

						case 3:
							tv = (TextView) findViewById(R.id.passivemetric3);
							tv.setText(value);
							break;

						case 4:
							tv = (TextView) findViewById(R.id.passivemetric4);
							tv.setText(value);
							break;

						case 5:
							tv = (TextView) findViewById(R.id.passivemetric5);
							tv.setText(value);
							break;

						case 6:
							tv = (TextView) findViewById(R.id.passivemetric6);
							tv.setText(value);
							break;

						case 7:
							tv = (TextView) findViewById(R.id.passivemetric7);
							tv.setText(value);
							break;

						case 8:
							tv = (TextView) findViewById(R.id.passivemetric8);
							tv.setText(value);
							break;

						case 9:
							tv = (TextView) findViewById(R.id.passivemetric9);
							tv.setText(value);
							break;

						case 10:
							tv = (TextView) findViewById(R.id.passivemetric10);
							tv.setText(value);
							break;

						case 11:
							tv = (TextView) findViewById(R.id.passivemetric11);
							tv.setText(value);
							break;
						case 12:
							tv = (TextView) findViewById(R.id.passivemetric12);
							tv.setText(value);
							break;
						case 13:
							tv = (TextView) findViewById(R.id.passivemetric13);
							tv.setText(value);
							break;
						case 14:
							tv = (TextView) findViewById(R.id.passivemetric14);
							tv.setText(value);
							break;
						case 15:
							tv = (TextView) findViewById(R.id.passivemetric15);
							tv.setText(value);
							break;
						case 16:
							tv = (TextView) findViewById(R.id.passivemetric16);
							tv.setText(value);
							break;
						case 17:
							tv = (TextView) findViewById(R.id.passivemetric17);
							tv.setText(value);
							break;
						case 18:
							tv = (TextView) findViewById(R.id.passivemetric18);
							tv.setText(value);
							break;
						case 19:
							tv = (TextView) findViewById(R.id.passivemetric19);
							tv.setText(value);
							break;
						case 20:
							tv = (TextView) findViewById(R.id.passivemetric20);
							tv.setText(value);
							break;
						case 21:
							tv = (TextView) findViewById(R.id.passivemetric21);
							tv.setText(value);
							break;
						case 22:
							tv = (TextView) findViewById(R.id.passivemetric22);
							tv.setText(value);
							break;
						case 23:
							tv = (TextView) findViewById(R.id.passivemetric23);
							tv.setText(value);
							break;
						case 24:
							tv = (TextView) findViewById(R.id.passivemetric24);
							tv.setText(value);
							break;
						case 25:
							tv = (TextView) findViewById(R.id.passivemetric25);
							tv.setText(value);
							break;
						case 26:
							tv = (TextView) findViewById(R.id.passivemetric26);
							tv.setText(value);
							break;
						case 27:
							tv = (TextView) findViewById(R.id.passivemetric27);
							tv.setText(value);
							break;
						case 28:
							tv = (TextView) findViewById(R.id.passivemetric28);
							tv.setText(value);
							break;
						case 29:
							tv = (TextView) findViewById(R.id.passivemetric29);
							tv.setText(value);
							break;
						case 30:
							tv = (TextView) findViewById(R.id.passivemetric30);
							tv.setText(value);
							break;
						case 31:
							tv = (TextView) findViewById(R.id.passivemetric31);
							tv.setText(value);
							break;
						case 32:
							tv = (TextView) findViewById(R.id.passivemetric32);
							tv.setText(value);
							break;
						default:
							//

						}
						if (!value.equals("") && tv != null) {

							TableLayout tl1 = (TableLayout) findViewById(R.id.passive_metrics_status);
							tl1.setVisibility(View.GONE);
							TableLayout tl = (TableLayout) tv.getParent()
									.getParent();
							tl.setVisibility(View.VISIBLE);
						}

						if (value.equals("") && tv != null) {
							TableLayout tl = (TableLayout) tv.getParent()
									.getParent();
							tl.setVisibility(View.GONE);
						}

					}

				} catch (JSONException e) {
					SKLogger.e(this, e.getMessage());
				}
			}
		};

		launchTest(testID);
		}catch(Throwable t){
			SKLogger.e(this, "handler or test failure", t);
		}
	}

	@Override
	public void finish() {

		Intent returnIntent = new Intent();

		if (result == 0) {
			// setResult(RESULT_OK,returnIntent);
		} else {
			setResult(RESULT_OK, returnIntent);
		}
		super.finish();

	}

	@Override
	protected void onStart() {
		super.onStart();

		// make passive metrics invisible

		for (int x = 1; x < 33; x = x + 1) {
			Message msg = new Message();
			JSONObject jtc = new JSONObject();
			try {
				jtc.put("type", "passivemetric");
				jtc.put("metric", "" + x);
				jtc.put("metricString", "invisible");
				jtc.put("value", "");
				msg.obj = jtc;
			} catch (JSONException je) {
				SKLogger.e(this,
						"Error in creating JSONObject:" + je.getMessage());
			}
			handler.sendMessage(msg);
		}

	}

	private void launchTest(int testID) {

	
		StringBuilder errorDescription = new StringBuilder();
		
		boolean run = true;
		// create a new thread
		if (testID != -1) {
			mt = ManualTest.create(this, handler, testID, errorDescription);

			if (testID == 2) { // download
				// hide others
				TableLayout tl = (TableLayout) findViewById(R.id.upload_test_panel);
				tl.setVisibility(View.GONE);
				TableLayout tl2 = (TableLayout) findViewById(R.id.latency_test_panel);
				tl2.setVisibility(View.GONE);
				TableLayout tl3 = (TableLayout) findViewById(R.id.packetloss_test_panel);
				tl3.setVisibility(View.GONE);
			}

			if (testID == 4) { // loss / latency
				// hide others
				TableLayout tl = (TableLayout) findViewById(R.id.download_test_panel);
				tl.setVisibility(View.GONE);
				TableLayout tl2 = (TableLayout) findViewById(R.id.upload_test_panel);
				tl2.setVisibility(View.GONE);
			}

			if (testID == 3) { // upload
				// hide others
				TableLayout tl = (TableLayout) findViewById(R.id.download_test_panel);
				tl.setVisibility(View.GONE);
				TableLayout tl2 = (TableLayout) findViewById(R.id.latency_test_panel);
				tl2.setVisibility(View.GONE);
				TableLayout tl3 = (TableLayout) findViewById(R.id.packetloss_test_panel);
				tl3.setVisibility(View.GONE);
			}

		} else {
			mt = ManualTest.create(this, handler, errorDescription);
		}

		if (mt == null) {
			String theErrorString = errorDescription.toString();
			if (theErrorString.length() == 0) {
				theErrorString = getString(R.string.manual_test_error);
			}
			
			SKLogger.d(FCCRunningTestActivity.class,
					"Impossible to run manual tests");
			new AlertDialog.Builder(this)
					.setMessage(theErrorString)
					.setPositiveButton(R.string.ok_dialog,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									result = 0;
									FCCRunningTestActivity.this.finish();
									overridePendingTransition(0, 0);
								}
							}).show();

		} else if (FCCAppSettings.getFCCAppSettingsInstance().isDataCapReached(mt.getNetUsage())) {
			SKLogger.d(FCCRunningTestActivity.class, "Data cap exceeded");
			new AlertDialog.Builder(this)
					.setMessage(getString(R.string.data_cap_exceeded))
					.setPositiveButton(R.string.ok_dialog,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									startTest(mt);
								}
							})
					.setNegativeButton(R.string.no_dialog,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									result = 0;
									FCCRunningTestActivity.this.finish();
									overridePendingTransition(0, 0);
								}
							}).show();
		} else {
			startTest(mt);
		}
	}

	private void startTest(ManualTest mt) {
		new Thread(mt).start();
	}

	private void SingleChoice() {
		Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.choose_test));
		// dropdown setup

		for (int i = 0; i < testList.size(); i++) {
			TestDescription td = testList.get(i);
			array_spinner[i] = td.displayName;
			array_spinner_int[i] = td.testId;
		}
		array_spinner[testList.size()] = "All";
		array_spinner_int[testList.size()] = -1;

		builder.setItems(array_spinner, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.e("", "Launch Id=" + array_spinner_int[which]);
				dialog.dismiss();
				launchTest(array_spinner_int[which]);
			}
		});
		builder.setNegativeButton(getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						FCCRunningTestActivity.this.finish();
						overridePendingTransition(0, 0);
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	public void onBackPressed() {
		
		if (this.wouldBackButtonReturnMeToTheHomeScreen()) {
			super.onBackPressed();
			return;
		}
		
		new AlertDialog.Builder(this)
				.setMessage(getString(R.string.cancel_test_question))
				.setCancelable(true)
				.setPositiveButton(getString(R.string.yes),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								result = 0;
								if (mt == null) {
					              // Log.d(this.getClass().getName(), "Avoided null pointer exception!");
								} else {
									mt.stop();
								}
								FCCRunningTestActivity.this.finish();
								overridePendingTransition(0, 0);
							}
						})
				.setNegativeButton(getString(R.string.no_dialog),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

							}
						}).show();
	}

}