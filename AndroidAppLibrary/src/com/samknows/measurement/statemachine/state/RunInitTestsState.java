package com.samknows.measurement.statemachine.state;

import android.content.Context;

import com.samknows.measurement.CachingStorage;
import com.samknows.measurement.Logger;
import com.samknows.measurement.MainService;
import com.samknows.measurement.Storage;
import com.samknows.measurement.schedule.ScheduleConfig;
import com.samknows.measurement.schedule.TestDescription;
import com.samknows.measurement.statemachine.StateResponseCode;
import com.samknows.measurement.test.TestContext;
import com.samknows.measurement.test.TestExecutor;
import com.samknows.measurement.test.TestResult;

public class RunInitTestsState extends BaseState{

	public RunInitTestsState(MainService c) {
		super(c);
	}

	@Override
	public StateResponseCode executeState() {
		Storage storage = CachingStorage.getInstance();
		ScheduleConfig sc = storage.loadScheduleConfig();
		if(sc  == null){
			Logger.e(this, "There is no schedule config");
			return StateResponseCode.FAIL;
		}
		TestContext testContext = TestContext.create(ctx);
		TestExecutor testExecutor = new TestExecutor(testContext);
		testExecutor.startInBackGround();
		for (String type : sc.initTestTypes) {
			TestDescription td = sc.findTestForType(type);
			if (td == null) {
				Logger.e(this, "no init test for type: " + type);
			} else {
				TestResult result = testExecutor.execute(td.id);
				if (!result.isSuccess) {
					return StateResponseCode.FAIL;
				}
			}
		}
		testExecutor.stop();
		testExecutor.save("init_test");
		storage.saveTestParamsManager(testContext.paramsManager);
		
		return StateResponseCode.OK;
	}

}
