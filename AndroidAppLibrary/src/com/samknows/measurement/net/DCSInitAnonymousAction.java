package com.samknows.measurement.net;

import java.net.InetAddress;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import com.samknows.measurement.AppSettings;
import com.samknows.measurement.Logger;

public class DCSInitAnonymousAction extends NetAction {
	public String serverBaseUrl;
	
	public DCSInitAnonymousAction(){
		super();
		setRequest(AppSettings.getInstance().dCSInitUrl);	
	}
	
	@Override
	protected void onActionFinished() {
		super.onActionFinished();
		try {
			serverBaseUrl = null;
			String resp = IOUtils.toString(response.getEntity().getContent()).trim();
			
			new URL(AppSettings.getInstance().protocol_scheme+"://"+ resp);
			serverBaseUrl = resp;
			
		} catch (Exception e) {
			Logger.e(this, "failed to parse result", e);
		}
	}

	@Override
	public boolean isSuccess() {
		return serverBaseUrl != null;
	}

}
