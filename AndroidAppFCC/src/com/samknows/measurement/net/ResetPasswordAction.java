package com.samknows.measurement.net;

import com.samknows.measurement.FCCAppSettings;

public class ResetPasswordAction extends NetAction{

	public ResetPasswordAction(String name, String pass, String code) {
		super();
		setRequest(FCCAppSettings.getInstance().reportingServerPath + "user/change_password?email="+name+"&secret="+code+"&password=" + pass);
	}
}
