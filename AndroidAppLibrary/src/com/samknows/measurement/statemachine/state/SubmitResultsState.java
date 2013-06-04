package com.samknows.measurement.statemachine.state;

import android.content.Context;

import com.samknows.measurement.MainService;
import com.samknows.measurement.environment.PhoneIdentityData;
import com.samknows.measurement.environment.PhoneIdentityDataCollector;
import com.samknows.measurement.net.SubmitTestResultsAction;
import com.samknows.measurement.statemachine.StateResponseCode;

public class SubmitResultsState extends BaseState{

	public SubmitResultsState(MainService c) {
		super(c);
	}

	@Override
	public StateResponseCode executeState() {
		new SubmitTestResultsAction(ctx).execute();
		return StateResponseCode.OK;
	}

}
