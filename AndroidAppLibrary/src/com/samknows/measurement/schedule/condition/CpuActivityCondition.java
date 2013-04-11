package com.samknows.measurement.schedule.condition;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;

import com.samknows.measurement.Logger;
import com.samknows.measurement.environment.linux.CpuUsageReader;
import com.samknows.measurement.test.TestContext;
import com.samknows.measurement.util.XmlUtils;

public class CpuActivityCondition extends Condition {
	private static final long serialVersionUID = 1L;
	public static final String TYPE_VALUE = "CPUACTIVITY";
	public static final String JSON_MAX_AVG = "max_average";
	public static final String JSON_READ_AVG = "read_average";

	private int maxAvg;
	private long time;
	
	public static CpuActivityCondition parseXml(Element node) {
		CpuActivityCondition c = new CpuActivityCondition();
		c.maxAvg = Integer.valueOf(node.getAttribute("maxAvg"));
		String time = node.getAttribute("time");
		c.time = XmlUtils.convertTime(time);
		return c;
	}

	@Override
	public boolean needSeparateThread() {
		return true;
	}

	@Override
	public ConditionResult doTestBefore(TestContext tc) {
		int cpuLoad = (int) new CpuUsageReader().read(time);
		boolean isSuccess = cpuLoad < maxAvg;
		
		ConditionResult result = new ConditionResult(isSuccess);
		result.setJSONFields(JSON_MAX_AVG,JSON_READ_AVG);
		result.generateOut(TYPE_VALUE, String.valueOf(maxAvg), String.valueOf(cpuLoad));
		return result;
	}
	
}
