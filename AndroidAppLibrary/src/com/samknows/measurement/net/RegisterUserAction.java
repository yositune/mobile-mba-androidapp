package com.samknows.measurement.net;

import com.samknows.measurement.AppSettings;
import com.samknows.measurement.Constants;

public class RegisterUserAction extends NetAction{

	public RegisterUserAction(String name, String pass) {
		super();
//		setPost(true);
		setRequest(AppSettings.getInstance().reportingServerPath + "user/create?" + "email=" + name + "&password=" + pass);
	}
}
