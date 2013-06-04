package com.samknows.measurement.net;

import java.io.InputStream;

import android.content.Context;

import com.samknows.measurement.AppSettings;
import com.samknows.measurement.Logger;

public class RequestScheduleAnonymousAction extends NetAction {
	public InputStream content;

	public RequestScheduleAnonymousAction(Context c){
		super();
		AppSettings settings = AppSettings.getInstance();
		setRequest(settings.getConfigPath());
		addHeader("X-App-Version", settings.app_version_code);
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
			Logger.e(this, "failed to parse response", e);
		}
	}

}
