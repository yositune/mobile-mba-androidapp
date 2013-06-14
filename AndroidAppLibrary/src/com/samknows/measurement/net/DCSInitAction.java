package com.samknows.measurement.net;

import java.net.URLEncoder;

import org.apache.commons.io.IOUtils;

import com.samknows.measurement.AppSettings;
import com.samknows.measurement.Logger;
import com.samknows.measurement.environment.PhoneIdentityData;

public class DCSInitAction extends DCSInitAnonymousAction{
	
	public DCSInitAction(PhoneIdentityData data) {
		super();
		setRequest(AppSettings.getInstance().dCSInitUrl + "?IMEI=" + URLEncoder.encode(data.imei));

		addHeader("X-Mobile-IMSI", data.imsi);
		addHeader("X-Mobile-Manufacturer", data.manufacturer);
		addHeader("X-Mobile-Model", data.model);
		addHeader("X-Mobile-OSType", data.osType);
		addHeader("X-Mobile-OSVersion", data.osVersion);
	}

	
}
