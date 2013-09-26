package com.samknows.measurement.statemachine;

import java.util.Map;

import com.samknows.measurement.FCCAppSettings;

public abstract class Transition {
	
	public static Transition create(FCCAppSettings as){
		if(as.anonymous){
			return new TransitionAnonymous();
		}else{
			return new TransitionUser();
		}
	}
	
	public abstract String getType();
	protected abstract Map<State, String[]> getTransition();
	
	public State getNextState(State state, StateResponseCode code) {
		String[] state_transiction = getTransition().get(state);
		if(state_transiction == null){
			throw new RuntimeException(getType() +" state machine doesn't define state: "+ state);
		}
		for (String s : state_transiction) {
			if (s.startsWith(code.toString())) {
				return State.valueOf(s.split(":")[1]);
			}
		}
		throw new RuntimeException(getType() +" does not define " +state +" ->" + code);
		
	}
}
