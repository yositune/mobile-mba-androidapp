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

import com.samknows.measurement.AppSettings;
import com.samknows.measurement.Logger;
import com.samknows.measurement.storage.PassiveMetric;
import com.samknows.measurement.util.DCSStringBuilder;

public class PhoneIdentityData implements DCSData{
	public static final String JSON_TYPE_PHONE_IDENTITY = "phone_identity";
	public static final String JSON_IMEI = "imei";
	public static final String JSON_IMSI = "imsi";
	public static final String JSON_MANUFACTURER = "manufacturer";
	public static final String JSON_MODEL = "model";
	public static final String JSON_OSTYPE = "os_type";
	public static final String JSON_OSVERSION = "os_version";
	
	
	public String imei;
	public long time;
	public String imsi;
	public String manufacturer;
	public String model;
	public String osType;
	public String osVersion;
	
	@Override
	public List<String> convert() {
		List<String> list = new ArrayList<String>();
		
		DCSStringBuilder builder = new DCSStringBuilder();
		builder.append("PHONEIDENTITY");
		builder.append(time/1000);
		builder.append(imei);
		builder.append(imsi);
		builder.append(manufacturer);
		builder.append(model);
		builder.append(osType);
		builder.append(osVersion);
		list.add(builder.build());
		
		return list;
	}

	@Override
	public List<JSONObject> getPassiveMetric() {
		List<JSONObject> ret = new ArrayList<JSONObject>();
		long time = System.currentTimeMillis();
		if(!AppSettings.getInstance().anonymous){
			ret.add(PassiveMetric.create(PassiveMetric.METRIC_TYPE.IMEI, time, imei));
			ret.add(PassiveMetric.create(PassiveMetric.METRIC_TYPE.IMSI, time, imsi));
		}
		ret.add(PassiveMetric.create(PassiveMetric.METRIC_TYPE.MANUFACTOR, time, manufacturer));
		ret.add(PassiveMetric.create(PassiveMetric.METRIC_TYPE.MODEL, time, model));
		ret.add(PassiveMetric.create(PassiveMetric.METRIC_TYPE.OSTYPE, time, osType ));
		ret.add(PassiveMetric.create(PassiveMetric.METRIC_TYPE.OSVERSION, time, osVersion));
		return ret;
	}
	
	@Override
	public List<JSONObject> convertToJSON() {
		List<JSONObject> ret = new ArrayList<JSONObject>();
		JSONObject j = new JSONObject();
		try{
		j.put(JSON_TYPE, JSON_TYPE_PHONE_IDENTITY);
		collectSensitiveData(j);
		j.put(JSON_TIMESTAMP, time/1000);
		j.put(JSON_DATETIME, new java.util.Date(time).toString());
		j.put(JSON_MANUFACTURER, manufacturer);
		j.put(JSON_MODEL, model);
		j.put(JSON_OSTYPE, osType);
		j.put(JSON_OSVERSION, osVersion);
		ret.add(j);
		}catch(JSONException je){
			Logger.e(PhoneIdentityData.class, "Error in creating a JSONObject: "+je.getMessage());
			ret = null;
		}
		return ret;
	}
	
	private void collectSensitiveData(JSONObject j){
		try{
			if(!AppSettings.getInstance().anonymous){
				j.put(JSON_IMEI, imei);
				j.put(JSON_IMSI, imsi);
			}
		}catch(JSONException je){
			Logger.e(PhoneIdentityData.class, "Error in creating a JSONObject: "+ je.getMessage());
		}
	}
}
