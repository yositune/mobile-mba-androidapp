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


package com.samknows.measurement.schedule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.samknows.measurement.util.IdGenerator;
import com.samknows.measurement.util.TimeUtils;
import com.samknows.measurement.util.XmlUtils;

public class TestGroup implements Serializable{
	
	private static final long serialVersionUID = 1L;
	public long id = IdGenerator.generate();
	
	//condition group to be executed before and after
	public String conditionGroupId;
	public long netUsage = 0;
	//time the tests belonging to the group should start during the day
	public List<TestTime> times;
	
	//List of tests ids belonging to the test group
	public List<Integer> testIds;
	
	public static TestGroup parseXml(Element node){
		TestGroup ret = new TestGroup();
		ret.conditionGroupId = node.getAttribute(ScheduleConfig.CONDITION_GROUP_ID);
		ret.times = new ArrayList<TestTime>();
		
		//get the times the test group is supposed to run during the day
		NodeList list = node.getElementsByTagName(ScheduleConfig.TIME);
		for(int i = 0; i < list.getLength(); i++){
			Element ep = (Element) list.item(i);
			long time = XmlUtils.convertTestStartTime(ep.getFirstChild().getNodeValue());
			String attribute = ep.getAttribute(ScheduleConfig.RANDOM_INTERVAL);
			TestTime tt;
			if(attribute != null && ! attribute.equals("")){
				tt = new TestTime(time, XmlUtils.convertTime(attribute));
			}else{
				tt = new TestTime(time);
			}			
			ret.times.add(tt);
		}
		Collections.sort(ret.times);
		
		
		//get the list of test belonging to the test group
		ret.testIds = new ArrayList<Integer>();
		NodeList test_ids = node.getElementsByTagName(ScheduleConfig.TEST);
		for(int i=0; i < test_ids.getLength(); i++){
			Element ep = (Element) test_ids.item(i);
			ret.testIds.add(Integer.parseInt(ep.getAttribute(ScheduleConfig.ID)));
		}
		return ret;
	}
	
	public void setUsage(List<TestDescription> tds){
		for(TestDescription td: tds){
			if(testIds.contains(td.testId)){
				netUsage += td.maxUsageBytes;
			}
		}
	}
	
	
	public long getNextTime(long time){
		long ret = TestTime.NO_START_TIME;
		for(TestTime tt: times){
			if(tt.getNextStart(time) > time){
				ret = tt.getNextTime(time);
				break;
			}
		}
		
		if(ret <= time && !times.isEmpty()){
			ret = times.get(0).getNextTime(TimeUtils.getStartNextDayTime(time));
		}
		return ret;
	}
	
	
	
	public List<Long> getTimesInInterval(long startInterval, long endInterval){
		List<Long> ret = new ArrayList<Long>();
		long time = startInterval;
		while(time <= endInterval){
			for(TestTime tt: times){
				if(tt.getNextStart(time) > startInterval && tt.getNextEnd(time) < endInterval){
					ret.add(tt.getNextTime(time));
				}
			}
			time = TimeUtils.getStartNextDayTime(time);
		}
		return ret;
	}
}
