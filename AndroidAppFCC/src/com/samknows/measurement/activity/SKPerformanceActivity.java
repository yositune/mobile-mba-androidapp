package com.samknows.measurement.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.samknows.libcore.SKConstants;
import com.samknows.measurement.FCCAppSettings;
import com.samknows.measurement.CachingStorage;
import com.samknows.measurement.MainService;
import com.samknows.measurement.R;
import com.samknows.measurement.Storage;
import com.samknows.measurement.schedule.ScheduleConfig;
import com.samknows.measurement.schedule.TestDescription;
import com.samknows.measurement.statemachine.State;

public class SKPerformanceActivity extends BaseLogoutActivity {
	private boolean canceled;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		startService(new Intent(this, MainService.class));
		canceled = false;
		new AsyncTask<Void, Void, ScheduleConfig>() {

			@Override
			protected ScheduleConfig doInBackground(Void... params) {
				Storage storage = CachingStorage.getInstance();
				ScheduleConfig config = storage.loadScheduleConfig(); //TODO refacto this block to ServiceInfoManager if new data will appear
				while (!canceled && config == null && (MainService.isExecuting() || FCCAppSettings.getFCCAppSettingsInstance().getState() == State.NONE)) {
					SystemClock.sleep(1000);
					config = storage.loadScheduleConfig();
				}
				return config;
			}

			@Override
			protected void onPostExecute(final ScheduleConfig config) {
				super.onPostExecute(config);
				removeDialog(LOADING_DIALOG);
				if (config != null) {
					buildView(config);
				} else if (!canceled){
					showDialog(FAIL_TO_LOAD_CONFIG);
				}
			}
			
		}.execute();
		showDialog(LOADING_DIALOG);
	}

	private void buildView(ScheduleConfig config) {
		if (!FCCAppSettings.getInstance().wasIntroShown()) {
			showDialog(INTRO);
			FCCAppSettings.getInstance().saveIntroShown(true);
		}
		
		TableLayout mainTable = (TableLayout) findViewById(R.id.main_table);
		for (final TestDescription td : config.tests) {
			TableRow tr = new TableRow(this);

			TextView label = new TextView(this);
			label.setText(td.displayName);
			label.setMaxEms(10);
			label.setTextSize(20);
			label.setSingleLine(false);
			tr.addView(label);

			Button btn = new Button(this);
			btn.setText("   Run test   ");
			btn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (!td.canExecute()) {
						Toast.makeText(getBaseContext(), getString(R.string.cant_run_test), Toast.LENGTH_SHORT).show();
					} else {
						//Intent intent = new Intent(SKPerformanceActivity.this, RunTestActivity.class);
						//intent.putExtra(Constants.INTENT_EXTRA_TD, td);
						//startActivity(intent);
					}
				}
			});

			tr.addView(btn);

			mainTable.addView(tr, new TableLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		}
		mainTable.invalidate();
		
		((Button)findViewById(R.id.btn_logs)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(SKPerformanceActivity.this, SKLogsActivity.class));
			}
		});
		((Button)findViewById(R.id.btn_stats)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//startActivity(new Intent(SKPerformanceActivity.this, SamKnowsMainStatTabActivity.class));
			}
		});
		((Button)findViewById(R.id.btn_logs)).setVisibility(SKConstants.DEBUG ? View.VISIBLE : View.GONE);
		
		((Button)findViewById(R.id.btn_system_info)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(SKPerformanceActivity.this, SamKnowsInfoActivity.class));
			}
		});
		((Button)findViewById(R.id.btn_test_results)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(SKPerformanceActivity.this, TestResultsTabActivity.class));
			}
		});
	}

	private static final int LOADING_DIALOG = 1;
	private static final int FAIL_TO_LOAD_CONFIG = 2;
	private static final int INTRO = 3;
	private static final int ABOUT = 4;
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == LOADING_DIALOG) { //TODO change to switch
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setCancelable(true);
			dialog.setMessage(getText(R.string.loading_config));
			dialog.setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					canceled = true;
					finish();
				}
			});
			return dialog;
		} else if (id == FAIL_TO_LOAD_CONFIG) {
			AlertDialog dialog = new AlertDialog.Builder(this).create();
			dialog.setMessage(getText(R.string.failed_to_load_config));
			dialog.setButton(AlertDialog.BUTTON_POSITIVE, getText(R.string.ok), (android.content.DialogInterface.OnClickListener)null);
			return dialog;
		} else if (id == INTRO) {
			AlertDialog dialog = new AlertDialog.Builder(this).create();
			dialog.setMessage(getText(R.string.intro_text));
			dialog.setButton(AlertDialog.BUTTON_POSITIVE, getText(R.string.ok), (android.content.DialogInterface.OnClickListener)null);
			return dialog;
		} else if (id == ABOUT) {
			AlertDialog dialog = new AlertDialog.Builder(this).create();
			dialog.setMessage(getText(R.string.about_text));
			dialog.setButton(AlertDialog.BUTTON_POSITIVE, getText(R.string.ok), (android.content.DialogInterface.OnClickListener)null);
			return dialog;
		}
		return super.onCreateDialog(id);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_settings) {
			startActivity(new Intent(this, SKPreferenceActivity.class));
			return true;
		} else if (item.getItemId() == R.id.menu_about) {
			showDialog(ABOUT);
		}
		return super.onOptionsItemSelected(item);
	}
	
	

}