package com.samknows.measurement.statemachine;

import java.util.Map;

import com.samknows.measurement.SK2AppSettings;

public abstract class Transition {
	
	public static Transition create(SK2AppSettings as){
		//if(as.anonymous){} else {}
		
		// Always return an instance of TransitionAnonymous...
		return new TransitionAnonymous();
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
