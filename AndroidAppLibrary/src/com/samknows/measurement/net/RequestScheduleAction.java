package com.samknows.measurement.net;

import java.io.InputStream;

import android.content.Context;

import com.samknows.measurement.AppSettings;


public class RequestScheduleAction extends RequestScheduleAnonymousAction {
	public InputStream content;
	public RequestScheduleAction(Context c) {
		super(c);
		addHeader("X-Unit-ID", AppSettings.getInstance().getUnitId());
	}

}
