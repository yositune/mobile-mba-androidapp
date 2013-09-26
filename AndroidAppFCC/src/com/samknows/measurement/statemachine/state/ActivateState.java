package com.samknows.measurement.statemachine.state;

import com.samknows.libcore.SKLogger;
import com.samknows.measurement.FCCAppSettings;
import com.samknows.measurement.MainService;
import com.samknows.measurement.environment.PhoneIdentityData;
import com.samknows.measurement.environment.PhoneIdentityDataCollector;
import com.samknows.measurement.net.DCSActivateAction;
import com.samknows.measurement.statemachine.StateResponseCode;

public class ActivateState extends BaseState{

	public ActivateState(MainService ctx) {
		super(ctx);
	}

	@Override
	public StateResponseCode executeState() {
		PhoneIdentityData data = new PhoneIdentityDataCollector(ctx).collect();
		DCSActivateAction action = new DCSActivateAction(ctx, data);
		action.execute();
		if (action.isSuccess()) {
			SKLogger.d(this, "retrived unitId: " + action.unitId);
			FCCAppSettings.getInstance().saveUnitId(action.unitId);
			SKLogger.d(this, "save unitId: " + action.unitId);
			return StateResponseCode.OK;
		} 
		return StateResponseCode.FAIL;
	}

}
