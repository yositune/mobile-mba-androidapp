package com.samknows.measurement.statemachine.state;


import com.samknows.libcore.SKLogger;
import com.samknows.measurement.FCCAppSettings;
import com.samknows.measurement.CachingStorage;
import com.samknows.measurement.MainService;
import com.samknows.measurement.Storage;
import com.samknows.measurement.statemachine.StateResponseCode;
import com.samknows.measurement.test.ScheduledTestExecutionQueue;
import com.samknows.measurement.test.TestContext;
import com.samknows.measurement.util.OtherUtils;

public class ExecuteScheduledTestQueueState extends BaseState{

	public ExecuteScheduledTestQueueState(MainService ctx) {
		super(ctx);
	}

	@Override
	public StateResponseCode executeState() {
		if(!FCCAppSettings.getInstance().isServiceEnabled()){
			return StateResponseCode.OK;
		}
		
		Storage storage = CachingStorage.getInstance();
		TestContext tc = TestContext.create(ctx);
		
		ScheduledTestExecutionQueue queue = storage.loadQueue();
		if (queue == null) {
			SKLogger.e(this, "fail to load execution queue, creating new...");
			queue = new ScheduledTestExecutionQueue(tc);
		} else {
			queue.setTestContext(tc);
		}
		long testRun = queue.execute();
		
		storage.saveExecutionQueue(queue);
		storage.saveTestParamsManager(tc.paramsManager);
		
		// schedule from Queue or from config refresh
		OtherUtils.reschedule(ctx, testRun);
		
		return StateResponseCode.OK;
	}
}
