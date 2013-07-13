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


package com.samknows.measurement.util;

import org.w3c.dom.Element;

import com.samknows.measurement.schedule.TestDescription;

public class XmlUtils {
	public static String getNodeAttrValue(Element parent, String nodeName, String attrName) {
		try {
			return ((Element)parent.getElementsByTagName(nodeName).item(0)).getAttribute(attrName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 *  convert time from xml to millis. Time examples - 30s, 30m, 30h, 30d
	 */
	public static long convertTime(String original) {
		String time = original.substring(0, original.length() - 1);
		long number = Long.valueOf(time);
		
		if (original.endsWith("s")) {
			return number*1000;
		} else if (original.endsWith("m")) {
			return TimeUtils.minutesToMillis(number);
		} else if (original.endsWith("h")) {
			return TimeUtils.hoursToMillis(number);
		} else if (original.endsWith("d")) {
			return TimeUtils.daysToMillis(number);
		} else {
			throw new RuntimeException("failed to parse time: " + original);
		}
	}
	
	/**
	 * in example 13:20. hh:mm
	 * @param original
	 * @return
	 */
	public static long convertTestStartTime(String original) {
		if (original == null || original.equals("")) return TestDescription.NO_START_TIME;
		String parts[] = original.split(":");
		return TimeUtils.hoursToMillis(Long.valueOf(parts[0])) + TimeUtils.minutesToMillis(Long.valueOf(parts[1])); 
	}
	
}

