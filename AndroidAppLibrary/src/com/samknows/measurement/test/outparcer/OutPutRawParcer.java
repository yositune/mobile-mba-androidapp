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


package com.samknows.measurement.test.outparcer;

import java.text.SimpleDateFormat;
import java.util.List;

import com.samknows.measurement.CachingStorage;
import com.samknows.measurement.Constants;
import com.samknows.measurement.schedule.ScheduleConfig;
import com.samknows.measurement.util.OtherUtils;

public class OutPutRawParcer {
	private List<ParcerDataType> datas;
	private String source[];
	private ScheduleConfig config;
	
	public OutPutRawParcer(List<ParcerDataType> datas) {
		super();
		this.datas = datas;
		config = CachingStorage.getInstance().loadScheduleConfig();
	}

	public void setSource(String source) {
		this.source = source.split(Constants.RESULT_LINE_SEPARATOR);
	}

	public String[] parce() {
		String result[] = new String[datas.size()];
		for (int i = 0; i < datas.size(); i++) {
			result[i] = parce(i);
		}
		return result;
	}
	
	public boolean isSuccess() {
		return source[2].equals(Constants.RESULT_OK);
	}
	
	public String[] headersArray() {
		String result[] = new String[datas.size()];
		int i = 0;
		for (ParcerDataType type : datas) {
			result[i++] = type.header;
		}
		return result;
	}
	
	private String parce(int idx) {
		Object value = getValue(idx);
		ParcerDataType type = datas.get(idx);
		switch (type.type) {
		case FIELD_TIME:
			return new SimpleDateFormat().format((Long)value);
		case FIELD_SPEED: {
			return OtherUtils.formatToBits((Long)value) + "/s";
		}
		case FIELD_TARGET: {
			return value.toString();
		}
		case FIELD_P_LOSS: {
			return value + "%";
		}
		case FIELD_LATENCY:
		case FIELD_JITTER: {
			if (!isSuccess()) {
				return "";
			}
			return value + "ms";
		}
		default:
			throw new RuntimeException();
		}
	}
	
	//used to compare results
	public Comparable<?> getValue(int rowIdx) {
		ParcerDataType type = datas.get(rowIdx);
		switch (type.type) {
		case FIELD_TIME:
			return Long.valueOf(source[type.idx]) * 1000;
		case FIELD_SPEED: {
			return Long.valueOf(source[type.idx]);
		}
		case FIELD_TARGET: {
			return config.findHostName(source[type.idx]);
		}
		case FIELD_LATENCY: {
			if (!isSuccess()) {
				return "";
			}
			return Long.valueOf(source[type.idx])/1000;
		}
		case FIELD_P_LOSS: {
			if (!isSuccess()) {
				return "";
			}
			long loss = Long.valueOf(source[10]);
			long send = Long.valueOf(source[9]);
			return 100*loss/send;
		}
		case FIELD_JITTER: {
			if (!isSuccess()) { 
				return "";
			}
			return Long.valueOf(source[12])/1000;

		}
		default:
			throw new RuntimeException();
		}
	}
}
