package com.samknows.measurement.schedule.condition;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.w3c.dom.Element;

import com.samknows.measurement.Logger;
import com.samknows.measurement.test.TestContext;
import com.samknows.measurement.util.SimpleFuture;

public abstract class Condition implements Serializable{
	public static final String CONDITION_TYPE = "condition_type";
	public static final String SUCCESS = "success";
	private static final long serialVersionUID = 1L;
	
	public enum Type {
		NetActivity, CpuActivity, NetworkType, LocationAvailable, ParamExpired
	}

	public abstract ConditionResult doTestBefore(TestContext tc);
	public ConditionResult doTestAfter(TestContext tc) {return new ConditionResult(true);};
	public void release(TestContext tc){}
	protected abstract boolean needSeparateThread();
	
	/**
	 * async method with FutureTask pattern. It may return result immediately though. 
	 */
	public Future<ConditionResult> testBefore(final TestContext ctx) {
		if (!needSeparateThread()) {
			return new SimpleFuture<ConditionResult>(doTestBefore(ctx));
		} else {
			return new FutureTask<ConditionResult>(new Callable<ConditionResult>() {
				@Override
				public ConditionResult call() throws Exception {
					return doTestBefore(ctx);
				}
			});
		}
	}
	
	public ConditionResult testAfter(TestContext ctx) {return doTestAfter(ctx);}
	
	public static Condition parseXml(Element node) {
		Condition c = null;
		try {
			Type type = Type.valueOf(node.getAttribute("type"));
			
			switch (type) {
				case NetActivity : c = NetActivityCondition.parseXml(node); break;
				case CpuActivity : c = CpuActivityCondition.parseXml(node); break;
				case NetworkType : c = NetworkTypeCondition.parseXml(node); break;
				case LocationAvailable : c = LocationAvailableCondition.parseXml(node); break;
				case ParamExpired : c = ParamExpiredCondition.parseXml(node); break;
				default : Logger.e(Condition.class, "not such condition: " + type, new RuntimeException());
			}
		} catch (Exception e) {
			e.printStackTrace();
			//skip it
		}
		return c;
	}
}
