package com.samknows.measurement.net;

import com.samknows.measurement.FCCAppSettings;

public class RecoverPasswordAction extends NetAction{

	public RecoverPasswordAction(String name, String pass) {
		super();
		setRequest(FCCAppSettings.getInstance().reportingServerPath + "user/request_secret?email=" + name);
	}
}
