package com.samknows.measurement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.samknows.measurement.schedule.OutParamDescription;
import com.samknows.tests.Param;

public class TestParamsManager implements Serializable {
	private static final long serialVersionUID = 1L;
	private HashMap<String, TestParam> map = new HashMap<String, TestParam>();
	
	public void put(String name, String value) {
		Logger.d(this, "saving param: " + name + " with value: " + value);
		map.put(name, new TestParam(name, value));
	}
	
	public List<Param> prepareParams(List<Param> params) {
		List<Param> result = new ArrayList<Param>();
		StringBuilder sb = new StringBuilder();
		for (Param p : params) {
			sb.append(p.getName()).append(" ").append(p.getValue()).append(". ");
			if (p.getValue().startsWith(Constants.PARAM_PREFIX)) {
				String name = p.getValue().substring(Constants.PARAM_PREFIX.length());
				TestParam newParam = map.get(name);
				if (newParam != null) {
					Logger.d(this, "replacing value: " + p.getValue() + " with: " + newParam.value);
					result.add(new Param(p.getName(), newParam.value));
				} else {
					Logger.e(this, "can't replace param: " + p.getName() + " with value: " + p.getValue(), new RuntimeException());
				}
			} else {
				result.add(p);
			}
		}
		Logger.d(this, "Test params are: "+sb.toString());
		return result;
	}
	
	public void processOutParams(String out, List<OutParamDescription> outParamsDescription) {
		String data[] = out.split(Constants.RESULT_LINE_SEPARATOR);
		for (OutParamDescription pd : outParamsDescription) {
			put(pd.name, data[pd.idx]);
		}
	}
	
	public boolean isExpiried(String param, long expTime) {
		TestParam p = map.get(param);
		if (p == null) {
			Logger.e(this, "can not find param for name: " + param);
			return true;
		}
		return (p.createdTime + expTime) < System.currentTimeMillis();
	}
	
	public boolean hasParam(String param) {
		return map.get(param) != null;
	}

	private class TestParam implements Serializable{
		private static final long serialVersionUID = 1L;
		public String name, value;
		public long createdTime;
		public TestParam(String name, String value) {
			super();
			this.name = name;
			this.value = value;
			createdTime = System.currentTimeMillis();
		}
	}
}
