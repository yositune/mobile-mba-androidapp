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


package com.samknows.measurement.schedule.condition;

import org.w3c.dom.Element;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.samknows.measurement.Logger;
import com.samknows.measurement.test.TestContext;
import com.samknows.measurement.util.DCSConvertorUtil;

public class NetworkTypeCondition extends Condition{
	private static final long serialVersionUID = 1L;

	private ConnectivityType expectedNetworkType;
	private int type = -1;
	private transient boolean hasNetworkTypeChanged;
	private transient BroadcastReceiver networkStateReceiver;
	
	private transient String networkChangedString = "";
	
	public enum ConnectivityType {
		TYPE_MOBILE, TYPE_WIFI
	}
	
	@Override
	public ConditionResult doTestBefore(final TestContext tc) {
		NetworkInfo info = ((ConnectivityManager) tc.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		if (info != null) {
			type = info.getType();
		}
		boolean isSuccess;
		switch (expectedNetworkType) {
		case TYPE_MOBILE: {
			isSuccess = type == ConnectivityManager.TYPE_MOBILE;
			break;
		}
		case TYPE_WIFI: {
			isSuccess = type == ConnectivityManager.TYPE_WIFI;
			break;
		}
		default:
			Logger.e(this, "null network info");
			isSuccess = false;
		}
		
		if (isSuccess) {
			hasNetworkTypeChanged = false;
			networkChangedString = DCSConvertorUtil.convertActiveConnectivityType(type);
			networkStateReceiver = new BroadcastReceiver() {
			    @Override
			    public void onReceive(Context context, Intent intent) {
			    	NetworkInfo info = ((ConnectivityManager) tc.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
			    	if (info != null) { 
			    		if (type != info.getType()) { //skip the same events
					    	type = info.getType();
					    	networkChangedString += "," + DCSConvertorUtil.convertActiveConnectivityType(type);
					    	hasNetworkTypeChanged = true;
			    		}
			    	} else if (type != -1){ //skip the same events
			    		if (type != -1) {
				    		type = -1;
				    		networkChangedString += "," + "NONE";
				    		hasNetworkTypeChanged = true;
			    		}
			    	}
			    }
			};

			IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);        
			tc.getServiceContext().registerReceiver(networkStateReceiver, filter);

		}
		
		ConditionResult result = new ConditionResult(isSuccess);
		result.setJSONFields("expected_network", "connectivity");
		result.generateOut("NETWORKTYPE", DCSConvertorUtil.covertConnectivityType(expectedNetworkType), DCSConvertorUtil.convertActiveConnectivityType(type));
		
		return result;
	}

	@Override
	public ConditionResult testAfter(TestContext tc) {
		ConditionResult result = new ConditionResult(!hasNetworkTypeChanged);
		result.setJSONFields("expected_network", "connectivity_changed");
		result.generateOut("NETWORKTYPELISTENER", DCSConvertorUtil.covertConnectivityType(expectedNetworkType), networkChangedString);
		return result;
	}

	@Override
	public void release(TestContext tc) {
		if (networkStateReceiver != null) {
			tc.getServiceContext().unregisterReceiver(networkStateReceiver);
		}
	}

	@Override
	public boolean needSeparateThread() {
		return false;
	}

	public static NetworkTypeCondition parseXml(Element node) {
		NetworkTypeCondition c = new NetworkTypeCondition();
		String type = node.getAttribute("value");
		if (type.equalsIgnoreCase("mobile")) c.expectedNetworkType = ConnectivityType.TYPE_MOBILE;
		else if (type.equalsIgnoreCase("wifi")) c.expectedNetworkType = ConnectivityType.TYPE_WIFI;
		else {
			throw new RuntimeException("unknown connectivity type: " + type);
		}
		return c;
	}
}
