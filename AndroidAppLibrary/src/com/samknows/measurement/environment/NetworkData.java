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


package com.samknows.measurement.environment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.samknows.measurement.Logger;
import com.samknows.measurement.storage.PassiveMetric;
import com.samknows.measurement.util.DCSConvertorUtil;
import com.samknows.measurement.util.DCSStringBuilder;

public class NetworkData implements DCSData{
	
	private static final String ID_PHONE = "NETWORKSTATE";
	private static final String ID_NETWORK_OP = "NETWORKOPERATOR";
	private static final String ID_SIM_OP = "SIMOPERATOR";
	//JSONOutput
	public static final String JSON_TYPE_VALUE = "network_data";
	public static final String JSON_PHONE_TYPE = "phone_type";
	public static final String JSON_PHONE_TYPE_CODE = "phone_type_code";
	public static final String JSON_NETWORK_TYPE = "network_type";
	public static final String JSON_NETWORK_TYPE_CODE = "network_type_code";
	public static final String JSON_ACTIVE_NETWORK_TYPE = "active_network_type";
	public static final String JSON_ACTIVE_NETWORK_TYPE_CODE = "active_network_type_code";
	public static final String JSON_CONNECTED = "connected";
	public static final String JSON_ROAMING = "roaming";
	public static final String JSON_NETWORK_OPERATOR_CODE = "network_operator_code";
	public static final String JSON_NETWORK_OPERATOR_NAME = "network_operator_name";
	public static final String JSON_SIM_OPERATOR_CODE = "sim_operator_code";
	public static final String JSON_SIM_OPERATOR_NAME = "sim_operator_name";
	
	
	/** time in milis */
	public long time;
	
	//phone
	public int phoneType;
	public int networkType;
	public NetworkInfo activeNetworkInfo;
	public boolean isConnected;
	public boolean isRoaming;

	//network operator
	public String networkOperatorCode;
	public String networkOperatorName;
	
	//sim operator
	public String simOperatorCode;
	public String simOperatorName;
	
	public List<String> convert() {
		List<String> list = new ArrayList<String>();
		
		DCSStringBuilder builder = new DCSStringBuilder();
		builder.append(ID_PHONE);
		builder.append(time/1000);
		builder.append(DCSConvertorUtil.convertPhoneType(phoneType));
		builder.append(DCSConvertorUtil.convertNetworkType(networkType));
		
		String s = "NONE";
		if (activeNetworkInfo != null) {
			switch (activeNetworkInfo.getType()) {
			case ConnectivityManager.TYPE_MOBILE: {
				s = "MOBILE";
				break;
			}
			case ConnectivityManager.TYPE_WIFI: {
				s = "WiFi";
				break;
			}
			}
		}
		builder.append(s);
		builder.append(isConnected ? 1 : 0);
		builder.append(isRoaming ? 1 : 0);
		list.add(builder.build());
		
		builder = new DCSStringBuilder();
		builder.append(ID_NETWORK_OP);
		builder.append(time/1000);
		builder.append(networkOperatorCode);
		builder.append(networkOperatorName);
		list.add(builder.build());
		
		builder = new DCSStringBuilder();
		builder.append(ID_SIM_OP);
		builder.append(time/1000);
		builder.append(simOperatorCode);
		builder.append(simOperatorName);
		list.add(builder.build());
		
		return list;
	}

	//ret.add(new PassiveMetric());
	@Override
	public List<JSONObject> getPassiveMetric() {
		List<JSONObject> ret = new ArrayList<JSONObject>();
		ret.add(PassiveMetric.create(PassiveMetric.METRIC_TYPE.PHONETYPE, time, DCSConvertorUtil.convertPhoneType(phoneType)));
		ret.add(PassiveMetric.create(PassiveMetric.METRIC_TYPE.NETWORKTYPE, time, DCSConvertorUtil.convertNetworkType(networkType)));
		if(activeNetworkInfo != null){
			ret.add(PassiveMetric.create(PassiveMetric.METRIC_TYPE.ACTIVENETWORKTYPE, time, DCSConvertorUtil.convertConnectivityType(activeNetworkInfo.getType())));
		}
		ret.add(PassiveMetric.create(PassiveMetric.METRIC_TYPE.ROAMINGSTATUS, time, (isRoaming ? "true" : "false")));
		ret.add(PassiveMetric.create(PassiveMetric.METRIC_TYPE.NETWORKOPERATORCODE, time, networkOperatorCode));
		ret.add(PassiveMetric.create(PassiveMetric.METRIC_TYPE.NETWORKOPERATORNAME, time, networkOperatorName));
		ret.add(PassiveMetric.create(PassiveMetric.METRIC_TYPE.SIMOPERATORCODE, time, simOperatorCode));
		ret.add(PassiveMetric.create(PassiveMetric.METRIC_TYPE.SIMOPERATORNAME, time, simOperatorName));
	
		return ret;
	}

	@Override
	public List<JSONObject> convertToJSON() {
		JSONObject ret  = new JSONObject();
		try{
			ret.put(JSON_TYPE, JSON_TYPE_VALUE);
			ret.put(JSON_PHONE_TYPE_CODE, phoneType+"");
			ret.put(JSON_PHONE_TYPE, DCSConvertorUtil.convertPhoneType(phoneType));
			ret.put(JSON_TIMESTAMP, time/1000+"");
			ret.put(JSON_DATETIME, new java.util.Date(time).toString());
			ret.put(JSON_NETWORK_TYPE_CODE, networkType+"");
			ret.put(JSON_NETWORK_TYPE, DCSConvertorUtil.convertNetworkType(networkType));
			
			if(activeNetworkInfo != null){
				ret.put(JSON_ACTIVE_NETWORK_TYPE, activeNetworkInfo.getTypeName());
				ret.put(JSON_ACTIVE_NETWORK_TYPE_CODE, activeNetworkInfo.getType());
			}
			ret.put(JSON_CONNECTED, Boolean.toString(isConnected));
			ret.put(JSON_ROAMING, Boolean.toString(isRoaming));
			ret.put(JSON_NETWORK_OPERATOR_CODE, networkOperatorCode);
			ret.put(JSON_NETWORK_OPERATOR_NAME, networkOperatorName);
			ret.put(JSON_SIM_OPERATOR_CODE, simOperatorCode);
			ret.put(JSON_SIM_OPERATOR_NAME, simOperatorName);
		}catch(JSONException je){
			Logger.e(NetworkData.class, "Error in creating JSONObject: "+ je.getMessage());
		}
		List<JSONObject> l = new ArrayList<JSONObject>();
		l.add(ret);
		return l;
	}

	
}
