package com.samknows.fcc.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


import org.achartengine.GraphicalView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.samknows.fcc.activity.components.StatView;
import com.samknows.libcore.SKCommon;
import com.samknows.libcore.SKLogger;
import com.samknows.measurement.SK2AppSettings;
import com.samknows.measurement.CachingStorage;
import com.samknows.measurement.DeviceDescription;
import com.samknows.measurement.MainService;
import com.samknows.measurement.SKApplication;
import com.samknows.measurement.SKApplication.eNetworkTypeResults;
import com.samknows.fcc.R;
import com.samknows.measurement.SamKnowsLoginService;
import com.samknows.measurement.SamKnowsResponseHandler;
import com.samknows.measurement.Storage;
import com.samknows.measurement.activity.BaseLogoutActivity;
import com.samknows.measurement.activity.components.ResizeAnimation;
import com.samknows.measurement.activity.components.SamKnowsGraph;
import com.samknows.measurement.activity.components.ServiceStatusDisplayManager;
import com.samknows.measurement.activity.components.StatModel;
import com.samknows.measurement.activity.components.StatRecord;
import com.samknows.measurement.activity.components.UpdatedTextView;
import com.samknows.measurement.activity.components.Util;
import com.samknows.measurement.schedule.ScheduleConfig;
import com.samknows.measurement.schedule.TestDescription;
import com.samknows.measurement.storage.DBHelper;
import com.samknows.measurement.storage.TestResult;
import com.samknows.measurement.util.OtherUtils;
import com.samknows.measurement.util.SKDateFormat;

public class FCCMainResultsActivity extends BaseLogoutActivity
		implements OnClickListener {

	// use to decide when to show the state_machine_status_failure

	SamKnowsGraph graphHandlerDownload;
	SamKnowsGraph graphHandlerUpload;
	SamKnowsGraph graphHandlerLatency;
	SamKnowsGraph graphHandlerPacketLoss;
	SamKnowsGraph graphHandlerJitter;
	int download_page_index = 0;
	int upload_page_index = 0;
	int latency_page_index = 0;
	int packetloss_page_index = 0;
	int jitter_page_index = 0;

	// For mock testing...
	public SamKnowsGraph getDownloadGraphHandler () {
        return graphHandlerDownload;
	}

	private static final String TAG = FCCMainResultsActivity.class
			.getSimpleName();
	public static final String SETTINGS = "SamKnows";
	private static final int PANEL_HEIGHT = 550;
	private final Context context = this;
	private SamKnowsLoginService service = new SamKnowsLoginService();

	private StatModel statModel = new StatModel();
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

	private int total_archive_records = 0;
	private int total_download_archive_records = 0;
	private int total_upload_archive_records = 0;
	private int total_latency_archive_records = 0;
	private int total_packetloss_archive_records = 0;
	private int total_jitter_archive_records = 0;

	private DBHelper dbHelper;
	// private DBHelper dbHelperAsync;

	private MyPagerAdapter adapter = null;
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
	
	TextView tvHeader = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SKLogger.d(this, "+++++DEBUG+++++ SamKnowsAggregateStatViewerActivity onCreate...");
		
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
		
		this.setTitle(getString(R.string.sk2_main_results_activity_title));

		setContentView(R.layout.fcc_main_results_activity_main_page_views);

		dbHelper = new DBHelper(FCCMainResultsActivity.this);
		//dbHelperAsync = new DBHelper(SamKnowsAggregateStatViewerActivity.this);

		viewPager = (ViewPager) findViewById(R.id.viewPager);
	
		adapter = new MyPagerAdapter(this);
		viewPager.setAdapter(adapter);
		// viewPager.setOffscreenPageLimit(3);

		tvHeader = (TextView) findViewById(R.id.textViewHeader);

		viewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int page) {
             	handleOnPageSelected(page);
			}

			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
			}

			@Override
			public void onPageScrollStateChanged(int state) {
//				if (state == ViewPager.SCROLL_STATE_SETTLING) {
//
//				}
			}
		});
		

		Util.initializeFonts(this);
		Util.overrideFonts(this, findViewById(android.R.id.content));

	}
	
	void handleOnPageSelected(int page) {

		tvHeader.setText(getString(R.string.page) + " " + (page + 1));

		if (page == 0) {
			on_aggregate_page = true;
			boolean db_refresh = false;

			FCCMainResultsActivity.this.setTitle(getString(R.string.sk2_main_results_activity_title));

			View v = viewPager.findViewWithTag(page);

			if (v == null) {
				// ... we should trap this where possible in the debugger...
				SKLogger.sAssert(getClass(), false);
			} else {
				TextView timestampView = (TextView) v.findViewById(R.id.average_results_title);

				if (timestampView == null) {
					// ... we should trap this where possible in the debugger...
					SKLogger.sAssert(getClass(), false);
				} else {

					timestampView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
				}
			}

		} else {
			View v = viewPager.findViewWithTag(page);
			if (v == null) {
				// ... we should trap this where possible in the debugger...
				SKLogger.sAssert(getClass(), false);
			} else {
				TextView timestampView = (TextView) v.findViewById(R.id.timestamp);
				timestampView.setContentDescription(getString(R.string.archive_result) + " " + timestampView.getText());
				timestampView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
				on_aggregate_page = false;
				FCCMainResultsActivity.this.setTitle(getString(R.string.archive_result));
				
				
				String caption = getString(R.string.archive_result) + " " + page + " " + getString(R.string.archive_result_of) + " " + total_archive_records;
				// Following line was used for testing that the text will fit...
				//caption = getString(R.string.archive_result) + " " + 9999 + " " + getString(R.string.archive_result_of) + " " + 9999;
				TextView captionView = (TextView) v.findViewById(R.id.archived_result_x_of_y);
				captionView.setText(caption);

			}
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		SKLogger.d(this, "+++++DEBUG+++++ SamKnowsAggregateStatViewerActivity onDestroy...");
	}
	
	boolean mbHandlingOnActivityResult = false;

	@Override
	public void onResume() {
		super.onResume(); // Always call the superclass method first
		
        SKLogger.d(this, "+++++DEBUG+++++ SamKnowsAggregateStatViewerActivity onResume...");

        if (mbHandlingOnActivityResult == true) {
        	// Already handled on the activity result... which has taken us to the archived results screen.
            mbHandlingOnActivityResult = false;
        } else {
        	// Resuming from e.g. T&C screen, or About screen.
        	if (setTotalArchiveRecords()) {
        		adapter = new MyPagerAdapter(FCCMainResultsActivity.this);
        		//viewPager = (ViewPager) findViewById(R.id.viewPager);
				SKLogger.sAssert(getClass(), viewPager == (ViewPager) findViewById(R.id.viewPager));
        		viewPager.setAdapter(adapter);
        	}
        }
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (resultCode == RESULT_OK) {
			mbHandlingOnActivityResult = true;

			// The returned result might be a mobile result, or a network result.
			// We must update the filter to match, and then show that result screen...!
			String activeNetworkType = data.getStringExtra("activeneworktype");

			if (activeNetworkType == null) {
				SKLogger.sAssert(getClass(), false);
			} else if (activeNetworkType.equals("mobile")) {
				if (SKApplication.getNetworkTypeResults() == eNetworkTypeResults.eNetworkTypeResults_WiFi) {
    				SKApplication.setNetworkTypeResults(eNetworkTypeResults.eNetworkTypeResults_Any);
				}
			} else if (activeNetworkType.equals("WiFi")) {
				if (SKApplication.getNetworkTypeResults() == eNetworkTypeResults.eNetworkTypeResults_Mobile) {
    				SKApplication.setNetworkTypeResults(eNetworkTypeResults.eNetworkTypeResults_Any);
				}
			} else {
				SKLogger.sAssert(getClass(), false);
			}
			setNetworkTypeToggleButton();

			// refresh data
			adapter = new MyPagerAdapter(FCCMainResultsActivity.this);
			setTotalArchiveRecords();
			//viewPager = (ViewPager) findViewById(R.id.viewPager);
			SKLogger.sAssert(getClass(), viewPager == (ViewPager) findViewById(R.id.viewPager));
			viewPager.setAdapter(adapter);
			viewPager.setCurrentItem(1, true); // true means - perform a smooth scroll!
			//overridePendingTransition(0, 0);
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

	private void loadAverage() {
		JSONArray jsonResult;
		Calendar now = Calendar.getInstance();

		long current_dtime = now.getTimeInMillis();
		now.add(Calendar.WEEK_OF_YEAR, mWeeks * -1);
		long starting_dtime = now.getTimeInMillis();

		jsonResult = dbHelper.getAverageResults(starting_dtime, current_dtime);
	
		// Clear out the results, as there might be no results on first
		// use, when toggling mobile results <-> network results.
		((TextView) subview.findViewById(R.id.download_average)).setText("");
		((TextView) subview.findViewById(R.id.upload_average)).setText("");
		((TextView) subview.findViewById(R.id.latency_average)).setText("");
		((TextView) subview.findViewById(R.id.packetloss_average)).setText("");
		((TextView) subview.findViewById(R.id.jitter_average)).setText("");

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
		
		total_archive_records = 0;
		total_download_archive_records = 0;
		total_upload_archive_records = 0;
		total_latency_archive_records = 0;
		total_packetloss_archive_records = 0;
		total_jitter_archive_records = 0;

		JSONObject summary = dbHelper.getArchiveDataSummary();

		JSONObject results = null;

		try {
			results = summary.getJSONObject("test_counter");

		} catch (JSONException e) {

			SKLogger.sAssert(getClass(),  false);
			
			return false;
		}
		

		try {
			if (results.has("" + TestResult.DOWNLOAD_TEST_ID)) {
				total_download_archive_records = results.getInt(""
						+ TestResult.DOWNLOAD_TEST_ID);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		if (mShowArchivedResultsButton != null) {
			mShowArchivedResultsButton.setVisibility((total_download_archive_records > 0) ? View.VISIBLE :
				View.INVISIBLE);
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
             	last_run_test_formatted = this.getString(R.string.last_run_never);
			}

		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		return result;
	}

	private void loadDownloadGrid(int testnumber, int grid, int offset,
			int limit) {

		double a1 = (double) total_archive_records;
		double a2 = (double) limit;
		int pages = (int) Math.ceil(a1 / a2);
		int page_number = (offset + limit) / limit;

		TextView tv;

		switch (testnumber) {
		case TestResult.DOWNLOAD_TEST_ID:
			a1 = (double) total_download_archive_records;
			pages = (int) Math.ceil(a1 / a2);
			if (pages == 0) pages = 1;

			tv = (TextView) subview.findViewById(R.id.download_pagenumber);
			tv.setText(getString(R.string.page) + " " + page_number + " "
					+ getString(R.string.of) + " " + pages);
			break;

		case TestResult.UPLOAD_TEST_ID:
			a1 = (double) total_upload_archive_records;
			pages = (int) Math.ceil(a1 / a2);
			if (pages == 0) pages = 1;
			
			tv = (TextView) subview.findViewById(R.id.upload_pagenumber);
			tv.setText(getString(R.string.page) + " " + page_number + " "
					+ getString(R.string.of) + " " + pages);
			break;

		case TestResult.LATENCY_TEST_ID:
			a1 = (double) total_latency_archive_records;
			pages = (int) Math.ceil(a1 / a2);
			if (pages == 0) pages = 1;
			
			tv = (TextView) subview.findViewById(R.id.latency_pagenumber);
			tv.setText(getString(R.string.page) + " " + page_number + " "
					+ getString(R.string.of) + " " + pages);
			break;

		case TestResult.PACKETLOSS_TEST_ID:
			a1 = (double) total_packetloss_archive_records;
			pages = (int) Math.ceil(a1 / a2);
			if (pages == 0) pages = 1;
			
			tv = (TextView) subview.findViewById(R.id.packetloss_pagenumber);
			tv.setText(getString(R.string.page) + " " + page_number + " "
					+ getString(R.string.of) + " " + pages);
			break;

		case TestResult.JITTER_TEST_ID:
			a1 = (double) total_jitter_archive_records;
			pages = (int) Math.ceil(a1 / a2);
			if (pages == 0) pages = 1;
			
			tv = (TextView) subview.findViewById(R.id.jitter_pagenumber);
			tv.setText(getString(R.string.page) + " " + page_number + " "
					+ getString(R.string.of) + " " + pages);
			break;

		default:

		}

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

		int addedRowCount = 0;
		
		if (results != null) {

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
				
				if (user != null) {
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
				}
			
				if (addedRowCount == 0) {
					// Add the header, just before adding the first row!
					addGridItemHeader(getString(R.string.date), getString(R.string.server),
							getString(R.string.result), grid);
				}

				if (success.equals("1")) {
					addGridItem(dtime_formatted, location, result, grid);
				} else {
					result = getString(R.string.failed);
					addGridItemFailed(dtime_formatted, location, result, grid);
				}
			
				addedRowCount++;
			}
		}
		
		TableLayout table = (TableLayout) findViewById(grid);
		if (addedRowCount > 0) {
//			LayoutParams x = new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
//			table.setLayoutParams(x);
			table.setVisibility(View.VISIBLE);
		} else {
//			LayoutParams x = new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, 1);
//			table.setLayoutParams(x);
//			table.setVisibility(View.VISIBLE);
			table.setVisibility(View.GONE);
			table.getLayoutParams().height = 0;
		}
		table.getParent().requestLayout();

		Util.overrideFonts(this, findViewById(android.R.id.content));
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);

//	    // Checks the orientation of the screen
//	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//	        Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
//	    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
//	        Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
//	    }
	}

	Button mNetworkTypeToggleButton = null;
//	TextView     mNetworkTypeToggleTextView = null;
	Button       mShowArchivedResultsButton = null;
	
	void setNetworkTypeToggleButton() {
		mNetworkTypeToggleButton = (Button) findViewById(R.id.networkTypeButton);

		if (mNetworkTypeToggleButton == null) {
			SKLogger.sAssert(getClass(), false);
		} else {
			if (SKApplication.getNetworkTypeResults() == eNetworkTypeResults.eNetworkTypeResults_WiFi) {
				mNetworkTypeToggleButton.setText(R.string.network_type_wifi_results);
			} else if (SKApplication.getNetworkTypeResults() == eNetworkTypeResults.eNetworkTypeResults_Mobile) {
				mNetworkTypeToggleButton.setText(R.string.network_type_mobile_results);
			} else {
				mNetworkTypeToggleButton.setText(R.string.network_type_all_results);
			}
		}
	}
	
	private void buttonSetup() {
		


		// button setup

//		Button execute_button;
//		execute_button = (Button) subview.findViewById(R.id.btnRunTest);
//		execute_button.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//
////				// Force an exception, for test purposes!
////				Log.d(this.getClass().toString(), "Forcing exception in the FCC Android app, to test Crittercism!");
////				String[] strings = { "a", "b", "c" };
////				for (int i = 0; i <= 1000; i++) {
////					String x = strings[i];
////				}
//
//				Intent intent = new Intent(
//						SamKnowsAggregateStatViewerActivity.this,
//						SamKnowsTestViewerActivity.class);
//
//				startActivityForResult(intent, 1);
//				overridePendingTransition(R.anim.transition_in,
//						R.anim.transition_out);
//
//			}
//		});

		Button run_now_choice_button;
		run_now_choice_button = (Button) findViewById(R.id.btnRunTest);

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

//		LinearLayout timeperiod_button2;
//		timeperiod_button2 = (LinearLayout) findViewById(R.id.timeperiod_header);
//
//		timeperiod_button2.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				SingleChoice();
//			}
//		});

		// page turn navigation button
		mShowArchivedResultsButton = (Button) findViewById(R.id.main_show_archived_results_button);

		mShowArchivedResultsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// This is how you scroll "right" to the next page (we always
				// start on page "zero"...)
				viewPager.setCurrentItem(1);
			}
		});
		
		// When we start-up, the button might be hidden or shown!
		mShowArchivedResultsButton.setVisibility((total_download_archive_records > 0) ? View.VISIBLE : View.INVISIBLE);
		
		
		// Network type results button, and associated text label.
     	setNetworkTypeToggleButton();
		
		// Set-up the button listener.
		mNetworkTypeToggleButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				String the_array_spinner[];
				the_array_spinner = new String[3];
				the_array_spinner[0] = getString(R.string.network_type_choose_mobile_results);
				the_array_spinner[1] = getString(R.string.network_type_choose_wifi_results);
				the_array_spinner[2] = getString(R.string.network_type_choose_all_results);

				Builder builder = new AlertDialog.Builder(FCCMainResultsActivity.this);
				builder.setTitle(getString(R.string.network_type_choose));

				builder.setItems(the_array_spinner, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						dialog.dismiss();
				
						boolean bChanged = false;
						if (which == 0) {
    						if (SKApplication.getNetworkTypeResults() != eNetworkTypeResults.eNetworkTypeResults_Mobile) {
    	  						SKApplication.setNetworkTypeResults(eNetworkTypeResults.eNetworkTypeResults_Mobile);
    	  						bChanged = true;
    						}
						} else if (which == 1) {
    						if (SKApplication.getNetworkTypeResults() != eNetworkTypeResults.eNetworkTypeResults_WiFi) {
    							SKApplication.setNetworkTypeResults(eNetworkTypeResults.eNetworkTypeResults_WiFi);
    	  						bChanged = true;
    						}
						} else { // if (which == 2) {
    						if (SKApplication.getNetworkTypeResults() != eNetworkTypeResults.eNetworkTypeResults_Any) {
    							SKApplication.setNetworkTypeResults(eNetworkTypeResults.eNetworkTypeResults_Any);
    	  						bChanged = true;
    						}
						}
						
    	  				if (bChanged) {
    	  					setNetworkTypeToggleButton();

    	  					// Update all the graphs etc.!!!
    	  					// Query average data, and update charts - this might make them invisible, if there is no data!
    	  					setTotalArchiveRecords();
    	  					adapter = new MyPagerAdapter(FCCMainResultsActivity.this);
    	  					//viewPager = (ViewPager) findViewById(R.id.viewPager);
    	  					SKLogger.sAssert(getClass(), viewPager == (ViewPager) findViewById(R.id.viewPager));
    	  					viewPager.setAdapter(adapter);

    	  					queryAverageDataAndUpdateTheCharts();

    	  					// And reload the grids!
    	  					clearGrid(R.id.download_results_tablelayout);
    	  					clearGrid(R.id.upload_results_tablelayout);
    	  					clearGrid(R.id.latency_results_tablelayout);
    	  					clearGrid(R.id.packetloss_results_tablelayout);
    	  					clearGrid(R.id.jitter_results_tablelayout);
    	  					adapter.loadGrids();
    	  				}
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

				clearGrid(R.id.download_results_tablelayout);
				loadDownloadGrid(TestResult.DOWNLOAD_TEST_ID,
						R.id.download_results_tablelayout, download_page_index,
						ITEMS_PER_PAGE);
				
				// And force a relayout, otherwise we get a huge chunk of white space on the screen!
         		findViewById(R.id.download_panel).requestLayout();
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
				clearGrid(R.id.download_results_tablelayout);
				loadDownloadGrid(TestResult.DOWNLOAD_TEST_ID,
						R.id.download_results_tablelayout, download_page_index,
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

				clearGrid(R.id.upload_results_tablelayout);
				loadDownloadGrid(TestResult.UPLOAD_TEST_ID,
						R.id.upload_results_tablelayout, upload_page_index,
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
				clearGrid(R.id.upload_results_tablelayout);
				loadDownloadGrid(TestResult.UPLOAD_TEST_ID,
						R.id.upload_results_tablelayout, upload_page_index,
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

				clearGrid(R.id.latency_results_tablelayout);
				loadDownloadGrid(TestResult.LATENCY_TEST_ID,
						R.id.latency_results_tablelayout, latency_page_index,
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
				clearGrid(R.id.latency_results_tablelayout);
				loadDownloadGrid(TestResult.LATENCY_TEST_ID,
						R.id.latency_results_tablelayout, latency_page_index,
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

				clearGrid(R.id.packetloss_results_tablelayout);
				loadDownloadGrid(TestResult.PACKETLOSS_TEST_ID,
						R.id.packetloss_results_tablelayout, packetloss_page_index,
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
				clearGrid(R.id.packetloss_results_tablelayout);
				loadDownloadGrid(TestResult.PACKETLOSS_TEST_ID,
						R.id.packetloss_results_tablelayout, packetloss_page_index,
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

				clearGrid(R.id.jitter_results_tablelayout);
				loadDownloadGrid(TestResult.JITTER_TEST_ID,
						R.id.jitter_results_tablelayout, jitter_page_index,
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
				clearGrid(R.id.jitter_results_tablelayout);
				loadDownloadGrid(TestResult.JITTER_TEST_ID,
						R.id.jitter_results_tablelayout, jitter_page_index,
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
				FCCMainResultsActivity.this).inflate(
				R.layout.fcc_main_results_activity_stat_grid_header, null);

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
				FCCMainResultsActivity.this).inflate(
				R.layout.fcc_main_results_activity_stat_grid, null);

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
				FCCMainResultsActivity.this).inflate(
				R.layout.fcc_main_results_activity_stat_grid_fail, null);

		((TextView) row.findViewById(R.id.stat_grid_timestamp))
				.setText(timestamp);
		((TextView) row.findViewById(R.id.stat_grid_location))
				.setText(location);
		((TextView) row.findViewById(R.id.stat_grid_result)).setText(result);

		table.addView(row);
	}

	private void clearGrid(int grid) {
		TableLayout table = (TableLayout) findViewById(grid);
//		table.setVisibility(View.GONE);
//		table.getLayoutParams().height = 0;
		
		int count = table.getChildCount();
		for (int i = 0; i < count; i++) {
			View child = table.getChildAt(i);
			((ViewGroup) child).removeAllViews();
		}
	}

	private void graphsSetup() {

		ViewGroup graphDownload =  (ViewGroup)subview.findViewById(R.id.download_graph);
		TextView downloadCaption =  (TextView)subview.findViewById(R.id.downloadCaption);
		ViewGroup graphUpload =  (ViewGroup)subview.findViewById(R.id.upload_graph);
		TextView uploadCaption =  (TextView)subview.findViewById(R.id.uploadCaption);
		ViewGroup graphLatency =  (ViewGroup)subview.findViewById(R.id.latency_graph);
		TextView latencyCaption =  (TextView)subview.findViewById(R.id.latencyCaption);
		ViewGroup graphPacketLoss =  (ViewGroup)subview.findViewById(R.id.packetloss_graph);
		TextView packetlossCaption =  (TextView)subview.findViewById(R.id.packetlossCaption);
		ViewGroup graphJitter =  (ViewGroup)subview.findViewById(R.id.jitter_graph);
		TextView jitterCaption =  (TextView)subview.findViewById(R.id.jitterCaption);

		graphHandlerDownload = new SamKnowsGraph(context, graphDownload, downloadCaption, "download");
		graphHandlerUpload = new SamKnowsGraph(context, graphUpload, uploadCaption, "upload");
		graphHandlerLatency = new SamKnowsGraph(context, graphLatency, latencyCaption, "latency");
		graphHandlerPacketLoss = new SamKnowsGraph(context, graphPacketLoss, packetlossCaption, "packetloss");
		graphHandlerJitter = new SamKnowsGraph(context, graphJitter, jitterCaption, "jitter");
	}

	/**
	 * Create the options menu that displays the refresh and about options
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.fcc_main_results_activity_menu, menu);
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
			Intent intent = new Intent(this, FCCAboutActivity.class);
			startActivity(intent);
			ret = true;
		} else if (R.id.menu_settings == itemId) {
			startActivity(new Intent(this, FCCPreferenceActivity.class));
			ret = true;
		} else if (R.id.menu_system_info == itemId) {
			startActivity(new Intent(this, FCCSystemInfoActivity.class));
			ret = true;
		} else if (R.id.menu_activation == itemId) {
			int size = SK2AppSettings.getInstance().getDevices().size();
			if (size == 0 || (size == 1 && OtherUtils.isPhoneAssosiated(this))) {
				SK2AppSettings.getInstance().setForceDownload();
			}
			
			startActivity(new Intent(this, FCCActivationActivity.class));
			finish();
			ret = true;
//		} else if (R.id.menu_map == itemId) {
//			// startActivity(new Intent(this, SamKnowsMapActivity.class));
//			startActivityForResult(new Intent(this, FCCMapActivity.class),
//					1);
//			ret = true;
		} else if (R.id.menu_terms_and_condition == itemId) {
			Intent intent = new Intent(this, FCCTermsOfUseActivity.class);
			startActivity(intent);
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
				SKLogger.e(FCCMainResultsActivity.class,
						"failed to get data", error);
				dialog.dismiss();
			}
		};
	}

	private ProgressDialog getProgressDialog(String message) {
		return ProgressDialog.show(FCCMainResultsActivity.this,
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

		private ArrayList<StatRecord> statRecords; // TODO - rename views to statRecords!

		@Override
		public void setPrimaryItem(ViewGroup container, int position,
				Object object) {
			current_page_view = (View) object;
			current_page_view_position = position;
		}
		
		FCCMainResultsActivity mMainResultsActivity;

		public MyPagerAdapter(FCCMainResultsActivity inMainResultsActivity) {
			
			mMainResultsActivity = inMainResultsActivity;
			
			statRecords = new ArrayList<StatRecord>();
			statRecords.add(new StatRecord());
			// views.get(0) is the aggregate view

			JSONObject summary = dbHelper.getArchiveDataSummary();

			try {
				total_archive_records = summary.getInt("counter");
				String last_run_test = summary.getString("enddate");

				if (Long.parseLong(last_run_test) != 0) {
					last_run_test_formatted = new SKDateFormat(context)
							.UITime(Long.parseLong(last_run_test));

				} else {
					last_run_test_formatted = "";
				}
			} catch (JSONException e1) {
				SKLogger.e(this, "Error in reading from JSONObject.", e1);
			}

			for (int i = 0; i < total_archive_records; i++) {
				statRecords.add(new StatRecord());
				// load blank records ready for populating
			}
		}

		public void readArchiveItem(int archiveItemIndex) {
			JSONObject archive;
			try {

				archive = dbHelper.getArchiveData(archiveItemIndex);

			} catch (Exception e) {
				SKLogger.e(this, "Error in reading archive item " + archiveItemIndex, e);
				SKLogger.sAssert(getClass(), false);
				return;
			}
			
			if (archive == null) {
				SKLogger.sAssert(getClass(), false);
				return;
			}

			// read headers of json
			String datetime = "";
			String dtime_formatted;
			try {
				datetime = archive.getString("dtime");

				dtime_formatted = new SKDateFormat(context).UITime(Long
						.parseLong(datetime));
				statRecords.get(archiveItemIndex + 1).time_stamp = dtime_formatted;

			} catch (JSONException e1) {
				e1.printStackTrace();
			}

			// unpack activemetrics
			JSONArray results = null;

			try {
				results = archive.getJSONArray("activemetrics");
			} catch (JSONException je) {
				SKLogger.e(this, "Exception in reading active metrics array: "
						+ je.getMessage());
			}

			if (results != null) {
				int itemcount = 0;

				for (itemcount = 0; itemcount < results.length(); itemcount++) {

					JSONObject user = null;
					try {
						user = results.getJSONObject(itemcount);
					} catch (JSONException je) {
						SKLogger.e(
								this,
								"Exception in reading JSONObject: "
										+ je.getMessage());
					}

					if (user != null) {
						try {
							String testnumber = user.getString("test");
							String location = user.getString("location");
							String success = user.getString("success");
							String hrresult = user.getString("hrresult");

							if (success.equals("0")) {
								hrresult = getString(R.string.failed);
							}

							if (testnumber.equals("" + TestResult.UPLOAD_TEST_ID)) {
								statRecords.get(archiveItemIndex + 1).tests_location = location;
								statRecords.get(archiveItemIndex + 1).upload_location = location;
								statRecords.get(archiveItemIndex + 1).upload_result = hrresult;

							}
							if (testnumber.equals("" + TestResult.DOWNLOAD_TEST_ID)) {
								statRecords.get(archiveItemIndex + 1).tests_location = location;
								statRecords.get(archiveItemIndex + 1).download_location = location;
								statRecords.get(archiveItemIndex + 1).download_result = hrresult;
							}

							if (testnumber.equals("" + TestResult.LATENCY_TEST_ID)) {
								statRecords.get(archiveItemIndex + 1).tests_location = location;
								statRecords.get(archiveItemIndex + 1).latency_location = location;
								statRecords.get(archiveItemIndex + 1).latency_result = hrresult;
							}

							if (testnumber.equals("" + TestResult.PACKETLOSS_TEST_ID)) {
								statRecords.get(archiveItemIndex + 1).tests_location = location;
								statRecords.get(archiveItemIndex + 1).packetloss_location = location;
								statRecords.get(archiveItemIndex + 1).packetloss_result = hrresult;
							}

							if (testnumber.equals("" + TestResult.JITTER_TEST_ID)) {
								statRecords.get(archiveItemIndex + 1).tests_location = location;
								statRecords.get(archiveItemIndex + 1).jitter_location = location;
								statRecords.get(archiveItemIndex + 1).jitter_result = hrresult;
							}

						} catch (JSONException je) {
							SKLogger.e(
									this,
									"Exception in reading JSONObject: "
											+ je.getMessage());
						}
					}
				}
			}

			// Log.d(this.getClass().toString(), "*** SamKnowsAggregateStatViewerActivity:UNPACK PASSIVE METRICS!!");
			
			// unpack passivemetrics
			results = null;
			try {
				results = archive.getJSONArray("passivemetrics");
			} catch (JSONException je) {
				SKLogger.e(this,
						"Exception in reading JSONObject: " + je.getMessage());
			}

			if (results != null) {
				int itemcount = 0;
				
				for (itemcount = 0; itemcount < results.length(); itemcount++) {
					JSONObject user = null;
					try {
						user = results.getJSONObject(itemcount);
					} catch (JSONException je) {
						SKLogger.e(
								this,
								"Exception in reading JSONObject: "
										+ je.getMessage());
						user = null;
					}
				
					if (user == null) {
					  continue;
					}
						
					captureUserMetricAtArchiveItemIndex(archiveItemIndex, user);
				}
			}

		}

		private void captureUserMetricAtArchiveItemIndex(int archiveItemIndex,
				JSONObject user) {
			if (user == null) {
				Log.e(this.getClass().toString(), "captureUserMetricAtArchiveItemIndex - user == null");
				return;
			}

			
			try {
				String metric = user.getString("metric");
				String value = user.getString("value");
				String type = user.getString("type");
				
				//Log.d("*** SamKnowsAggregateStatViewerActivity:MyPagerAdapter", "INFO - metric (" + metric +"), type=(" + type + "), value=(" + value + ")");

				// There is a completed disconnect between the integer metric value
				// as considered by the PassiveMetric class, and the layout "passive metric"
				// identifiers, such as R.id.passivemetric20.
				// The only safe thing to do, is to use as String value to determine
				// which resource id to use.

				if (metric.equals("connected")) { // connected
					statRecords.get(archiveItemIndex + 1).passivemetric1 = value;
					statRecords.get(archiveItemIndex + 1).passivemetric1_type = type;
				} else if (metric.equals("connectivitytype")) { // connectivity
					// type
					statRecords.get(archiveItemIndex + 1).passivemetric2 = value;
					statRecords.get(archiveItemIndex + 1).passivemetric2_type = type;
				} else if (metric.equals("gsmcelltowerid")) { // cell tower id
					// TODO - Giancarlo says this isn't displayed in SamKnowsAggregateStatViewerActivity - "Archived Result" (archive_result)
					// METRIC_TYPE.GSMCID("gsmcelltowerid")
					statRecords.get(archiveItemIndex + 1).passivemetric3 = value;
					statRecords.get(archiveItemIndex + 1).passivemetric3_type = type;
				} else if (metric.equals("gsmlocationareacode")) { // cell tower
					// location area
					statRecords.get(archiveItemIndex + 1).passivemetric4 = value;
					statRecords.get(archiveItemIndex + 1).passivemetric4_type = type;
				} else if (metric.equals("gsmsignalstrength")) { // signal strength
					statRecords.get(archiveItemIndex + 1).passivemetric5 = value;
					statRecords.get(archiveItemIndex + 1).passivemetric5_type = type;
				} else if (metric.equals("networktype")) { // bearer
					statRecords.get(archiveItemIndex + 1).passivemetric6 = value;
					statRecords.get(archiveItemIndex + 1).passivemetric6_type = type;
				} else if (metric.equals("networkoperatorname")) { // network
					// operator
					statRecords.get(archiveItemIndex + 1).passivemetric7 = value;
					statRecords.get(archiveItemIndex + 1).passivemetric7_type = type;
				} else if (metric.equals("latitude")) { // latitude
					statRecords.get(archiveItemIndex + 1).passivemetric8 = 
							SKCommon.sGetDecimalStringAnyLocaleAs1Pt5LocalisedString(value);
					statRecords.get(archiveItemIndex + 1).passivemetric8_type = type;
				} else if (metric.equals("longitude")) { // longitude
					statRecords.get(archiveItemIndex + 1).passivemetric9 = 
							SKCommon.sGetDecimalStringAnyLocaleAs1Pt5LocalisedString(value);
					statRecords.get(archiveItemIndex + 1).passivemetric9_type = type;
				} else if (metric.equals("accuracy")) { // accuracy
					statRecords.get(archiveItemIndex + 1).passivemetric10 = value;
					statRecords.get(archiveItemIndex + 1).passivemetric10_type = type;
				} else if (metric.equals("locationprovider")) { // location
					// provider
					statRecords.get(archiveItemIndex + 1).passivemetric11 = value;
					statRecords.get(archiveItemIndex + 1).passivemetric11_type = type;
				} else if (metric.equals("simoperatorcode")) { // sim operator code
					statRecords.get(archiveItemIndex + 1).passivemetric12 = value;
					statRecords.get(archiveItemIndex + 1).passivemetric12_type = type;
				} else if (metric.equals("simoperatorname")) { // sim operator name
					statRecords.get(archiveItemIndex + 1).passivemetric13 = value;
					statRecords.get(archiveItemIndex + 1).passivemetric13_type = type;
				} else if (metric.equals("imei")) { // imei
					statRecords.get(archiveItemIndex + 1).passivemetric14 = value;
					statRecords.get(archiveItemIndex + 1).passivemetric14_type = type;
				} else if (metric.equals("imsi")) { // imsi
					statRecords.get(archiveItemIndex + 1).passivemetric15 = value;
					statRecords.get(archiveItemIndex + 1).passivemetric15_type = type;
				} else if (metric.equals("manufactor")) { // manufacturer
					statRecords.get(archiveItemIndex + 1).passivemetric16 = value;
					statRecords.get(archiveItemIndex + 1).passivemetric16_type = type;
				} else if (metric.equals("model")) { // model
					statRecords.get(archiveItemIndex + 1).passivemetric17 = value;
					statRecords.get(archiveItemIndex + 1).passivemetric17_type = type;
				} else if (metric.equals("ostype")) { // os type
					statRecords.get(archiveItemIndex + 1).passivemetric18 = value;
					statRecords.get(archiveItemIndex + 1).passivemetric18_type = type;
				} else if (metric.equals("osversion")) { // os version
					statRecords.get(archiveItemIndex + 1).passivemetric19 = value;
					statRecords.get(archiveItemIndex + 1).passivemetric19_type = type;
				} else if (metric.equals("gsmbiterrorrate")) { // gsmbiterrorrate
					statRecords.get(archiveItemIndex + 1).passivemetric20 = value;
					statRecords.get(archiveItemIndex + 1).passivemetric20_type = type;
				} else if (metric.equals("cdmaecio")) { // cdmaecio
					statRecords.get(archiveItemIndex + 1).passivemetric21 = value;
					statRecords.get(archiveItemIndex + 1).passivemetric21_type = type;
				} else if (metric.equals("phonetype")) { // phone type
					// TODO - Giancarlo says this isn't displayed in SamKnowsAggregateStatViewerActivity - "Archived Result" (archive_result)
					// TODO - the phone type is NOT SHOWN! MODEL("model")
					statRecords.get(archiveItemIndex + 1).passivemetric22 = value;
					statRecords.get(archiveItemIndex + 1).passivemetric22_type = type;
				} else if (metric.equals("activenetworktype")) { // active network
					// TODO
					// type
					if(value.length() > 0 ){
						String new_value = value.substring(0, 1).toUpperCase() + value.substring(1);
						statRecords.get(archiveItemIndex + 1).active_network_type = "("+new_value+")";
					}

					//views.get(i + 1).passivemetric23 = value;
					//views.get(i + 1).passivemetric23_type = type;
				} else if (metric.equals("connectionstatus")) { // connection
					// status
					statRecords.get(archiveItemIndex + 1).passivemetric24 = value;
					statRecords.get(archiveItemIndex + 1).passivemetric24_type = type;
				} else if (metric.equals("roamingstatus")) { // roaming status
					statRecords.get(archiveItemIndex + 1).passivemetric25 = value;
					statRecords.get(archiveItemIndex + 1).passivemetric25_type = type;
				} else if (metric.equals("networkoperatorcode")) { // network
					// operator code
					statRecords.get(archiveItemIndex + 1).passivemetric26 = value;
					statRecords.get(archiveItemIndex + 1).passivemetric26_type = type;
				} else if (metric.equals("cdmasignalstrength")) { // cdmasignalstrength
					statRecords.get(archiveItemIndex + 1).passivemetric27 = value;
					statRecords.get(archiveItemIndex + 1).passivemetric27_type = type;
				} else if (metric.equals("cdmabasestationid")) { // cdmabasestationid
					statRecords.get(archiveItemIndex + 1).passivemetric28 = value;
					statRecords.get(archiveItemIndex + 1).passivemetric28_type = type;
				} else if (metric.equals("cdmabasestationlatitude")) { // cdmabasestationlatitude
					statRecords.get(archiveItemIndex + 1).passivemetric29 = value;
					statRecords.get(archiveItemIndex + 1).passivemetric29_type = type;
				} else if (metric.equals("cdmabasestationlongitude")) { // cdmabasestationlongitude
					statRecords.get(archiveItemIndex + 1).passivemetric30 = value;
					statRecords.get(archiveItemIndex + 1).passivemetric30_type = type;
				} else if (metric.equals("cdmanetworkid")) { // cdmanetworkid
					statRecords.get(archiveItemIndex + 1).passivemetric31 = value;
					statRecords.get(archiveItemIndex + 1).passivemetric31_type = type;
				} else if (metric.equals("cdmasystemid")) { // cdmasystemid
					statRecords.get(archiveItemIndex + 1).passivemetric32 = value;
					statRecords.get(archiveItemIndex + 1).passivemetric32_type = type;
				} else {
					Log.d("SamKnowsAggregateStatViewerActivity:MyPagerAdapter", "WARNING - unsupported metric (" + metric +")");
				}

			} catch (JSONException je) {
				Log.d("SamKnowsAggregateStatViewerActivity:MyPagerAdapter", "ERROR - exception reading JSON object (" + je.getMessage() +")");
				
				SKLogger.e(
						this,
						"Exception in reading JSONObject: "
								+ je.getMessage());
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
			return statRecords.size();
		}

		@Override
		public Object instantiateItem(View view, int position) {
			
			StatView sc = new StatView(FCCMainResultsActivity.this);
			sc.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT));
			sc.setFillViewport(true);

			LayoutInflater inflater = (LayoutInflater) FCCMainResultsActivity.this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);



			View subview_archive;

			// If position is zero take care of the visibility of the messages
			if(position == 0){
				subview = inflater.inflate(R.layout.fcc_main_results_activity_runnow_and_graphs, null);
				subview.setTag(position);


				sc.addView(subview);

				((ViewPager) view).addView(sc);
				//if there is a problem with with the state machine display the 
				//appropriate message

				if (!SK2AppSettings.getSK2AppSettingsInstance().stateMachineStatus()) {
					// We are NOT activated!
					TextView tv = (TextView) subview.findViewById(R.id.no_data_message_text);
					tv.setText(R.string.activation_needed);
    				((TextView) subview.findViewById(R.id.test_last_run)).setVisibility(View.INVISIBLE);
				} else {
					// We are activated!
					
					if (total_archive_records == 0) {
						// No data!
        				((TextView) subview.findViewById(R.id.test_last_run)).setVisibility(View.INVISIBLE);
					} else {
						// Data!
						TextView tv = (TextView) subview.findViewById(R.id.no_data_message_text);
						ImageView iv = (ImageView) subview.findViewById(R.id.no_data_message_image);
						tv.setVisibility(View.GONE);
						iv.setVisibility(View.GONE);
						// data icon
						// & message
						((TextView) subview.findViewById(R.id.test_last_run)).setVisibility(View.VISIBLE);
					}
				}
				
				//in case there are results to display load it
				//no matter if the state machine status
				if(total_archive_records > 0){

					adapter.readArchiveItem(0);
				}
				
				String caption = getString(R.string.last_run) + " " + last_run_test_formatted;
				((TextView) subview.findViewById(R.id.test_last_run)).setText(caption);
				
				// For FCC app, we ALWAYS hide the display of Jitter results!
				subview.findViewById(R.id.jitter_panel).setVisibility(View.GONE);

				buttonSetup();

				loadAverage();
				graphsSetup();

				loadGrids();
			}
			
			subview_archive = inflater.inflate(R.layout.fcc_main_results_activity_single_result, null);

			//
			// Show or hide the passive results, depending on whether
			// we're looking at mobile (show) or WiFi (hide!)
			//
			LinearLayout passiveResultsLinearLayout = (LinearLayout)subview_archive.findViewById(R.id.passive_results_linearlayout);
			if (SKApplication.getNetworkTypeResults() == eNetworkTypeResults.eNetworkTypeResults_WiFi) {
				// Hide for WiFi results!
				passiveResultsLinearLayout.setVisibility(View.GONE);
			} else {
				// Show for Mobile results!
				passiveResultsLinearLayout.setVisibility(View.VISIBLE);
			}

			//
			// Prepare left/right buttons etc. ..
			//
			Button leftButton = (Button) subview_archive.findViewById(R.id.page_turn_left);
			Button rightButton = (Button) subview_archive.findViewById(R.id.page_turn_right);
			if (position == total_archive_records) {
			  rightButton.setVisibility(View.INVISIBLE);
			} else {
			  rightButton.setVisibility(View.VISIBLE);
			}
			
			leftButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int currentPage = viewPager.getCurrentItem();
           		    //mMainResultsActivity.handleOnPageSelected(currentPage - 1);
    				// This is how you scroll "left" to the previous page (we always
    				// start on page "zero"...)
    				viewPager.setCurrentItem(currentPage - 1);
				}
			});
			
			rightButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int currentPage = viewPager.getCurrentItem();
             		// mMainResultsActivity.handleOnPageSelected(currentPage + 1);
    				// This is how you scroll "right" to the previous page (we always
    				// start on page "zero"...)
					
					// https://code.google.com/p/android/issues/detail?id=27526
					int targetPage = currentPage + 1;
					int pageCount = viewPager.getChildCount();
					//SKLogger.sAssert(getClass(),  pageCount >= currentPage);
					
    				viewPager.setCurrentItem(targetPage);
					//int testCurrentPage = viewPager.getCurrentItem();
					//SKLogger.sAssert(getClass(),  testCurrentPage == (currentPage+1));
				}
			});

			if (position > 0) {


				subview_archive.setTag(position);
				sc.addView(subview_archive);

				StatRecord sr = statRecords.get(position);

				sc.setData(sr);

				((ViewPager) view).addView(sc);

				if (position == total_archive_records) {
					sc.setRightPageIndicator(false);
				}

				// http://stackoverflow.com/questions/9235374/confusion-on-position-in-instantiateitem-function-in-class-pageradapter-andro
				// Inside of instantiateItem, the position parameter is the position that is in need of rendering.
				// It is NOT the position of the currently focused item that the user would see.
				// The pages to the left and right of the currently displayed view need to be pre rendered in memory
				// so that the animations to those screens will be smooth.
				if (position < total_archive_records) {
					// Position (page number) is:
					//    0, 1, 2
					// Archive item index is:
					//    NA 0  1
					// It looks like we should pass (position-1) here, but if we do that, we get un-populated
					// archived results pages!
					// TODO - figure out why that is the case.
					adapter.readArchiveItem(position);
				}
			}

			Util.overrideFonts(FCCMainResultsActivity.this, sc);
			return sc;

		}

		public void loadGrids() {

			loadDownloadGrid(TestResult.DOWNLOAD_TEST_ID,
					R.id.download_results_tablelayout, 0, ITEMS_PER_PAGE);
			loadDownloadGrid(TestResult.UPLOAD_TEST_ID,
					R.id.upload_results_tablelayout, 0, ITEMS_PER_PAGE);
			loadDownloadGrid(TestResult.LATENCY_TEST_ID,
					R.id.latency_results_tablelayout, 0, ITEMS_PER_PAGE);
			loadDownloadGrid(TestResult.PACKETLOSS_TEST_ID,
					R.id.packetloss_results_tablelayout, 0, ITEMS_PER_PAGE);
			loadDownloadGrid(TestResult.JITTER_TEST_ID,
					R.id.jitter_results_tablelayout, 0, ITEMS_PER_PAGE);

			LinearLayout l = (LinearLayout) findViewById(R.id.download_content);
			l.setVisibility(View.GONE);
			l.getLayoutParams().height = 0;

			l = (LinearLayout) findViewById(R.id.upload_content);
			l.setVisibility(View.GONE);
			l.getLayoutParams().height = 0;

			l = (LinearLayout) findViewById(R.id.latency_content);
			l.setVisibility(View.GONE);
			l.getLayoutParams().height = 0;

			l = (LinearLayout) findViewById(R.id.packetloss_content);
			l.setVisibility(View.GONE);
			l.getLayoutParams().height = 0;

			l = (LinearLayout) findViewById(R.id.jitter_content);
			l.setVisibility(View.GONE);
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
	
	private int mWeeks = 1;

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

				String value = array_spinner[which];
				if (value.equals(getString(R.string.time_period_1week))) {
					mWeeks = 1;
				} else if (value.equals(getString(R.string.time_period_1month))) {
					mWeeks = 4;
				} else if (value.equals(getString(R.string.time_period_3months))) {
					mWeeks = 12;
				} else if (value.equals(getString(R.string.time_period_1year))) {
					mWeeks = 52;
				} else {
					Log.e(this.getClass().toString(), "onClick - value out of range=" + value);
				}

				Button tvHeader = (Button) findViewById(R.id.btn_timeperiod);
				String caption = getString(R.string.results_for) + " " + array_spinner[which];
				tvHeader.setText(caption);
				
				// Query average data, and update charts - this might make them invisible, if there is no data!
				queryAverageDataAndUpdateTheCharts();

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
	
	// Query average data, and update charts - this might make them invisible, if there is no data!
	void queryAverageDataAndUpdateTheCharts() {
		loadAverage();

		// Update charts - this might make them invisible, if there is no data!
		setGraphDataForColumnIdAndHideIfNoResultsFound(TestResult.DOWNLOAD_TEST_ID);
		setGraphDataForColumnIdAndHideIfNoResultsFound(TestResult.UPLOAD_TEST_ID);
		setGraphDataForColumnIdAndHideIfNoResultsFound(TestResult.LATENCY_TEST_ID);
		setGraphDataForColumnIdAndHideIfNoResultsFound(TestResult.PACKETLOSS_TEST_ID);
		setGraphDataForColumnIdAndHideIfNoResultsFound(TestResult.JITTER_TEST_ID);
	}

	private void RunChoice() {

		storage = CachingStorage.getInstance();
		config = storage.loadScheduleConfig();
		// if config == null the app is not been activate and
		// no test can be run
		if (config == null) {
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
						FCCMainResultsActivity.this,
						FCCRunningTestActivity.class);
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
	
	
	public boolean forceBackToAllowClose() {
		if (on_aggregate_page) {
			return true;
		}
		return false;
	}

	@Override
	public void onBackPressed() {
	
		// This screen has strange behaviour on the "Back" button being pressed.
		// It is the task root - so, normally, pressing back would terminate the application.
		// However, pressing back actually "changes page" until we reach the "aggregate page";
		// at which point, we can try to close the app if required.

		if (on_aggregate_page) {
			// The app can be closed from this page - we expect to be the task root.
			if (this.wouldBackButtonReturnMeToTheHomeScreen()) {
				super.onBackPressed();
				return;
			}
		} else {
			// The app must NOT be closed from this page - change "viewed page" instead.
			//viewPager = (ViewPager) findViewById(R.id.viewPager);
			// viewPager.setAdapter(adapter);
			viewPager.setCurrentItem(0, true); // The true means to perform a smooth scroll!
			// overridePendingTransition(0, 0);
			on_aggregate_page = true;
		}
	}

	// This method will query the data synchronously, for the specified column.

	private JSONObject getDataForColumnId(int PColumnId) {
		Calendar fromCal = Calendar.getInstance();
		fromCal.add(Calendar.WEEK_OF_YEAR, mWeeks * -1);
		long startTime = fromCal.getTimeInMillis();

		Calendar upToCal = Calendar.getInstance();
		long upToTime = upToCal.getTimeInMillis();
		
		if (!(startTime < upToTime)) {
			Log.e(this.getClass().toString(), "getDataForColumnId - startTime/upToTime out of range mis-matched");
		}
		
		JSONObject data = dbHelper.getGraphData(PColumnId, startTime, upToTime);	
		return data;
	}
	

    // This method will set the graph data and update, based on the specified column;
	// and will make the container layout invisible if there are no results in that data.
	//
	// GIVEN: the response data has been queried synchronously from the local database for the specified column
	// WHEN: there are NO results in the response data
	// THEN: the graph layout will be made invisible (GONE)
	
	private boolean setGraphDataForColumnIdAndHideIfNoResultsFound(int PColumnId) {
		boolean buttonFound = false;
		
		JSONObject data = null;
		try {
			data = getDataForColumnId(PColumnId);
			if (data.getJSONArray("results").length() > 0) {
				buttonFound = true;
			}
		} catch (JSONException e1) {
		}
		
		LinearLayout l = null;

		switch (PColumnId) {
		case TestResult.DOWNLOAD_TEST_ID:
			l = (LinearLayout) findViewById(R.id.download_content);
			graphHandlerDownload.setData(data);
			graphHandlerDownload.update();
			break;
		case TestResult.UPLOAD_TEST_ID:
			l = (LinearLayout) findViewById(R.id.upload_content);
			graphHandlerUpload.setData(data);
			graphHandlerUpload.update();
			break;
		case TestResult.LATENCY_TEST_ID:
			l = (LinearLayout) findViewById(R.id.latency_content);
			graphHandlerLatency.setData(data);
			graphHandlerLatency.update();
			break;
		case TestResult.PACKETLOSS_TEST_ID:
			l = (LinearLayout) findViewById(R.id.packetloss_content);
			graphHandlerPacketLoss.setData(data);
			graphHandlerPacketLoss.update();
			break;
		case TestResult.JITTER_TEST_ID:
			l = (LinearLayout) findViewById(R.id.jitter_content);
			graphHandlerJitter.setData(data);
			graphHandlerJitter.update();
			break;
		default:
			Log.e(this.getClass().toString(), "setGraphDataForColumnIdAndHideIfNoResultsFound - unexpected result");
			return buttonFound;
		}
		
		if (l == null) {
			Log.e(this.getClass().toString(), "setGraphDataForColumnIdAndHideIfNoResultsFound - l == null");
		}

		// Changed to NO LONGER HIDE if not visible; as that is unhelpful
		// behaviour now that we have a network type results toggle!
//		if (buttonFound) {
////			if (PForceVisibleIfThereIsData == false) {
////				// Button found, but do NOT set visibility to VISIBLE.
////			} else {
////				// Set button visibility to VISIBLE.
////				l.setVisibility(View.VISIBLE);
////			}
//		} else {
//		    // Not found!
//   		    l.setVisibility(View.GONE);
//		}
		
		return buttonFound;
	}

	@Override
	public void onClick(View v) {
		// Toast.makeText(this,"clicked ..."+v.getId(),3000).show();

		int grid = 0;
		boolean buttonfound = false;
		ImageView button = null;
		LinearLayout l = null;
		int testid = 0;

		int id = v.getId();
		if (id == R.id.download_header || id == R.id.btn_download_toggle) {
   		    testid = TestResult.DOWNLOAD_TEST_ID;
			buttonfound = setGraphDataForColumnIdAndHideIfNoResultsFound(testid);
			button = (ImageView) findViewById(R.id.btn_download_toggle);
			l = (LinearLayout) findViewById(R.id.download_content);
			grid = R.id.download_results_tablelayout;
		}

		if (id == R.id.upload_header || id == R.id.btn_upload_toggle) {
   		    testid = TestResult.UPLOAD_TEST_ID;
			buttonfound = setGraphDataForColumnIdAndHideIfNoResultsFound(testid);
			button = (ImageView) findViewById(R.id.btn_upload_toggle);
			l = (LinearLayout) findViewById(R.id.upload_content);
			grid = R.id.upload_results_tablelayout;
		}

		if (id == R.id.latency_header || id == R.id.btn_latency_toggle) {
   		    testid = TestResult.LATENCY_TEST_ID;
			buttonfound = setGraphDataForColumnIdAndHideIfNoResultsFound(testid);
			button = (ImageView) findViewById(R.id.btn_latency_toggle);
			l = (LinearLayout) findViewById(R.id.latency_content);
			grid = R.id.latency_results_tablelayout;
		}

		if (id == R.id.packetloss_header || id == R.id.btn_packetloss_toggle) {
   		    testid = TestResult.PACKETLOSS_TEST_ID;
			buttonfound = setGraphDataForColumnIdAndHideIfNoResultsFound(testid);
			button = (ImageView) findViewById(R.id.btn_packetloss_toggle);
			l = (LinearLayout) findViewById(R.id.packetloss_content);
			grid = R.id.packetloss_results_tablelayout;
		}

		if (id == R.id.jitter_header || id == R.id.btn_jitter_toggle) {
   		    testid = TestResult.JITTER_TEST_ID;
			buttonfound = setGraphDataForColumnIdAndHideIfNoResultsFound(testid);
			button = (ImageView) findViewById(R.id.btn_jitter_toggle);
			l = (LinearLayout) findViewById(R.id.jitter_content);
			grid = R.id.jitter_results_tablelayout;
		}

		// actions

		if (l.getVisibility() != View.VISIBLE) {

			button.setBackgroundResource(R.drawable.btn_open_selector);
			button.setContentDescription(getString(R.string.close_panel));
			// graphHandler1.update();
			clearGrid(grid);
			loadDownloadGrid(testid, grid, 0, ITEMS_PER_PAGE);

			l.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
			l.setVisibility(View.VISIBLE);

		} else {

			l.setVisibility(View.GONE);

			button.setBackgroundResource(R.drawable.btn_closed_selector);
			button.setContentDescription(getString(R.string.open_panel));
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

}
