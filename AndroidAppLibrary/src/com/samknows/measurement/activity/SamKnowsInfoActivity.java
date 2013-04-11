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

import java.text.SimpleDateFormat;
import java.util.Date;


import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.NeighboringCellInfo;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.samknows.measurement.AppSettings;
import com.samknows.measurement.CachingStorage;
import com.samknows.measurement.Constants;
import com.samknows.measurement.Logger;
import com.samknows.measurement.MainService;
import com.samknows.measurement.R;
import com.samknows.measurement.Storage;
import com.samknows.measurement.activity.components.Util;
import com.samknows.measurement.environment.CellTowersData;
import com.samknows.measurement.environment.CellTowersDataCollector;
import com.samknows.measurement.environment.NetworkData;
import com.samknows.measurement.environment.NetworkDataCollector;
import com.samknows.measurement.environment.PhoneIdentityData;
import com.samknows.measurement.environment.PhoneIdentityDataCollector;
import com.samknows.measurement.schedule.ScheduleConfig;
import com.samknows.measurement.util.DCSConvertorUtil;
import com.samknows.measurement.util.SKDateFormat;

public class SamKnowsInfoActivity extends BaseLogoutActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (AppSettings.getInstance().anonymous) {
			setContentView(R.layout.info_anonymous_layout);
		}
		else
		{
			setContentView(R.layout.info_layout);
		}
		Util.initializeFonts(this);
		Util.overrideFonts(this, findViewById(android.R.id.content));
	}
	
	@Override
	protected void onResume() {
		populateInfo();
		super.onResume();
	}

	private void populateInfo() {
		String value;
		if (MainService.isExecuting()) {
			value = getString(R.string.executing_now); 
		} else {
			if(AppSettings.getInstance().isServiceActivated()){
				value = getString(R.string.yes);
			}else{
				value = getString(R.string.no);
			}
		}
		((TextView)findViewById(R.id.tv_service_activated_value)).setText(value);
		if(AppSettings.getInstance().isServiceEnabled()){
			value = getString(R.string.enabled);
		}else{
			value = getString(R.string.disabled);
		}
		((TextView)findViewById(R.id.tv_service_autotesting_value)).setText(value);
		((TextView)findViewById(R.id.tv_service_status_value)).setText(getString(AppSettings.getInstance().getState().sId));
		
		String versionName="";
		try {
			versionName = this.getPackageManager().getPackageInfo(this.getPackageName(), 0 ).versionName;
		} catch (NameNotFoundException e) {
			Logger.e(this, "Error in getting app version name.", e);
		}
		
		((TextView)findViewById(R.id.version)).setText(versionName);
		
		ScheduleConfig config = CachingStorage.getInstance().loadScheduleConfig();
		String schedule_version = config == null ? "" : config.getConfigVersion(); 
		((TextView)findViewById(R.id.schedule_version)).setText(schedule_version);
		
		String nextTestScheduled = "";
		if (MainService.isExecuting()) {
			nextTestScheduled = getString(R.string.executing_now);
		} else {
			long nextRunTime = AppSettings.getInstance().getNextRunTime();
			if (nextRunTime == Constants.NO_NEXT_RUN_TIME) {
				nextTestScheduled = getString(R.string.none);
			} else {
				nextTestScheduled = new SKDateFormat(this).UITime(nextRunTime);
			}
		}
		((TextView)findViewById(R.id.tv_scheduledFor_value)).setText(nextTestScheduled);

		PhoneIdentityData phoneData = new PhoneIdentityDataCollector(this).collect();
		if (!AppSettings.getInstance().anonymous){
			((TextView)findViewById(R.id.tv_imei_value)).setText(phoneData.imei + "");
			((TextView)findViewById(R.id.tv_imsi_value)).setText(phoneData.imsi + "");
			((TextView)findViewById(R.id.tv_unitId_value)).setText(AppSettings.getInstance().getUnitId());
		}
		
		value = phoneData.manufacturer + "\n\r" + phoneData.model;
		((TextView)findViewById(R.id.tv_phone_value)).setText(value);
		value = phoneData.osType + " v" + phoneData.osVersion;
		((TextView)findViewById(R.id.tv_os_value)).setText(value);
		
		NetworkData networkData = new NetworkDataCollector(this).collect();
		value = DCSConvertorUtil.convertPhoneType(networkData.phoneType);
		((TextView)findViewById(R.id.tv_phone_type_value)).setText(value);
		value = getString(DCSConvertorUtil.networkTypeToStringId(networkData.networkType));
		((TextView)findViewById(R.id.tv_network_type_value)).setText(value);
		value = networkData.networkOperatorCode + "/" + networkData.networkOperatorName;
		((TextView)findViewById(R.id.tv_network_operator_value)).setText(value);
		if(networkData.isRoaming){
			value = getString(R.string.yes);
		}else{
			value = getString(R.string.no);
		}
		((TextView)findViewById(R.id.tv_roaming_value)).setText(value);
		
		Location loc1 = ((LocationManager)getSystemService(LOCATION_SERVICE)).getLastKnownLocation(LocationManager.GPS_PROVIDER);
		Location loc2 = ((LocationManager)getSystemService(LOCATION_SERVICE)).getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		Location loc = null;
		if (loc1 != null && loc2 != null) {
			loc = loc1.getTime() > loc2.getTime() ? loc1 : loc2;
		} else {
			loc = loc1 == null ? loc2 : loc1;
		}
		if (loc != null) {
			((TextView)findViewById(R.id.tv_loc_date_value)).setText(new SKDateFormat(this).UITime(loc.getTime()));
			((TextView)findViewById(R.id.tv_loc_provider_value)).setText(loc.getProvider());
			((TextView)findViewById(R.id.tv_loc_long_value)).setText(loc.getLongitude() + "");
			((TextView)findViewById(R.id.tv_loc_lat_value)).setText(loc.getLatitude() + "");
			((TextView)findViewById(R.id.tv_loc_acc_value)).setText(loc.getAccuracy() + "");
		}
		
		
		//Cells
		CellTowersData cellData = new CellTowersDataCollector(this).collect();
		if (cellData.cellLocation instanceof GsmCellLocation) {
			GsmCellLocation gsmLocation = (GsmCellLocation) cellData.cellLocation;
			((TextView)findViewById(R.id.tv_cell_tower_type_value)).setText("GSM");
			((TextView)findViewById(R.id.tv_cell_id_value)).setText("" + gsmLocation.getCid());
			((TextView)findViewById(R.id.tv_area_code_value)).setText("" + gsmLocation.getLac());
		} else if (cellData.cellLocation instanceof CdmaCellLocation) {
			((TextView)findViewById(R.id.tv_cell_tower_type_value)).setText("CDMA");
//			CdmaCellLocation cdmaLocation = (CdmaCellLocation) cellLocation;
//			builder.append(CDMA);
//			builder.append(time/1000);
//			builder.append(cdmaLocation.getBaseStationId());
//			builder.append(cdmaLocation.getBaseStationLatitude());
//			builder.append(cdmaLocation.getBaseStationLongitude());
//			builder.append(cdmaLocation.getNetworkId());
//			builder.append(cdmaLocation.getSystemId());
		}
		
		if (cellData.signal.isGsm()) {
			((TextView)findViewById(R.id.tv_signal_value)).setText("" + cellData.signal.getGsmSignalStrength());
		} else {
			((TextView)findViewById(R.id.tv_signal_value)).setText(cellData.signal.getCdmaDbm() + "dBm");
		}
		
		for (NeighboringCellInfo info : cellData.neighbors) {
			appendNeighborCellInfo(info);
		}
		
		Util.initializeFonts(this);
		Util.overrideFonts(this, findViewById(android.R.id.content));
	}
	
	public void appendNeighborCellInfo(NeighboringCellInfo data) {
		
		TableRow tr = new TableRow(this);
		int color = Color.parseColor("#000000");
		TextView label = new TextView(this);
		TableRow.LayoutParams params = new TableRow.LayoutParams();
		params.span = 2;
		params.leftMargin=10;
		params.rightMargin=10;
		params.bottomMargin=10;
		
		TableRow.LayoutParams params2 = new TableRow.LayoutParams();
		params2.weight=1;
		//params2.gravity=16;
		
		label.setLayoutParams(params);
		label.setText("Neighbor Cell Tower");
		label.setTextSize(20);
		label.setTextColor(Color.parseColor("#909090"));
		//label.setTypeface(null, Typeface.BOLD);
		//label.setGravity(Gravity.CENTER);
		//label.setPadding(0, 10, 0, 0);
		tr.addView(label);
		tr.setLayoutParams(params);
		
		((TableLayout)findViewById(R.id.info_table)).addView(tr, params);
		
		tr = new TableRow(this);
		
		label = new TextView(this);
		label.setLayoutParams(params);
		label.setText("Network type ");
		label.setTextSize(18);
		label.setTextColor(color);
		
		
		tr.setBackgroundResource(R.drawable.black_alpha);
		tr.addView(label);
		
		label = new TextView(this);
		label.setTextSize(18);
		label.setLayoutParams(params2);
		label.setText(getString(DCSConvertorUtil.networkTypeToStringId(data.getNetworkType())));
		label.setTextColor(color);
		tr.addView(label);
		
		
		((TableLayout)findViewById(R.id.info_table)).addView(tr,params);
		tr.setLayoutParams(params);
		
		tr = new TableRow(this);
		label = new TextView(this);
		label.setTextSize(18);
		label.setTextColor(color);
		tr.setBackgroundResource(R.drawable.black_alpha);
		label.setLayoutParams(params);
		label.setText("PSC ");
		tr.addView(label);
		
		
		label = new TextView(this);
		label.setText(data.getPsc() + "");
		label.setLayoutParams(params2);
		label.setTextColor(color);
		tr.addView(label);
		((TableLayout)findViewById(R.id.info_table)).addView(tr, params);
		
		tr.setLayoutParams(params);
		
		tr = new TableRow(this);
		label = new TextView(this);
		label.setLayoutParams(params);
		label.setText("Cell id ");
		label.setTextSize(18);
		label.setTextColor(color);
		tr.setBackgroundResource(R.drawable.black_alpha);
		tr.addView(label);
		
		
		label = new TextView(this);
		label.setLayoutParams(params);
		label.setText(data.getCid() + "");
		label.setTextColor(color);
		label.setLayoutParams(params2);
		tr.addView(label);
		((TableLayout)findViewById(R.id.info_table)).addView(tr, params);
		tr.setLayoutParams(params);
		
		tr = new TableRow(this);
		label = new TextView(this);
		label.setLayoutParams(params);
		label.setText("Area code ");
		label.setTextSize(18);
		label.setTextColor(color);
		tr.setBackgroundResource(R.drawable.black_alpha);
		tr.addView(label);
		
		
		label = new TextView(this);
		label.setText(data.getLac() + "");
		label.setTextColor(color);
		label.setLayoutParams(params2);
		tr.addView(label);
		((TableLayout)findViewById(R.id.info_table)).addView(tr,params);
		tr.setLayoutParams(params);
		
		tr = new TableRow(this);
		label = new TextView(this);
		label.setLayoutParams(params);
		label.setText("Signal Strength ");
		label.setTextSize(18);
		label.setTextColor(color);
		tr.setBackgroundResource(R.drawable.black_alpha);
		tr.addView(label);
		
		
		label = new TextView(this);
		label.setLayoutParams(params);
		label.setText(data.getRssi() + "");
		label.setTextColor(color);
		label.setLayoutParams(params2);
		tr.addView(label);
		((TableLayout)findViewById(R.id.info_table)).addView(tr, params);
		tr.setLayoutParams(params);
	}

}
