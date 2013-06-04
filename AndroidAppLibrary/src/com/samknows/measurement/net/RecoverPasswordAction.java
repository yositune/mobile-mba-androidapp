package com.samknows.measurement.net;

import com.samknows.measurement.AppSettings;
import com.samknows.measurement.Constants;

public class RecoverPasswordAction extends NetAction{

	public RecoverPasswordAction(String name, String pass) {
		super();
		setRequest(AppSettings.getInstance().reportingServerPath + "user/request_secret?email=" + name);
	}
}
