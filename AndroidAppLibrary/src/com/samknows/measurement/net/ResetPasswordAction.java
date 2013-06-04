package com.samknows.measurement.net;

import com.samknows.measurement.AppSettings;
import com.samknows.measurement.Constants;

public class ResetPasswordAction extends NetAction{

	public ResetPasswordAction(String name, String pass, String code) {
		super();
		setRequest(AppSettings.getInstance().reportingServerPath + "user/change_password?email="+name+"&secret="+code+"&password=" + pass);
	}
}
