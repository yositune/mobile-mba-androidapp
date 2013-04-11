package com.samknows.measurement.statemachine.state;

import com.samknows.measurement.AppSettings;
import com.samknows.measurement.Logger;
import com.samknows.measurement.MainService;
import com.samknows.measurement.net.DCSInitAction;
import com.samknows.measurement.net.DCSInitAnonymousAction;
import com.samknows.measurement.statemachine.StateResponseCode;

public class InitialiseAnonymousState extends BaseState {

	public InitialiseAnonymousState(MainService c){
		super(c);
	}

	@Override
	public StateResponseCode executeState() {
		DCSInitAnonymousAction action = new DCSInitAnonymousAction();
		action.execute();
		if (action.isSuccess()) {
			AppSettings appSettings = AppSettings.getInstance();
			Logger.d(this, "retrived server base url: " + action.serverBaseUrl);
			appSettings.saveServerBaseUrl(action.serverBaseUrl);
			Logger.d(this, "save server base url: " + action.serverBaseUrl);
			String config_path = appSettings.protocol_scheme+"://"+action.serverBaseUrl+"/"+appSettings.download_config_path;
			appSettings.saveConfigPath(config_path);
			Logger.d(this, "save config file url: " + config_path);
			return StateResponseCode.OK;
		}
		return StateResponseCode.FAIL;
	}

}
