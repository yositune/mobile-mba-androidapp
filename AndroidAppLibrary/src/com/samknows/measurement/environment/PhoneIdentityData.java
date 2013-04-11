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
