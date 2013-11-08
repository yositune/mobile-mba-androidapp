package com.samknows.measurement.statemachine.state;

import com.samknows.measurement.MainService;
import com.samknows.measurement.statemachine.StateResponseCode;

public abstract class BaseState {
	protected MainService ctx;
	
	public BaseState(MainService c) {
		super();
		this.ctx = c;
	}
	
	public abstract StateResponseCode executeState() throws Exception;
}
