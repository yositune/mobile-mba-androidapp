package com.samknows.measurement.net;

import java.net.URLEncoder;

import com.samknows.measurement.FCCAppSettings;
import com.samknows.measurement.environment.PhoneIdentityData;

public class DCSInitAction extends DCSInitAnonymousAction{
	
	public DCSInitAction(PhoneIdentityData data) {
		super();
		setRequest(FCCAppSettings.getInstance().dCSInitUrl + "?IMEI=" + URLEncoder.encode(data.imei));

		addHeader("X-Mobile-IMSI", data.imsi);
		addHeader("X-Mobile-Manufacturer", data.manufacturer);
		addHeader("X-Mobile-Model", data.model);
		addHeader("X-Mobile-OSType", data.osType);
		addHeader("X-Mobile-OSVersion", data.osVersion+"");
	}

	
}
