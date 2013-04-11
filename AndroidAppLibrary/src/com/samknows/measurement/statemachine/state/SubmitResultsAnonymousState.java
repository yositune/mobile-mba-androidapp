package com.samknows.measurement.statemachine.state;

import com.samknows.measurement.MainService;
import com.samknows.measurement.net.SubmitTestResultsAction;
import com.samknows.measurement.net.SubmitTestResultsAnonymousAction;
import com.samknows.measurement.statemachine.StateResponseCode;

public class SubmitResultsAnonymousState extends BaseState{
	public SubmitResultsAnonymousState(MainService c){
		super(c);
	}
	
	@Override
	public StateResponseCode executeState(){
		new SubmitTestResultsAnonymousAction(ctx).execute();
		return StateResponseCode.OK;
	}

}
