package com.samknows.measurement.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import com.samknows.measurement.R;
import com.samknows.measurement.test.outparcer.ParcerDataType;
import com.samknows.measurement.test.outparcer.ParcerFieldType;

public class TestResultsTabActivity extends TabActivity{
	protected static String TYPE_JHTTPGET = "JHTTPGET";
	protected static String TYPE_JHTTPPOST = "JHTTPPOST";
	protected static String TYPE_LATENCY = "JUDPLATENCY";
	protected static String TYPE_JITTER = "JUDPJITTER";
	protected static String TYPE_CPU = "CPUACTIVITY";
	protected static String TYPE_CLOSEST_TARGET = "CLOSESTTARGET";
	protected static final String PARAM_RESULT_TYPE = "PARAM_RESULT_TYPE";
	protected static final String PARAM_CONTENT_CONFIG = "PARAM_CONTENT_CONFIG";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		TabHost host = getTabHost();
		TabSpec tabSpec = host.newTabSpec("TAG1");
		Intent intent = new Intent(this, TestResultsActivity.class);
		intent.putExtra(PARAM_RESULT_TYPE, TYPE_JHTTPGET);
		intent.putExtra(PARAM_CONTENT_CONFIG, (Serializable)getDownloadUploadConfig());
		tabSpec.setContent(intent);
		tabSpec.setIndicator(getString(R.string.download));
		host.addTab(tabSpec);
		
		tabSpec = host.newTabSpec("TAG2");
		intent = new Intent(this, TestResultsActivity.class);
		intent.putExtra(PARAM_RESULT_TYPE, TYPE_JHTTPPOST);
		intent.putExtra(PARAM_CONTENT_CONFIG, (Serializable)getDownloadUploadConfig());
		tabSpec.setContent(intent);
		tabSpec.setIndicator(getString(R.string.upload));
		host.addTab(tabSpec);
		
		tabSpec = host.newTabSpec("TAG3");
		intent = new Intent(this, TestResultsActivity.class);
		intent.putExtra(PARAM_RESULT_TYPE, TYPE_LATENCY);
		intent.putExtra(PARAM_CONTENT_CONFIG, (Serializable)getLatencyConfig());
		tabSpec.setContent(intent);
		tabSpec.setIndicator(getString(R.string.latency));
		host.addTab(tabSpec);
		
		tabSpec = host.newTabSpec("TAG4");
		intent = new Intent(this, TestResultsActivity.class);
		intent.putExtra(PARAM_RESULT_TYPE, TYPE_LATENCY);
		intent.putExtra(PARAM_CONTENT_CONFIG, (Serializable)getPLoss());
		tabSpec.setContent(intent);
		tabSpec.setIndicator(getString(R.string.p_loss));
		host.addTab(tabSpec);
		
		tabSpec = host.newTabSpec("TAG5");
		intent = new Intent(this, TestResultsActivity.class);
		intent.putExtra(PARAM_RESULT_TYPE, TYPE_JITTER);
		intent.putExtra(PARAM_CONTENT_CONFIG, (Serializable)getJitter());
		tabSpec.setContent(intent);
		tabSpec.setIndicator(getString(R.string.jitter));
		host.addTab(tabSpec);
	}

	
	private List<ParcerDataType> getDownloadUploadConfig() {
		List<ParcerDataType> list = new ArrayList<ParcerDataType>();
		list.add(new ParcerDataType(1, ParcerFieldType.FIELD_TIME, getString(R.string.time)));
		list.add(new ParcerDataType(3, ParcerFieldType.FIELD_TARGET, getString(R.string.target)));
		list.add(new ParcerDataType(7, ParcerFieldType.FIELD_SPEED, getString(R.string.speed)));
		return list;
	}
	
	private List<ParcerDataType> getLatencyConfig() {
		List<ParcerDataType> list = new ArrayList<ParcerDataType>();
		list.add(new ParcerDataType(1, ParcerFieldType.FIELD_TIME, getString(R.string.time)));
		list.add(new ParcerDataType(3, ParcerFieldType.FIELD_TARGET, getString(R.string.target)));
		list.add(new ParcerDataType(5, ParcerFieldType.FIELD_LATENCY, getString(R.string.latency)));
		return list;
	}
	
	private List<ParcerDataType> getPLoss() {
		List<ParcerDataType> list = new ArrayList<ParcerDataType>();
		list.add(new ParcerDataType(1, ParcerFieldType.FIELD_TIME, getString(R.string.time)));
		list.add(new ParcerDataType(3, ParcerFieldType.FIELD_TARGET, getString(R.string.target)));
		list.add(new ParcerDataType(-1, ParcerFieldType.FIELD_P_LOSS, getString(R.string.p_loss)));
		return list;
	}
	
	private List<ParcerDataType> getJitter() {
		List<ParcerDataType> list = new ArrayList<ParcerDataType>();
		list.add(new ParcerDataType(1, ParcerFieldType.FIELD_TIME, getString(R.string.time)));
		list.add(new ParcerDataType(3, ParcerFieldType.FIELD_TARGET, getString(R.string.target)));
		list.add(new ParcerDataType(-1, ParcerFieldType.FIELD_JITTER, getString(R.string.jitter)));
		return list;
	}
}
