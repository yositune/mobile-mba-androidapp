package com.samknows.measurement.statemachine.state;

import com.samknows.libcore.SKLogger;
import com.samknows.measurement.SK2AppSettings;
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
		if(!SK2AppSettings.getInstance().isDataCapReached() || OtherUtils.isWifi(ctx)){
			new SubmitTestResultsAnonymousAction(ctx).execute();
		}else{
			SKLogger.d(this, "Results have not been submitted because the data cap is reached");
		}
		return StateResponseCode.OK;
	}

}
