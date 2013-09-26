package com.samknows.measurement.statemachine.state;

import com.samknows.libcore.SKLogger;
import com.samknows.libcore.SKConstants;
import com.samknows.measurement.FCCAppSettings;
import com.samknows.measurement.CachingStorage;
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
	public StateResponseCode executeState() throws Exception {
		ScheduleConfig config = null;
		Storage storage = CachingStorage.getInstance();
		FCCAppSettings appSettings  = FCCAppSettings.getFCCAppSettingsInstance();
		RequestScheduleAnonymousAction action = new RequestScheduleAnonymousAction(
				ctx);
		action.execute();
		if (action.isSuccess()) {
			if (SKConstants.USE_LOCAL_CONFIG) {
				SKLogger.w(this.getClass(), "Using local config file");
				try {
    				config = ScheduleConfig.parseXml(ctx.getResources()
						.openRawResource(R.raw.schedule_example));
				} catch (Exception e) {
					// Catch, and rethrow, the exception - as we'd seen this fail in some circumstances and needed to track it down.
					SKLogger.d(this, "+++++DEBUG+++++ error (1) parsing XML!" + e.toString());
					throw(e);
					// TODO: should we NOT rethrow the exception?
					// config = null;
				}
			} else {
				try {
					// Logger.d(this, "+++++DEBUG+++++ XML to parse = " + action.content.toString());
					config = ScheduleConfig.parseXml(action.content);
				} catch (Exception e) {
					// Catch, and rethrow, the exception - as we'd seen this fail in some circumstances and needed to track it down.
					SKLogger.d(this, "+++++DEBUG+++++ error (2) parsing XML!" + e.toString());
					throw(e);
					// TODO: should we NOT rethrow the exception?
					//config = null;
				}
			}
			if(config == null){
				return StateResponseCode.FAIL;
			}
			SKLogger.d(this, "obtained config from server");
		
			if (appSettings.updateConfig(config)) {
				storage.saveScheduleConfig(config);
				SKLogger.d(this, "Update config");
			} else {
				SKLogger.d(this, "Update config is not necessary");
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
