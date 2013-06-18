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
