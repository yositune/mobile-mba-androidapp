package com.samknows.measurement.net;

import java.net.URL;

import org.apache.commons.io.IOUtils;

import com.samknows.libcore.SKLogger;
import com.samknows.measurement.FCCAppSettings;

public class DCSInitAnonymousAction extends NetAction {
	public String serverBaseUrl;
	
	public DCSInitAnonymousAction(){
		super();
		setRequest(FCCAppSettings.getInstance().dCSInitUrl);	
	}
	
	@Override
	protected void onActionFinished() {
		super.onActionFinished();
		try {
			serverBaseUrl = null;
			String resp = IOUtils.toString(response.getEntity().getContent()).trim();
			
			new URL(FCCAppSettings.getFCCAppSettingsInstance().protocol_scheme+"://"+ resp);
			serverBaseUrl = resp;
			
		} catch (Exception e) {
			SKLogger.e(this, "failed to parse result", e);
		}
	}

	@Override
	public boolean isSuccess() {
		return serverBaseUrl != null;
	}

}
