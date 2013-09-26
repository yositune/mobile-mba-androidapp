package com.samknows.measurement;

import com.samknows.libcore.SKLogger;
import com.samknows.measurement.statemachine.State;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartupReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		FCCAppSettings a = FCCAppSettings.getFCCAppSettingsInstance();
		a.saveState(State.NONE);
		SKLogger.d(this, "State saved, None");
		MainService.poke(context);
		if(a.collect_traffic_data){
			NetUsageService.init(context, a.collect_traffic_data_interval);
		}
		
	}
	
	

}
