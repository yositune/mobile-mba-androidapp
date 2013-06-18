package com.samknows.measurement.statemachine.state;

import com.samknows.measurement.AppSettings;
import com.samknows.measurement.Logger;
import com.samknows.measurement.MainService;
import com.samknows.measurement.net.SubmitTestResultsAnonymousAction;
import com.samknows.measurement.statemachine.StateResponseCode;
import com.samknows.measurement.util.OtherUtils;

public class SubmitResultsAnonymousState extends BaseState{
	public SubmitResultsAnonymousState(MainService c){
		super(c);
	}
	
	@Override
	public StateResponseCode executeState(){
		if(!AppSettings.getInstance().isDataCapReached() || OtherUtils.isWifi(ctx)){
			new SubmitTestResultsAnonymousAction(ctx).execute();
		}else{
			Logger.d(this, "Results have not been submitted because the data cap is reached");
		}
		return StateResponseCode.OK;
	}

}
