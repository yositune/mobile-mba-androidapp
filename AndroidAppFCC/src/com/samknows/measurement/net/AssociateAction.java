package com.samknows.measurement.net;

import com.samknows.measurement.FCCAppSettings;
import com.samknows.measurement.util.LoginHelper;

public class AssociateAction extends NetAction{
	
	public AssociateAction(String unitId, String imei) {
		super();
		addHeader("Authorization", "Basic " + LoginHelper.getCredentialsEncoded());
		setRequest(FCCAppSettings.getInstance().reportingServerPath + "unit/associate" + "?unit_id=" + unitId + "&mac=" + imei);
	}
}
