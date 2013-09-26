package com.samknows.measurement.net;

import java.io.InputStream;

import android.content.Context;

import com.samknows.libcore.SKLogger;
import com.samknows.measurement.FCCAppSettings;

public class RequestScheduleAnonymousAction extends NetAction {
	public InputStream content;

	public RequestScheduleAnonymousAction(Context c){
		super();
		FCCAppSettings settings = FCCAppSettings.getFCCAppSettingsInstance();
		setRequest(settings.getConfigPath());
		addHeader("X-App-Version", settings.app_version_code+"");
		addHeader("X-Enterprise-ID",settings.enterprise_id);
	}
	
	@Override
	public boolean isSuccess() {
		return content != null;
	}

	@Override
	protected void onActionFinished() {
		try {
			content = response.getEntity().getContent();
		} catch (Exception e) {
			SKLogger.e(this, "failed to parse response", e);
		}
	}

}
