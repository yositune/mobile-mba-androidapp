package com.samknows.measurement.statemachine.state;

import android.content.Context;

import com.samknows.measurement.AppSettings;
import com.samknows.measurement.Logger;
import com.samknows.measurement.MainService;
import com.samknows.measurement.net.GetVersionNumberAction;
import com.samknows.measurement.statemachine.StateResponseCode;

public class CheckConfigVersionState extends BaseState{

	public CheckConfigVersionState(MainService c) {
		super(c);
	}

	@Override
	public StateResponseCode executeState() {
		String currentVersion = AppSettings.getInstance().getConfigVersion();
		Logger.d(this, "current config version: " + currentVersion);
		
		GetVersionNumberAction action = new GetVersionNumberAction(ctx,	currentVersion);
		action.execute();
		if (action.isSuccess()) {
			Logger.d(this, "obtained config version from server: "
					+ action.version);
			if (action.version.equals(currentVersion)) {
				return StateResponseCode.OK;
			} else {
				AppSettings.getInstance().saveConfigVersion(action.version); 
				AppSettings.getInstance().saveConfigPath(action.path);
				return StateResponseCode.NOT_OK; //we will download config at next state
			}
		} else {
			return StateResponseCode.FAIL;
		}
	}

}
