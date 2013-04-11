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

import org.w3c.dom.Element;

import com.samknows.measurement.Logger;
import com.samknows.measurement.schedule.datacollection.LocationDataCollector;
import com.samknows.measurement.test.TestContext;
import com.samknows.measurement.util.XmlUtils;

public class LocationAvailableCondition extends Condition{
	private static final long serialVersionUID = 1L;
	private long waitTime;
	
	@Override
	public ConditionResult doTestBefore(TestContext tc) {
		LocationDataCollector collector = tc.findLocationDataCollector();
		boolean result = false;
		String explanation = null;
		if (collector != null) {
			Logger.d(this, "start waiting for location: " + waitTime/1000 + "s");
			result = collector.waitForLocation(waitTime);
			if (!result) {
				explanation = "TIMEOUT";
			}
		} else {
			Logger.e(this, "can't get LocationDataCollector!");
			explanation = "NO_DATA_COLLECTOR";
		}
		ConditionResult res = new ConditionResult(result);
		res.generateOut("LOCATIONAVAILABLE", explanation);
		Logger.d(this, "stop waiting for location");
		return res;
	}

	@Override
	protected boolean needSeparateThread() {
		return true;
	}

	public static LocationAvailableCondition parseXml(Element node) {
		LocationAvailableCondition c = new LocationAvailableCondition();
		String time = node.getAttribute("waitTime");
		c.waitTime = XmlUtils.convertTime(time);
		return c;
	}
}
