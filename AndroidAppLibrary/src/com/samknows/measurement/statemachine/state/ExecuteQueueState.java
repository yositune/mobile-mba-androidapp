package com.samknows.measurement.statemachine.state;


import com.samknows.measurement.AppSettings;
import com.samknows.measurement.CachingStorage;
import com.samknows.measurement.Logger;
import com.samknows.measurement.MainService;
import com.samknows.measurement.Storage;
import com.samknows.measurement.statemachine.StateResponseCode;
import com.samknows.measurement.test.ExecutionQueue;
import com.samknows.measurement.test.TestContext;
import com.samknows.measurement.util.OtherUtils;

public class ExecuteQueueState extends BaseState{

	public ExecuteQueueState(MainService ctx) {
		super(ctx);
	}

	@Override
	public StateResponseCode executeState() {
		if(!AppSettings.getInstance().isServiceEnabled()){
			return StateResponseCode.OK;
		}
		
		Storage storage = CachingStorage.getInstance();
		TestContext tc = TestContext.create(ctx);
		
		ExecutionQueue queue = storage.loadQueue();
		if (queue == null) {
			Logger.e(this, "fail to load execution queue, creating new...");
			queue = new ExecutionQueue(tc);
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
