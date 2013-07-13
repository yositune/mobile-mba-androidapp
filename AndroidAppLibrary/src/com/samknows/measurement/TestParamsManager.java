/*
2013 Measuring Broadband America Program
Mobile Measurement Android Application
Copyright (C) 2012  SamKnows Ltd.

The FCC Measuring Broadband America (MBA) Program's Mobile Measurement Effort developed in cooperation with SamKnows Ltd. and diverse stakeholders employs an client-server based anonymized data collection approach to gather broadband performance data in an open and transparent manner with the highest commitment to protecting participants privacy.  All data collected is thoroughly analyzed and processed prior to public release to ensure that subscribers’ privacy interests are protected.

Data related to the radio characteristics of the handset, information about the handset type and operating system (OS) version, the GPS coordinates available from the handset at the time each test is run, the date and time of the observation, and the results of active test results are recorded on the handset in JSON(JavaScript Object Notation) nested data elements within flat files.  These JSON files are then transmitted to storage servers at periodic intervals after the completion of active test measurements.

This Android application source code is made available under the GNU GPL2 for testing purposes only and intended for participants in the SamKnows/FCC Measuring Broadband American program.  It is not intended for general release and this repository may be disabled at any time.


This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/


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
