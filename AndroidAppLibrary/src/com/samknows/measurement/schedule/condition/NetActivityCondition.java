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


package com.samknows.measurement.schedule.condition;

import java.util.LinkedHashMap;
import java.util.Map;

import org.w3c.dom.Element;

import com.samknows.measurement.environment.TrafficStatsCollector;
import com.samknows.measurement.test.TestContext;
import com.samknows.measurement.util.XmlUtils;

public class NetActivityCondition extends Condition{
	public static final String TYPE_VALUE= "NETACTIVITY";
	public static final String JSON_MAXBYTESIN = "maxbytesin";
	public static final String JSON_MAXBYTESOUT = "maxbytesout";
	public static final String JSON_BYTESIN = "bytesin";
	public static final String JSON_BYTESOUT = "bytesout";
	
	private static final long serialVersionUID = 1L;

	private int maxByteIn;
	private int maxByteOut;
	private long time;
	
	public static NetActivityCondition parseXml(Element node) {
		NetActivityCondition c = new NetActivityCondition();
		c.maxByteIn = Integer.valueOf(node.getAttribute("maxByteIn"));
		c.maxByteOut = Integer.valueOf(node.getAttribute("maxByteOut"));
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
		TrafficStatsCollector.Data data = TrafficStatsCollector.collectAll(time);
		boolean isSuccess = data.trx < maxByteIn && data.ttx < maxByteOut;
		ConditionResult result = new ConditionResult(isSuccess);
		result.setJSONFields(JSON_MAXBYTESIN, JSON_MAXBYTESOUT, JSON_BYTESIN, JSON_BYTESOUT);
		result.generateOut(TYPE_VALUE, String.valueOf(maxByteIn), String.valueOf(maxByteOut), String.valueOf(data.trx), String.valueOf(data.ttx));
		return result;
	}
	
}
