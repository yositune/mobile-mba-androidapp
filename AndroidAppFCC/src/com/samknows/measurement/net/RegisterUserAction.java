package com.samknows.measurement.net;

import com.samknows.measurement.FCCAppSettings;

public class RegisterUserAction extends NetAction{

	public RegisterUserAction(String name, String pass) {
		super();
//		setPost(true);
		setRequest(FCCAppSettings.getInstance().reportingServerPath + "user/create?" + "email=" + name + "&password=" + pass);
	}
}
