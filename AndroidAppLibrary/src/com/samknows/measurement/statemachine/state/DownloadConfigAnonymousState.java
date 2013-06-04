package com.samknows.measurement.statemachine.state;

import com.samknows.measurement.AppSettings;
import com.samknows.measurement.CachingStorage;
import com.samknows.measurement.Constants;
import com.samknows.measurement.Logger;
import com.samknows.measurement.MainService;
import com.samknows.measurement.R;
import com.samknows.measurement.Storage;
import com.samknows.measurement.net.RequestScheduleAnonymousAction;
import com.samknows.measurement.schedule.ScheduleConfig;
import com.samknows.measurement.statemachine.StateResponseCode;

public class DownloadConfigAnonymousState extends BaseState {

	public DownloadConfigAnonymousState(MainService c) {
		super(c);
	}

	@Override
	public StateResponseCode executeState() {
		ScheduleConfig config = null;
		Storage storage = CachingStorage.getInstance();
		AppSettings appSettings  = AppSettings.getInstance();
		RequestScheduleAnonymousAction action = new RequestScheduleAnonymousAction(
				ctx);
		action.execute();
		if (action.isSuccess()) {
			if (Constants.USE_LOCAL_CONFIG) {
				Logger.w(this.getClass(), "Using local config file");
				config = ScheduleConfig.parseXml(ctx.getResources()
						.openRawResource(R.raw.schedule_example));
			} else {
				config = ScheduleConfig.parseXml(action.content);
			}
			if(config == null){
				return StateResponseCode.FAIL;
			}
			Logger.d(this, "obtained config from server");
		
			if (appSettings.updateConfig(config)) {
				storage.saveScheduleConfig(config);
				Logger.d(this, "Update config");
			} else {
				Logger.d(this, "Update config is not necessary");
				return StateResponseCode.OK;
			}
			storage.dropExecutionQueue();
			storage.dropParamsManager();
			appSettings.setConfig(config);
			return StateResponseCode.NOT_OK;
		}

		return StateResponseCode.FAIL;
	}
}
