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


package com.samknows.measurement.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import com.samknows.measurement.AppSettings;
import com.samknows.measurement.CachingStorage;
import com.samknows.measurement.Constants;
import com.samknows.measurement.DeviceDescription;
import com.samknows.measurement.Logger;
import com.samknows.measurement.MainService;
import com.samknows.measurement.R;
import com.samknows.measurement.SamKnowsLoginService;
import com.samknows.measurement.SamKnowsResponseHandler;
import com.samknows.measurement.Storage;
import com.samknows.measurement.activity.components.ResizeAnimation;
import com.samknows.measurement.activity.components.SamKnowsGraph;
import com.samknows.measurement.activity.components.ServiceStatusDisplayManager;
import com.samknows.measurement.activity.components.StatModel;
import com.samknows.measurement.activity.components.StatRecord;
import com.samknows.measurement.activity.components.StatView;
import com.samknows.measurement.activity.components.UpdatedTextView;
import com.samknows.measurement.activity.components.Util;
import com.samknows.measurement.schedule.ScheduleConfig;
import com.samknows.measurement.schedule.TestDescription;
import com.samknows.measurement.storage.DBHelper;
import com.samknows.measurement.storage.TestResult;
import com.samknows.measurement.util.LoginHelper;
import com.samknows.measurement.util.OtherUtils;
import com.samknows.measurement.util.SKDateFormat;

public class SamKnowsAggregateStatViewerActivity extends BaseLogoutActivity
		implements OnClickListener {

	// use to decide when to show the state_machine_status_failure

	WebView graph1;
	WebView graph2;
	WebView graph3;
	WebView graph4;
	WebView graph5;
	SamKnowsGraph graphHandler1;
	SamKnowsGraph graphHandler2;
	SamKnowsGraph graphHandler3;
	SamKnowsGraph graphHandler4;
	SamKnowsGraph graphHandler5;
	int download_page_index = 0;
	int upload_page_index = 0;
	int latency_page_index = 0;
	int packetloss_page_index = 0;
	int jitter_page_index = 0;

	private static final String TAG = SamKnowsAggregateStatViewerActivity.class
			.getSimpleName();
	public static final String SETTINGS = "SamKnows";
	private static final int PANEL_HEIGHT = 550;
	private final Context context = this;
	private SamKnowsLoginService service = new SamKnowsLoginService();

	private StatModel statModel = new StatModel();
	private JSONObject data;
	private String start_date;
	private JSONObject recentData;
	// private DeviceDescription device;
	// private boolean isCurrentDevice;

	private ServiceStatusDisplayManager serviceDisplay;

	public static final int RECENT = 0;
	public static final int WEEK = 1;
	public static final int MONTH = 2;
	public static final int THREE_MONTHS = 3;
	public static final int SIX_MONTHS = 4;
	public static final int YEAR = 5;

	private static final int ITEMS_PER_PAGE = 5;

	private boolean isDisplayingContent = false;

	private int[] latest;
	private int[] rows;
	private TableLayout table;
	private int[] buttons;

	private long start_dtime = 0;
	private long end_dtime = 0;

	private UpdatedTextView updated;

	View.OnTouchListener gestureListener;
	private GestureDetector gestureDetector;
	DeviceDescription currentDevice;
	private View subview;
	private boolean first_time = true;
	private int total_archive_records = 0;
	private int total_download_archive_records = 0;
	private int total_upload_archive_records = 0;
	private int total_latency_archive_records = 0;
	private int total_packetloss_archive_records = 0;
	private int total_jitter_archive_records = 0;

	private DBHelper dbHelper;
	private DBHelper dbHelperAsync;
	private asyncReadData dataReader;

	private MyPagerAdapter adapter;
	private ViewPager viewPager;
	private View current_page_view;
	private int current_page_view_position = 0;

	private String last_run_test_formatted;
	private int target_height = 0;
	private boolean on_aggregate_page = true;

	Storage storage;
	ScheduleConfig config;

	List<TestDescription> testList;
	String array_spinner[];
	int array_spinner_int[];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		 * device = (DeviceDescription) getIntent().getSerializableExtra(
		 * Constants.INTENT_EXTRA_DEVICE); isCurrentDevice =
		 * getIntent().getBooleanExtra(
		 * Constants.INTENT_EXTRA_IS_CURRENT_DEVICE, false);
		 * 
		 * List<DeviceDescription> devices = AppSettings.getInstance()
		 * .getDevices(); String imei =
		 * PhoneIdentityDataCollector.getImei(this); currentDevice = new
		 * CurrentDeviceDescription(imei); OtherUtils.removeDeviceForImei(imei,
		 * devices);
		 */

		this.setTitle(getString(R.string.average_results_title));

		setContentView(R.layout.page_views);

		dbHelper = new DBHelper(SamKnowsAggregateStatViewerActivity.this);
		dbHelperAsync = new DBHelper(SamKnowsAggregateStatViewerActivity.this);
		adapter = new MyPagerAdapter(this);

		viewPager = (ViewPager) findViewById(R.id.viewPager);

		viewPager.setAdapter(adapter);
		// viewPager.setOffscreenPageLimit(3);

		final TextView tvHeader = (TextView) findViewById(R.id.textViewHeader);

		viewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int page) {
				tvHeader.setText(getString(R.string.page) + " " + (page + 1));
				if (page == 0) {
					on_aggregate_page = true;
					boolean db_refresh = false;

					SamKnowsAggregateStatViewerActivity.this
							.setTitle(getString(R.string.average_results));

					TextView timestamp;
					View v;
					v = viewPager.findViewWithTag(page);

					timestamp = (TextView) v
							.findViewById(R.id.average_results_title);

					timestamp
							.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);

					if (setTotalArchiveRecords()) {
						adapter = new MyPagerAdapter(
								SamKnowsAggregateStatViewerActivity.this);
						viewPager = (ViewPager) findViewById(R.id.viewPager);
						viewPager.setAdapter(adapter);
					}

				} else {
					TextView timestamp;
					View v;
					v = viewPager.findViewWithTag(page);

					timestamp = (TextView) v.findViewById(R.id.timestamp);
					timestamp
							.setContentDescription(getString(R.string.archive_result)
									+ " " + timestamp.getText());
					timestamp
							.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
					on_aggregate_page = false;
					SamKnowsAggregateStatViewerActivity.this
							.setTitle(getString(R.string.archive_result));
				}
			}

			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {

			}

			@Override
			public void onPageScrollStateChanged(int state) {
				if (state == ViewPager.SCROLL_STATE_SETTLING) {

				}

			}
		});

		Util.initializeFonts(this);
		Util.overrideFonts(this, findViewById(android.R.id.content));

	}

	@Override
	public void onResume() {
		super.onResume(); // Always call the superclass method first

		if (setTotalArchiveRecords()) {
			adapter = new MyPagerAdapter(
					SamKnowsAggregateStatViewerActivity.this);
			viewPager = (ViewPager) findViewById(R.id.viewPager);
			viewPager.setAdapter(adapter);
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			// refresh data
			adapter = new MyPagerAdapter(this);
			viewPager = (ViewPager) findViewById(R.id.viewPager);
			viewPager.setAdapter(adapter);
			viewPager.setCurrentItem(1, false);
			overridePendingTransition(0, 0);
		}

		/*
		 * if (resultCode>0){
		 * 
		 * //adapter.instantiateItem(viewPager, resultCode);
		 * //adapter.instantiateItem(viewPager, resultCode-1);
		 * 
		 * viewPager.setCurrentItem(resultCode, false);
		 * adapter.notifyDataSetChanged(); overridePendingTransition(0, 0); }
		 */
	}

	private void loadAverage(int num_weeks) {
		JSONArray jsonResult;
		Calendar now = Calendar.getInstance();

		long current_dtime = now.getTimeInMillis();
		now.add(Calendar.WEEK_OF_YEAR, num_weeks * -1);
		long starting_dtime = now.getTimeInMillis();

		jsonResult = dbHelper.getAverageResults(starting_dtime, current_dtime);

		String result = "";

		for (int i = 0; i < jsonResult.length(); i++) {
			try {
				JSONObject json_data = jsonResult.getJSONObject(i);
				String value = json_data.getString("value");
				String type = json_data.getString("type");

				if (type.equals("" + TestResult.DOWNLOAD_TEST_ID)) {
					((TextView) subview.findViewById(R.id.download_average))
							.setText("" + value);
				}
				if (type.equals("" + TestResult.UPLOAD_TEST_ID)) {
					((TextView) subview.findViewById(R.id.upload_average))
							.setText("" + value);
				}
				if (type.equals("" + TestResult.LATENCY_TEST_ID)) {
					((TextView) subview.findViewById(R.id.latency_average))
							.setText("" + value);
				}
				if (type.equals("" + TestResult.PACKETLOSS_TEST_ID)) {
					((TextView) subview.findViewById(R.id.packetloss_average))
							.setText("" + value);
				}
				if (type.equals("" + TestResult.JITTER_TEST_ID)) {
					((TextView) subview.findViewById(R.id.jitter_average))
							.setText("" + value);
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

	}

	public boolean setTotalArchiveRecords() {
		boolean result = false;

		JSONObject summary = dbHelper.getArchiveDataSummary();

		JSONObject results = null;
		int itemcount = 0;

		try {
			results = summary.getJSONObject("test_counter");

		} catch (JSONException e) {

			e.printStackTrace();
		}

		try {
			if (results.has("" + TestResult.DOWNLOAD_TEST_ID)) {
				total_download_archive_records = results.getInt(""
						+ TestResult.DOWNLOAD_TEST_ID);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			if (results.has("" + TestResult.UPLOAD_TEST_ID)) {
				total_upload_archive_records = results.getInt(""
						+ TestResult.UPLOAD_TEST_ID);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			if (results.has("" + TestResult.LATENCY_TEST_ID)) {
				total_latency_archive_records = results.getInt(""
						+ TestResult.LATENCY_TEST_ID);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			if (results.has("" + TestResult.JITTER_TEST_ID)) {
				total_jitter_archive_records = results.getInt(""
						+ TestResult.JITTER_TEST_ID);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			if (results.has("" + TestResult.PACKETLOSS_TEST_ID)) {
				total_packetloss_archive_records = results.getInt(""
						+ TestResult.PACKETLOSS_TEST_ID);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			int records = summary.getInt("counter");
			if (total_archive_records != records) {
				total_archive_records = records;
				result = true;
			}

			String last_run_test = summary.getString("enddate");
			long last_run_test_l = Long.parseLong(last_run_test);
			if (last_run_test_l != 0) {
				last_run_test_formatted = new SKDateFormat(context)
						.UIDate(last_run_test_l);
			} else {
				last_run_test_formatted = "";
			}

		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		return result;
	}

	private void loadDownloadGrid(int testnumber, int grid, int offset,
			int limit) {

		double a1 = (double) total_archive_records;
		double a2 = (double) ITEMS_PER_PAGE;
		int pages = (int) Math.ceil(a1 / a2);
		int page_number = (offset + ITEMS_PER_PAGE) / ITEMS_PER_PAGE;

		TextView tv;

		switch (testnumber) {
		case TestResult.DOWNLOAD_TEST_ID:
			a1 = (double) total_download_archive_records;
			pages = (int) Math.ceil(a1 / a2);

			tv = (TextView) subview.findViewById(R.id.download_pagenumber);
			tv.setText(getString(R.string.page) + " " + page_number + " "
					+ getString(R.string.of) + " " + pages);
			break;

		case TestResult.UPLOAD_TEST_ID:
			a1 = (double) total_upload_archive_records;
			pages = (int) Math.ceil(a1 / a2);
			tv = (TextView) subview.findViewById(R.id.upload_pagenumber);
			tv.setText(getString(R.string.page) + " " + page_number + " "
					+ getString(R.string.of) + " " + pages);
			break;

		case TestResult.LATENCY_TEST_ID:
			a1 = (double) total_latency_archive_records;
			pages = (int) Math.ceil(a1 / a2);
			tv = (TextView) subview.findViewById(R.id.latency_pagenumber);
			tv.setText(getString(R.string.page) + " " + page_number + " "
					+ getString(R.string.of) + " " + pages);
			break;

		case TestResult.PACKETLOSS_TEST_ID:
			a1 = (double) total_packetloss_archive_records;
			pages = (int) Math.ceil(a1 / a2);
			tv = (TextView) subview.findViewById(R.id.packetloss_pagenumber);
			tv.setText(getString(R.string.page) + " " + page_number + " "
					+ getString(R.string.of) + " " + pages);
			break;

		case TestResult.JITTER_TEST_ID:
			a1 = (double) total_jitter_archive_records;
			pages = (int) Math.ceil(a1 / a2);
			tv = (TextView) subview.findViewById(R.id.jitter_pagenumber);
			tv.setText(getString(R.string.page) + " " + page_number + " "
					+ getString(R.string.of) + " " + pages);
			break;

		default:

		}

		addGridItemHeader(getString(R.string.date), getString(R.string.server),
				getString(R.string.result), grid);

		JSONObject jsonObject;

		jsonObject = dbHelper.getGridData(testnumber, offset, limit);
		// jsonObject=dbHelper.getGridData(1, offset, limit);

		JSONArray results = null;
		try {
			results = jsonObject.getJSONArray("results");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String location;
		String dtime;
		String dtime_formatted = null;
		String result;
		String success;

		for (int i = 0; i < results.length(); i++) {
			location = "";
			dtime = "";
			result = "";
			success = "";
			JSONObject user = null;
			try {
				user = results.getJSONObject(i);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			try {
				success = user.getString("success");
				location = user.getString("location");
				dtime = user.getString("dtime");
				if (dtime != "") {

					long datelong = Long.parseLong(dtime);
					if (datelong != 0) {
						dtime_formatted = new SKDateFormat(context)
								.UIDate(datelong);
					}

				} else {
					dtime_formatted = "";
				}
				result = user.getString("hrresult");

			} catch (JSONException e) {
				e.printStackTrace();
			}

			if (success.equals("1")) {
				addGridItem(dtime_formatted, location, result, grid);
			} else {
				result = getString(R.string.failed);
				addGridItemFailed(dtime_formatted, location, result, grid);
			}
		}

		Util.overrideFonts(this, findViewById(android.R.id.content));

	}

	private void buttonSetup() {

		// button setup

		Button execute_button;
		execute_button = (Button) subview.findViewById(R.id.btnRunTest);
		execute_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(
						SamKnowsAggregateStatViewerActivity.this,
						SamKnowsTestViewerActivity.class);

				startActivityForResult(intent, 1);
				overridePendingTransition(R.anim.transition_in,
						R.anim.transition_out);

			}
		});

		Button run_now_choice_button;
		run_now_choice_button = (Button) findViewById(R.id.btnRunTestChoice);

		run_now_choice_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				RunChoice();
			}
		});

		Button timeperiod_button;
		timeperiod_button = (Button) findViewById(R.id.btn_timeperiod);

		timeperiod_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SingleChoice();
			}
		});

		LinearLayout timeperiod_button2;
		timeperiod_button2 = (LinearLayout) findViewById(R.id.timeperiod_header);

		timeperiod_button2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SingleChoice();
			}
		});

		// page turn navigation button
		ImageView page_right = (ImageView) findViewById(R.id.page_turn_right_agg);

		page_right.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				viewPager = (ViewPager) findViewById(R.id.viewPager);
				viewPager.setCurrentItem(1, true);
			}
		});

		// grid navigation buttons

		Button download_grid_right_button;
		download_grid_right_button = (Button) subview
				.findViewById(R.id.btn_download_grid_right);
		download_grid_right_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (download_page_index < total_download_archive_records
						- ITEMS_PER_PAGE) {
					download_page_index = download_page_index + ITEMS_PER_PAGE;
				}

				clearGrid(R.id.agggregate_test1_grid);
				loadDownloadGrid(TestResult.DOWNLOAD_TEST_ID,
						R.id.agggregate_test1_grid, download_page_index,
						ITEMS_PER_PAGE);

			}
		});

		Button download_grid_left_button;
		download_grid_left_button = (Button) subview
				.findViewById(R.id.btn_download_grid_left);
		download_grid_left_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (download_page_index > 0) {
					download_page_index = download_page_index - ITEMS_PER_PAGE;
				}
				clearGrid(R.id.agggregate_test1_grid);
				loadDownloadGrid(TestResult.DOWNLOAD_TEST_ID,
						R.id.agggregate_test1_grid, download_page_index,
						ITEMS_PER_PAGE);

			}
		});

		Button upload_grid_right_button;
		upload_grid_right_button = (Button) subview
				.findViewById(R.id.btn_upload_grid_right);
		upload_grid_right_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (upload_page_index < total_upload_archive_records
						- ITEMS_PER_PAGE) {
					upload_page_index = upload_page_index + ITEMS_PER_PAGE;
				}

				clearGrid(R.id.agggregate_test2_grid);
				loadDownloadGrid(TestResult.UPLOAD_TEST_ID,
						R.id.agggregate_test2_grid, upload_page_index,
						ITEMS_PER_PAGE);

			}
		});

		Button upload_grid_left_button;
		upload_grid_left_button = (Button) subview
				.findViewById(R.id.btn_upload_grid_left);
		upload_grid_left_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (upload_page_index > 0) {
					upload_page_index = upload_page_index - ITEMS_PER_PAGE;
				}
				clearGrid(R.id.agggregate_test2_grid);
				loadDownloadGrid(TestResult.UPLOAD_TEST_ID,
						R.id.agggregate_test2_grid, upload_page_index,
						ITEMS_PER_PAGE);

			}
		});

		Button latency_grid_right_button;
		latency_grid_right_button = (Button) subview
				.findViewById(R.id.btn_latency_grid_right);
		latency_grid_right_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (latency_page_index < total_latency_archive_records
						- ITEMS_PER_PAGE) {
					latency_page_index = latency_page_index + ITEMS_PER_PAGE;
				}

				clearGrid(R.id.agggregate_test3_grid);
				loadDownloadGrid(TestResult.LATENCY_TEST_ID,
						R.id.agggregate_test3_grid, latency_page_index,
						ITEMS_PER_PAGE);

			}
		});

		Button latency_grid_left_button;
		latency_grid_left_button = (Button) subview
				.findViewById(R.id.btn_latency_grid_left);
		latency_grid_left_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (latency_page_index > 0) {
					latency_page_index = latency_page_index - ITEMS_PER_PAGE;
				}
				clearGrid(R.id.agggregate_test3_grid);
				loadDownloadGrid(TestResult.LATENCY_TEST_ID,
						R.id.agggregate_test3_grid, latency_page_index,
						ITEMS_PER_PAGE);

			}
		});

		Button packetloss_grid_right_button;
		packetloss_grid_right_button = (Button) subview
				.findViewById(R.id.btn_packetloss_grid_right);
		packetloss_grid_right_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (packetloss_page_index < total_packetloss_archive_records
						- ITEMS_PER_PAGE) {
					packetloss_page_index = packetloss_page_index
							+ ITEMS_PER_PAGE;
				}

				clearGrid(R.id.agggregate_test4_grid);
				loadDownloadGrid(TestResult.PACKETLOSS_TEST_ID,
						R.id.agggregate_test4_grid, packetloss_page_index,
						ITEMS_PER_PAGE);

			}
		});

		Button packetloss_grid_left_button;
		packetloss_grid_left_button = (Button) subview
				.findViewById(R.id.btn_packetloss_grid_left);
		packetloss_grid_left_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (packetloss_page_index > 0) {
					packetloss_page_index = packetloss_page_index
							- ITEMS_PER_PAGE;
				}
				clearGrid(R.id.agggregate_test4_grid);
				loadDownloadGrid(TestResult.PACKETLOSS_TEST_ID,
						R.id.agggregate_test4_grid, packetloss_page_index,
						ITEMS_PER_PAGE);

			}
		});

		Button jitter_grid_right_button;
		jitter_grid_right_button = (Button) subview
				.findViewById(R.id.btn_jitter_grid_right);
		jitter_grid_right_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (jitter_page_index < total_jitter_archive_records
						- ITEMS_PER_PAGE) {
					jitter_page_index = jitter_page_index + ITEMS_PER_PAGE;
				}

				clearGrid(R.id.agggregate_test5_grid);
				loadDownloadGrid(TestResult.JITTER_TEST_ID,
						R.id.agggregate_test5_grid, jitter_page_index,
						ITEMS_PER_PAGE);

			}
		});

		Button jitter_grid_left_button;
		jitter_grid_left_button = (Button) subview
				.findViewById(R.id.btn_jitter_grid_left);
		jitter_grid_left_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (jitter_page_index > 0) {
					jitter_page_index = jitter_page_index - ITEMS_PER_PAGE;
				}
				clearGrid(R.id.agggregate_test5_grid);
				loadDownloadGrid(TestResult.JITTER_TEST_ID,
						R.id.agggregate_test5_grid, jitter_page_index,
						ITEMS_PER_PAGE);

			}
		});

		// toggle buttons

		TableLayout button;
		button = (TableLayout) findViewById(R.id.download_header);
		ImageView button_iv = (ImageView) findViewById(R.id.btn_download_toggle);

		button.setOnClickListener(this);
		button_iv.setOnClickListener(this);

		TableLayout button2;
		button2 = (TableLayout) subview.findViewById(R.id.upload_header);
		ImageView button2_iv = (ImageView) findViewById(R.id.btn_upload_toggle);

		button2.setOnClickListener(this);
		button2_iv.setOnClickListener(this);

		TableLayout button3;
		button3 = (TableLayout) subview.findViewById(R.id.latency_header);
		ImageView button3_iv = (ImageView) findViewById(R.id.btn_latency_toggle);

		button3.setOnClickListener(this);
		button3_iv.setOnClickListener(this);

		TableLayout button4;
		button4 = (TableLayout) subview.findViewById(R.id.packetloss_header);
		ImageView button4_iv = (ImageView) findViewById(R.id.btn_packetloss_toggle);

		button4.setOnClickListener(this);
		button4_iv.setOnClickListener(this);

		TableLayout button5;
		button5 = (TableLayout) subview.findViewById(R.id.jitter_header);
		ImageView button5_iv = (ImageView) findViewById(R.id.btn_jitter_toggle);

		button5.setOnClickListener(this);
		button5_iv.setOnClickListener(this);

	}

	private void addGridItemHeader(String timestamp, String location,
			String result, int grid) {
		TableLayout table = (TableLayout) findViewById(grid);
		TableLayout row = (TableLayout) LayoutInflater.from(
				SamKnowsAggregateStatViewerActivity.this).inflate(
				R.layout.stat_grid_header, null);

		((TextView) row.findViewById(R.id.stat_grid_timestamp))
				.setText(timestamp);
		((TextView) row.findViewById(R.id.stat_grid_location))
				.setText(location);
		((TextView) row.findViewById(R.id.stat_grid_result)).setText(result);

		table.addView(row);
	}

	private void addGridItem(String timestamp, String location, String result,
			int grid) {
		TableLayout table = (TableLayout) findViewById(grid);
		TableLayout row = (TableLayout) LayoutInflater.from(
				SamKnowsAggregateStatViewerActivity.this).inflate(
				R.layout.stat_grid, null);

		((TextView) row.findViewById(R.id.stat_grid_timestamp))
				.setText(timestamp);
		((TextView) row.findViewById(R.id.stat_grid_location))
				.setText(location);
		((TextView) row.findViewById(R.id.stat_grid_result)).setText(result);

		table.addView(row);
	}

	private void addGridItemFailed(String timestamp, String location,
			String result, int grid) {
		TableLayout table = (TableLayout) findViewById(grid);
		TableLayout row = (TableLayout) LayoutInflater.from(
				SamKnowsAggregateStatViewerActivity.this).inflate(
				R.layout.stat_grid_fail, null);

		((TextView) row.findViewById(R.id.stat_grid_timestamp))
				.setText(timestamp);
		((TextView) row.findViewById(R.id.stat_grid_location))
				.setText(location);
		((TextView) row.findViewById(R.id.stat_grid_result)).setText(result);

		table.addView(row);
	}

	private void clearGrid(int grid) {
		TableLayout table = (TableLayout) findViewById(grid);
		int count = table.getChildCount();
		for (int i = 0; i < count; i++) {
			View child = table.getChildAt(i);
			((ViewGroup) child).removeAllViews();
		}
	}

	private void graphsSetup() {

		// get data

		data = new JSONObject();

		graph1 = (WebView) subview.findViewById(R.id.download_graph);
		graph2 = (WebView) subview.findViewById(R.id.upload_graph);
		graph3 = (WebView) subview.findViewById(R.id.latency_graph);
		graph4 = (WebView) subview.findViewById(R.id.packetloss_graph);
		graph5 = (WebView) subview.findViewById(R.id.jitter_graph);

		graphHandler1 = new SamKnowsGraph(graph1);
		graphHandler2 = new SamKnowsGraph(graph2);
		graphHandler3 = new SamKnowsGraph(graph3);
		graphHandler4 = new SamKnowsGraph(graph4);
		graphHandler5 = new SamKnowsGraph(graph5);
		graphHandler5 = new SamKnowsGraph(graph5);

		graph1.setVerticalScrollBarEnabled(false);
		graph1.setHorizontalScrollBarEnabled(false);
		graph1.setBackgroundColor(Color.parseColor("#dddddd"));
		graphHandler1.setTag("download");
		graphHandler1
				.setDateFormat(new SKDateFormat(context).getJSDateFormat());

		graph1.getSettings().setJavaScriptEnabled(true);
		graph1.addJavascriptInterface(graphHandler1, "graphHandler");
		graph1.loadUrl("file:///android_res/raw/graphs.html");

		graph2.setVerticalScrollBarEnabled(false);
		graph2.setHorizontalScrollBarEnabled(false);
		graph2.setBackgroundColor(Color.parseColor("#dddddd"));
		graphHandler2.setTag("upload");
		graphHandler2
				.setDateFormat(new SKDateFormat(context).getJSDateFormat());

		graph2.getSettings().setJavaScriptEnabled(true);
		graph2.addJavascriptInterface(graphHandler2, "graphHandler");
		graph2.loadUrl("file:///android_res/raw/graphs.html");

		graph3.setVerticalScrollBarEnabled(false);
		graph3.setHorizontalScrollBarEnabled(false);
		graph3.setBackgroundColor(Color.parseColor("#dddddd"));
		graphHandler3.setTag("latency");
		graphHandler3
				.setDateFormat(new SKDateFormat(context).getJSDateFormat());

		graph3.getSettings().setJavaScriptEnabled(true);
		graph3.addJavascriptInterface(graphHandler3, "graphHandler");
		graph3.loadUrl("file:///android_res/raw/graphs.html");

		graph4.setVerticalScrollBarEnabled(false);
		graph4.setHorizontalScrollBarEnabled(false);
		graph4.setBackgroundColor(Color.parseColor("#dddddd"));
		graphHandler4.setTag("packetloss");
		graphHandler4
				.setDateFormat(new SKDateFormat(context).getJSDateFormat());

		graph4.getSettings().setJavaScriptEnabled(true);
		graph4.addJavascriptInterface(graphHandler4, "graphHandler");
		graph4.loadUrl("file:///android_res/raw/graphs.html");

		graph5.setVerticalScrollBarEnabled(false);
		graph5.setHorizontalScrollBarEnabled(false);
		graph5.setBackgroundColor(Color.parseColor("#dddddd"));
		graphHandler5.setTag("jitter");
		graphHandler5
				.setDateFormat(new SKDateFormat(context).getJSDateFormat());

		graph5.getSettings().setJavaScriptEnabled(true);
		graph5.addJavascriptInterface(graphHandler5, "graphHandler");
		graph5.loadUrl("file:///android_res/raw/graphs.html");

	}

	/**
	 * Create the options menu that displays the refresh and about options
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (Constants.DEBUG) {
			MenuItem item = menu.add(getString(R.string.test_results));
			item.setIntent(new Intent(this, TestResultsTabActivity.class));
		}

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Handle menu options
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		boolean ret = false;
		int itemId = item.getItemId();
		if (R.id.menu_about == itemId) {
			Intent intent = new Intent(this, SamKnowsAbout.class);
			startActivity(intent);
			ret = true;
		} else if (R.id.menu_settings == itemId) {
			startActivity(new Intent(this, SKPreferenceActivity.class));
			ret = true;
		} else if (R.id.menu_system_info == itemId) {
			startActivity(new Intent(this, SamKnowsInfoActivity.class));
			ret = true;
		} else if (R.id.activation == itemId) {
			int size = AppSettings.getInstance().getDevices().size();
			if (size == 0 || (size == 1 && OtherUtils.isPhoneAssosiated(this))) {
				AppSettings.getInstance().setForceDownload();
				MainService.force_poke(this);
			}
			startActivity(new Intent(this, SamKnowsActivating.class));
			finish();
			ret = true;
		} else if (R.id.map == itemId) {
			// startActivity(new Intent(this, SamKnowsMapActivity.class));
			startActivityForResult(new Intent(this, SamKnowsMapActivity.class),
					1);
			ret = true;
		} else if (R.id.menu_terms_and_condition == itemId) {
			Intent intent = new Intent(this, SamKnowsTermsOfUse.class);
			startActivity(intent);
			ret = true;
		} else if (R.id.menu_logout == itemId) {
			LoginHelper.logout(this);
			this.finish();
			ret = true;
		} else {
			ret = super.onOptionsItemSelected(item);
		}
		return ret;
	}

	/**
	 * Returns a response handler that displays a loading message
	 * 
	 * @return SamKnowsResponseHandler
	 */
	private SamKnowsResponseHandler getLoadingResponseHandler(String message) {
		final ProgressDialog dialog = getProgressDialog(message);
		return new SamKnowsResponseHandler() {
			public void onSuccess(JSONObject result, Date date,
					String start_date) {
				dialog.dismiss();
				// setStartDate(start_date);
				// setData(result);
				// setDate(date);
			}

			public void onFailure(Throwable error) {
				Logger.e(SamKnowsAggregateStatViewerActivity.class,
						"failed to get data", error);
				dialog.dismiss();
			}
		};
	}

	private ProgressDialog getProgressDialog(String message) {
		return ProgressDialog.show(SamKnowsAggregateStatViewerActivity.this,
				"", message, true, true);
	}

	/**
	 * Returns a response handler that displays a loading message for the RECENT
	 * api
	 * 
	 * @return SamKnowsResponseHandler
	 */
	private SamKnowsResponseHandler getRecentLoadingResponseHandler(
			String message) {
		final ProgressDialog dialog = getProgressDialog(message);
		return new SamKnowsResponseHandler() {
			public void onSuccess(JSONObject result, Date date,
					String start_date) {
				dialog.dismiss();
				// setRecentData(result);
			}

			public void onFailure(Throwable error) {
				dialog.dismiss();
			}
		};
	}

	private class MyPagerAdapter extends PagerAdapter {

		private ArrayList<StatRecord> views;

		@Override
		public void setPrimaryItem(ViewGroup container, int position,
				Object object) {
			current_page_view = (View) object;
			current_page_view_position = position;
		}

		public MyPagerAdapter(Context context) {

			views = new ArrayList<StatRecord>();

			views.add(new StatRecord());
			// views.get(0) is the aggregate view

			JSONObject summary = dbHelper.getArchiveDataSummary();

			try {
				total_archive_records = summary.getInt("counter");
				String last_run_test = summary.getString("startdate");

				if (Long.parseLong(last_run_test) != 0) {
					last_run_test_formatted = new SKDateFormat(context)
							.UITime(Long.parseLong(last_run_test));

				} else {
					last_run_test_formatted = "";
				}

			} catch (JSONException e1) {
				Logger.e(this, "Error in reading from JSONObject.", e1);
			}

			//
			for (int i = 0; i < total_archive_records; i++) {
				views.add(new StatRecord());
				// load blank records ready for populating
			}
		}

		public void readArchiveItem(int i) {
			JSONObject archive;
			try {

				archive = dbHelper.getArchiveData(i);

			} catch (Exception e) {
				Logger.e(this, "Error in reading archive item " + i, e);
				return;
			}

			// read headers of json
			String datetime = "";
			String dtime_formatted;
			try {
				datetime = archive.getString("dtime");

				dtime_formatted = new SKDateFormat(context).UITime(Long
						.parseLong(datetime));
				views.get(i + 1).time_stamp = dtime_formatted;

			} catch (JSONException e1) {
				e1.printStackTrace();
			}

			String location;
			String testnumber;
			String hrresult;
			String success;
			JSONObject user = null;
			JSONArray results = null;
			int itemcount = 0;

			// unpack activemetrics

			try {
				results = archive.getJSONArray("activemetrics");
			} catch (JSONException je) {
				Logger.e(this, "Exception in reading active metrics array: "
						+ je.getMessage());
			}

			for (itemcount = 0; itemcount < results.length(); itemcount++) {
				location = "";
				testnumber = "";
				hrresult = "";
				success = "";
				user = null;
				try {
					user = results.getJSONObject(itemcount);
				} catch (JSONException je) {
					Logger.e(
							this,
							"Exception in reading JSONObject: "
									+ je.getMessage());
				}
				try {
					testnumber = user.getString("test");
					location = user.getString("location");
					success = user.getString("success");
					hrresult = user.getString("hrresult");

					if (success.equals("0")) {
						hrresult = getString(R.string.failed);
					}

					if (testnumber.equals("" + TestResult.UPLOAD_TEST_ID)) {
						views.get(i + 1).tests_location = location;
						views.get(i + 1).upload_location = location;
						views.get(i + 1).upload_result = hrresult;

					}
					if (testnumber.equals("" + TestResult.DOWNLOAD_TEST_ID)) {
						views.get(i + 1).tests_location = location;
						views.get(i + 1).download_location = location;
						views.get(i + 1).download_result = hrresult;
					}

					if (testnumber.equals("" + TestResult.LATENCY_TEST_ID)) {
						views.get(i + 1).tests_location = location;
						views.get(i + 1).latency_location = location;
						views.get(i + 1).latency_result = hrresult;
					}

					if (testnumber.equals("" + TestResult.PACKETLOSS_TEST_ID)) {
						views.get(i + 1).tests_location = location;
						views.get(i + 1).packetloss_location = location;
						views.get(i + 1).packetloss_result = hrresult;
					}

					if (testnumber.equals("" + TestResult.JITTER_TEST_ID)) {
						views.get(i + 1).tests_location = location;
						views.get(i + 1).jitter_location = location;
						views.get(i + 1).jitter_result = hrresult;
					}

				} catch (JSONException je) {
					Logger.e(
							this,
							"Exception in reading JSONObject: "
									+ je.getMessage());
				}
			}

			// unpack passivemetrics
			results = null;
			try {
				results = archive.getJSONArray("passivemetrics");
			} catch (JSONException je) {
				Logger.e(this,
						"Exception in reading JSONObject: " + je.getMessage());
			}

			String metric;
			String value;
			String type;

			for (itemcount = 0; itemcount < results.length(); itemcount++) {
				metric = "";
				value = "";
				type = "";
				user = null;
				try {
					user = results.getJSONObject(itemcount);
				} catch (JSONException je) {
					Logger.e(
							this,
							"Exception in reading JSONObject: "
									+ je.getMessage());
				}
				try {
					metric = user.getString("metric");
					value = user.getString("value");
					type = user.getString("type");

					if (metric.equals("connected")) { // connected
						views.get(i + 1).passivemetric1 = value;
						views.get(i + 1).passivemetric1_type = type;
					}
					if (metric.equals("connectivitytype")) { // connectivity
																// type
						views.get(i + 1).passivemetric2 = value;
						views.get(i + 1).passivemetric2_type = type;
					}
					if (metric.equals("gsmcelltowerid")) { // cell tower id
						views.get(i + 1).passivemetric3 = value;
						views.get(i + 1).passivemetric3_type = type;
					}
					if (metric.equals("gsmlocationareacode")) { // cell tower
																// location area
						views.get(i + 1).passivemetric4 = value;
						views.get(i + 1).passivemetric4_type = type;
					}
					if (metric.equals("gsmsignalstrength")) { // signal strength
						views.get(i + 1).passivemetric5 = value;
						views.get(i + 1).passivemetric5_type = type;
					}
					if (metric.equals("networktype")) { // bearer
						views.get(i + 1).passivemetric6 = value;
						views.get(i + 1).passivemetric6_type = type;
					}
					if (metric.equals("networkoperatorname")) { // network
																// operator
						views.get(i + 1).passivemetric7 = value;
						views.get(i + 1).passivemetric7_type = type;
					}
					if (metric.equals("latitude")) { // latitude
						views.get(i + 1).passivemetric8 = value;
						views.get(i + 1).passivemetric8_type = type;
					}
					if (metric.equals("longitude")) { // longitude
						views.get(i + 1).passivemetric9 = value;
						views.get(i + 1).passivemetric9_type = type;
					}
					if (metric.equals("accuracy")) { // accuracy
						views.get(i + 1).passivemetric10 = value;
						views.get(i + 1).passivemetric10_type = type;
					}
					if (metric.equals("locationprovider")) { // location
																// provider
						views.get(i + 1).passivemetric11 = value;
						views.get(i + 1).passivemetric11_type = type;
					}

					if (metric.equals("simoperatorcode")) { // sim operator code
						views.get(i + 1).passivemetric12 = value;
						views.get(i + 1).passivemetric12_type = type;
					}
					if (metric.equals("simoperatorname")) { // sim operator name
						views.get(i + 1).passivemetric13 = value;
						views.get(i + 1).passivemetric13_type = type;
					}
					if (metric.equals("imei")) { // imei
						views.get(i + 1).passivemetric14 = value;
						views.get(i + 1).passivemetric14_type = type;
					}
					if (metric.equals("imsi")) { // imsi
						views.get(i + 1).passivemetric15 = value;
						views.get(i + 1).passivemetric15_type = type;
					}
					if (metric.equals("manufactor")) { // manufacter
						views.get(i + 1).passivemetric16 = value;
						views.get(i + 1).passivemetric16_type = type;
					}
					if (metric.equals("model")) { // model
						views.get(i + 1).passivemetric17 = value;
						views.get(i + 1).passivemetric17_type = type;
					}
					if (metric.equals("ostype")) { // os type
						views.get(i + 1).passivemetric18 = value;
						views.get(i + 1).passivemetric18_type = type;
					}
					if (metric.equals("osversion")) { // os version
						views.get(i + 1).passivemetric19 = value;
						views.get(i + 1).passivemetric19_type = type;
					}
					if (metric.equals("gsmbiterrorrate")) { // gsmbiterrorrate
						views.get(i + 1).passivemetric20 = value;
						views.get(i + 1).passivemetric20_type = type;
					}
					if (metric.equals("cdmaecio")) { // cdmaecio
						views.get(i + 1).passivemetric21 = value;
						views.get(i + 1).passivemetric21_type = type;
					}

					if (metric.equals("phonetype")) { // phone type
						views.get(i + 1).passivemetric22 = value;
						views.get(i + 1).passivemetric22_type = type;
					}
					if (metric.equals("activenetworktype")) { // active network
																// type
						if(value.length() > 0 ){
							String new_value = value.substring(0, 1).toUpperCase() + value.substring(1);
							views.get(i + 1).active_network_type = "("+new_value+")";
						}
						
						//views.get(i + 1).passivemetric23 = value;
						//views.get(i + 1).passivemetric23_type = type;
					}
					if (metric.equals("connectionstatus")) { // connection
																// status
						views.get(i + 1).passivemetric24 = value;
						views.get(i + 1).passivemetric24_type = type;
					}
					if (metric.equals("roamingstatus")) { // roaming status
						views.get(i + 1).passivemetric25 = value;
						views.get(i + 1).passivemetric25_type = type;
					}
					if (metric.equals("networkoperatorcode")) { // network
																// operator code
						views.get(i + 1).passivemetric26 = value;
						views.get(i + 1).passivemetric26_type = type;
					}

					if (metric.equals("cdmasignalstrength")) { // cdmasignalstrength
						views.get(i + 1).passivemetric27 = value;
						views.get(i + 1).passivemetric27_type = type;
					}
					if (metric.equals("cdmabasestationid")) { // cdmabasestationid
						views.get(i + 1).passivemetric28 = value;
						views.get(i + 1).passivemetric28_type = type;
					}
					if (metric.equals("cdmabasestationlatitude")) { // cdmabasestationlatitude
						views.get(i + 1).passivemetric29 = value;
						views.get(i + 1).passivemetric29_type = type;
					}
					if (metric.equals("cdmabasestationlongitude")) { // cdmabasestationlongitude
						views.get(i + 1).passivemetric30 = value;
						views.get(i + 1).passivemetric30_type = type;
					}
					if (metric.equals("cdmanetworkid")) { // cdmanetworkid
						views.get(i + 1).passivemetric31 = value;
						views.get(i + 1).passivemetric31_type = type;
					}
					if (metric.equals("cdmasystemid")) { // cdmasystemid
						views.get(i + 1).passivemetric32 = value;
						views.get(i + 1).passivemetric32_type = type;
					}

				} catch (JSONException je) {
					Logger.e(
							this,
							"Exception in reading JSONObject: "
									+ je.getMessage());
				}
			}

		}

		@Override
		public void destroyItem(View view, int arg1, Object object) {
			((ViewPager) view).removeView((StatView) object);
		}

		@Override
		public void finishUpdate(View arg0) {

		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}

		@Override
		public int getCount() {
			return views.size();
		}

		@Override
		public Object instantiateItem(View view, int position) {

			StatView sc = new StatView(SamKnowsAggregateStatViewerActivity.this);
			sc.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT));
			sc.setFillViewport(true);

			LayoutInflater inflater = (LayoutInflater) SamKnowsAggregateStatViewerActivity.this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			// subview = inflater.inflate(R.layout.individual_stat, null);
			View subview_archive;

			if (position == 0 && first_time == true) {
				subview = inflater.inflate(R.layout.aggregate_stat, null);
				subview.setTag(position);

				// first_time=false;

				sc.addView(subview);

				((ViewPager) view).addView(sc);

				if (!AppSettings.getInstance().stateMachineStatus()) {
					TextView tv = (TextView) subview
							.findViewById(R.id.no_data_message_text);
					tv.setText(R.string.activation_needed);
				} else if (total_archive_records > 0) { // if no data hide no
														// data icon
														// & message
					TextView tv = (TextView) subview
							.findViewById(R.id.no_data_message_text);
					ImageView iv = (ImageView) subview
							.findViewById(R.id.no_data_message_image);
					tv.setVisibility(View.GONE);
					iv.setVisibility(View.GONE);
					// populate view
					adapter.readArchiveItem(position);
				}

				((TextView) subview.findViewById(R.id.test_last_run))
						.setText(last_run_test_formatted);

				buttonSetup();

				loadAverage(1);
				graphsSetup();

				dataReader = new asyncReadData();
				dataReader.execute();

				loadGrids();

			}

			if (position > 0) {

				subview_archive = inflater.inflate(R.layout.individual_stat,
						null);
				subview_archive.setTag(position);
				sc.addView(subview_archive);

				StatRecord sr = views.get(position);

				sc.setData(sr);

				((ViewPager) view).addView(sc);

				if (position == total_archive_records) {
					sc.setRightPageIndicator(false);
				}

				// populate view
				if (position < total_archive_records) {
					adapter.readArchiveItem(position);
				}
			}

			Util.overrideFonts(SamKnowsAggregateStatViewerActivity.this, sc);
			return sc;

		}

		private void loadGrids() {

			loadDownloadGrid(TestResult.DOWNLOAD_TEST_ID,
					R.id.agggregate_test1_grid, 0, 5);
			loadDownloadGrid(TestResult.UPLOAD_TEST_ID,
					R.id.agggregate_test2_grid, 0, 5);
			loadDownloadGrid(TestResult.LATENCY_TEST_ID,
					R.id.agggregate_test3_grid, 0, 5);
			loadDownloadGrid(TestResult.PACKETLOSS_TEST_ID,
					R.id.agggregate_test4_grid, 0, 5);
			loadDownloadGrid(TestResult.JITTER_TEST_ID,
					R.id.agggregate_test5_grid, 0, 5);

			LinearLayout l = (LinearLayout) findViewById(R.id.download_content);
			l.setVisibility(View.INVISIBLE);
			l.getLayoutParams().height = 0;

			l = (LinearLayout) findViewById(R.id.upload_content);
			l.setVisibility(View.INVISIBLE);
			l.getLayoutParams().height = 0;

			l = (LinearLayout) findViewById(R.id.latency_content);
			l.setVisibility(View.INVISIBLE);
			l.getLayoutParams().height = 0;

			l = (LinearLayout) findViewById(R.id.packetloss_content);
			l.setVisibility(View.INVISIBLE);
			l.getLayoutParams().height = 0;

			l = (LinearLayout) findViewById(R.id.jitter_content);
			l.setVisibility(View.INVISIBLE);
			l.getLayoutParams().height = 0;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {

		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {

		}
	}

	private void SingleChoice() {
		Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.choose_time));
		// dropdown setup
		final String array_spinner[];

		array_spinner = new String[4];
		array_spinner[0] = getString(R.string.time_period_1week);
		array_spinner[1] = getString(R.string.time_period_1month);
		array_spinner[2] = getString(R.string.time_period_3months);
		array_spinner[3] = getString(R.string.time_period_1year);

		builder.setItems(array_spinner, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				int weeks = 0;

				if (array_spinner[which] == getString(R.string.time_period_1week)) {
					weeks = 1;
				}
				if (array_spinner[which] == getString(R.string.time_period_1month)) {
					weeks = 4;
				}
				if (array_spinner[which] == getString(R.string.time_period_3months)) {
					weeks = 12;
				}
				if (array_spinner[which] == getString(R.string.time_period_1year)) {
					weeks = 52;
				}

				TextView tvHeader = (TextView) findViewById(R.id.timeperiod);
				tvHeader.setText(array_spinner[which]);

				loadAverage(weeks);

				Calendar now = Calendar.getInstance();
				long current_dtime = now.getTimeInMillis();
				now.add(Calendar.WEEK_OF_YEAR, weeks * -1);
				long starting_dtime = now.getTimeInMillis();

				// update charts
				data = new JSONObject();

				data = dbHelper.getGraphData(TestResult.DOWNLOAD_TEST_ID,
						starting_dtime, current_dtime);
				graphHandler1.setData(data);
				graphHandler1.update();

				data = dbHelper.getGraphData(TestResult.UPLOAD_TEST_ID,
						starting_dtime, current_dtime);
				graphHandler2.setData(data);
				graphHandler2.update();

				data = dbHelper.getGraphData(TestResult.LATENCY_TEST_ID,
						starting_dtime, current_dtime);
				graphHandler3.setData(data);
				graphHandler3.update();

				data = dbHelper.getGraphData(TestResult.PACKETLOSS_TEST_ID,
						starting_dtime, current_dtime);
				graphHandler4.setData(data);
				graphHandler4.update();

				data = dbHelper.getGraphData(TestResult.JITTER_TEST_ID,
						starting_dtime, current_dtime);
				graphHandler5.setData(data);
				graphHandler5.update();

				dialog.dismiss();
			}
		});
		builder.setNegativeButton(getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void RunChoice() {

		storage = CachingStorage.getInstance();
		config = storage.loadScheduleConfig();
		// if config == null the app is not been activate and
		// no test can be run
		if (config == null) {
			// TODO Add an alert that the app has not been init yet
			config = new ScheduleConfig();
		}
		testList = config.manual_tests;
		array_spinner = new String[testList.size() + 1];
		array_spinner_int = new int[testList.size() + 1];

		Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.choose_test));
		// dropdown setup

		for (int i = 0; i < testList.size(); i++) {
			TestDescription td = testList.get(i);
			array_spinner[i] = td.displayName;
			array_spinner_int[i] = td.testId;
		}
		array_spinner[testList.size()] = getString(R.string.all);
		array_spinner_int[testList.size()] = -1;

		builder.setItems(array_spinner, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				dialog.dismiss();

				Intent intent = new Intent(
						SamKnowsAggregateStatViewerActivity.this,
						SamKnowsTestViewerActivity.class);
				Bundle b = new Bundle();
				b.putInt("testID", array_spinner_int[which]);
				intent.putExtras(b);
				startActivityForResult(intent, 1);
				overridePendingTransition(R.anim.transition_in,
						R.anim.transition_out);
			}
		});
		builder.setNegativeButton(getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	public void onBackPressed() {

		if (on_aggregate_page) {
			SamKnowsAggregateStatViewerActivity.this.finish();
		} else {
			viewPager = (ViewPager) findViewById(R.id.viewPager);
			// viewPager.setAdapter(adapter);
			viewPager.setCurrentItem(0, false);
			overridePendingTransition(0, 0);
			on_aggregate_page = true;
		}
	}

	/*
	 * @Override public void onBackPressed() {
	 * 
	 * if(AppSettings.getInstance().anonymous){
	 * SamKnowsAggregateStatViewerActivity.this.finish(); }else{ new
	 * AlertDialog.Builder(this)
	 * .setMessage(getString(R.string.logout_question)) .setCancelable(true)
	 * .setPositiveButton(R.string.logout, new DialogInterface.OnClickListener()
	 * { public void onClick(DialogInterface dialog, int id) { LoginHelper
	 * .logout(SamKnowsAggregateStatViewerActivity.this);
	 * SamKnowsAggregateStatViewerActivity.this .finish(); } })
	 * .setNegativeButton(R.string.close, new DialogInterface.OnClickListener()
	 * { public void onClick(DialogInterface dialog, int id) {
	 * SamKnowsAggregateStatViewerActivity.this .finish(); } }).show(); } }
	 */

	@Override
	public void onClick(View v) {
		// Toast.makeText(this,"clicked ..."+v.getId(),3000).show();

		ImageView button = null;
		LinearLayout l = null;

		int grid = 0;
		int testid = 0;
		boolean buttonfound = false;

		int id = v.getId();
		if (id == R.id.download_header || id == R.id.btn_download_toggle) {
			if (total_download_archive_records > 0) {
				buttonfound = true;
			}
			grid = R.id.agggregate_test1_grid;
			testid = TestResult.DOWNLOAD_TEST_ID;
			l = (LinearLayout) findViewById(R.id.download_content);
			button = (ImageView) findViewById(R.id.btn_download_toggle);

		}

		if (id == R.id.upload_header || id == R.id.btn_upload_toggle) {

			if (total_upload_archive_records > 0) {
				buttonfound = true;
			}
			grid = R.id.agggregate_test2_grid;
			testid = TestResult.UPLOAD_TEST_ID;
			l = (LinearLayout) findViewById(R.id.upload_content);
			button = (ImageView) findViewById(R.id.btn_upload_toggle);
		}

		if (id == R.id.latency_header || id == R.id.btn_latency_toggle) {
			if (total_latency_archive_records > 0) {
				buttonfound = true;
			}
			grid = R.id.agggregate_test3_grid;
			testid = TestResult.LATENCY_TEST_ID;
			l = (LinearLayout) findViewById(R.id.latency_content);
			button = (ImageView) findViewById(R.id.btn_latency_toggle);
		}

		if (id == R.id.packetloss_header || id == R.id.btn_packetloss_toggle) {
			if (total_packetloss_archive_records > 0) {
				buttonfound = true;
			}
			grid = R.id.agggregate_test4_grid;
			testid = TestResult.PACKETLOSS_TEST_ID;
			l = (LinearLayout) findViewById(R.id.packetloss_content);
			button = (ImageView) findViewById(R.id.btn_packetloss_toggle);
		}

		if (id == R.id.jitter_header || id == R.id.btn_jitter_toggle) {
			if (total_jitter_archive_records > 0) {
				buttonfound = true;
			}
			grid = R.id.agggregate_test5_grid;
			testid = TestResult.JITTER_TEST_ID;
			l = (LinearLayout) findViewById(R.id.jitter_content);
			button = (ImageView) findViewById(R.id.btn_jitter_toggle);
		}

		// actions

		if (buttonfound) {

			if (l.getVisibility() == View.INVISIBLE) {

				button.setBackgroundResource(R.drawable.btn_up);
				button.setContentDescription(getString(R.string.close_panel));
				// graphHandler1.update();
				l.measure(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
				target_height = l.getMeasuredHeight();

				clearGrid(grid);
				loadDownloadGrid(testid, grid, 0, 5);

				l.getLayoutParams().height = 0;
				l.setVisibility(View.VISIBLE);

				ResizeAnimation animation = null;

				animation = new ResizeAnimation(l, target_height, 0, false);
				animation.setDuration(500);
				animation.setFillEnabled(true);
				animation.setFillAfter(true);

				animation
						.setAnimationListener(new Animation.AnimationListener() {
							@Override
							public void onAnimationStart(Animation animation) {
							}

							@Override
							public void onAnimationRepeat(Animation animation) {
							}

							@Override
							public void onAnimationEnd(Animation animation) {
							}
						});
				l.startAnimation(animation);

			} else {

				ResizeAnimation animation = null;
				int required_height = l.getMeasuredHeight();
				animation = new ResizeAnimation(l, 0, target_height, false);
				animation.setDuration(500);
				animation.setFillEnabled(true);
				animation.setFillAfter(true);
				MyAnimationListener animationListener = new MyAnimationListener();
				animationListener.setView(l);
				animation.setAnimationListener(animationListener);
				l.startAnimation(animation);

				button.setBackgroundResource(R.drawable.btn_down);
				button.setContentDescription(getString(R.string.open_panel));
			}
		}

	}

	public class MyAnimationListener implements AnimationListener {
		View view;

		public void setView(View view) {
			this.view = view;
		}

		public void onAnimationEnd(Animation animation) {
			view.setVisibility(View.INVISIBLE);
		}

		public void onAnimationRepeat(Animation animation) {
		}

		public void onAnimationStart(Animation animation) {
		}
	}

	private class asyncReadData extends AsyncTask<Void, Void, Boolean> {

		JSONObject data0 = new JSONObject();
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		JSONObject data3 = new JSONObject();
		JSONObject data4 = new JSONObject();

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			boolean status = false;

			JSONObject summary = dbHelperAsync.getArchiveDataSummary();

			try {
				total_archive_records = summary.getInt("counter");
			} catch (JSONException e1) {

				e1.printStackTrace();
			}

			Calendar now = Calendar.getInstance();
			long current_dtime = now.getTimeInMillis();
			now.add(Calendar.WEEK_OF_YEAR, -1);
			long starting_dtime = now.getTimeInMillis();

			// update charts

			data0 = dbHelperAsync.getGraphData(TestResult.DOWNLOAD_TEST_ID,
					starting_dtime, current_dtime);

			data1 = dbHelperAsync.getGraphData(TestResult.UPLOAD_TEST_ID,
					starting_dtime, current_dtime);

			data2 = dbHelperAsync.getGraphData(TestResult.LATENCY_TEST_ID,
					starting_dtime, current_dtime);

			data3 = dbHelperAsync.getGraphData(TestResult.PACKETLOSS_TEST_ID,
					starting_dtime, current_dtime);

			data4 = dbHelperAsync.getGraphData(TestResult.JITTER_TEST_ID,
					starting_dtime, current_dtime);

			status = true;
			return status;

		}

		@Override
		protected void onPostExecute(Boolean result) {
			// update view
			graphHandler1.setData(data0);
			graphHandler1.update();

			graphHandler2.setData(data1);
			graphHandler2.update();

			graphHandler3.setData(data2);
			graphHandler3.update();

			graphHandler4.setData(data3);
			graphHandler4.update();

			graphHandler5.setData(data4);
			graphHandler5.update();

		}
	}

}
