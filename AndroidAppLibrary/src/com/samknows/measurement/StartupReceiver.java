package com.samknows.measurement;

import com.samknows.measurement.statemachine.State;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartupReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		AppSettings.getInstance().saveState(State.NONE);
		Logger.d(this, "State saved, None");
		context.startService(new Intent(context, MainService.class));
	}

}
