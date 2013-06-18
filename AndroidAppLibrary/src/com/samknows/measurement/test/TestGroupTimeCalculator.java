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


package com.samknows.measurement.test;


public class TestGroupTimeCalculator {
/*	private TestGroup tg;
	private long time;
	
	public TestGroupTimeCalculator(TestGroup tg){
		this(tg, System.currentTimeMillis());
	}
	
	public TestGroupTimeCalculator(TestGroup tg, long startTime){
		super();
		this.tg = tg;
		this.time = startTime;
	}
	
	public static long nextTime(long time, TestGroup tg){
		long ret = TestGroup.NO_START_TIME;
		long startOfTheDay = TimeUtils.getStartDayTime(time);
		for(TestTime dayTime: tg.times){
			String st = TimeUtils.logString(startOfTheDay + dayTime.getTime());
			String et = TimeUtils.logString(startOfTheDay + dayTime.getEndInterval());
			ret = startOfTheDay + dayTime.getRandomizedTime();
			String rt = TimeUtils.logString(ret);
			Logger.d(TestGroup.class,"Start interval "+st+ " end interval" +et+" time " +rt);
			if(ret > time){
				break;
			}
		}
		
		if(ret == TestGroup.NO_START_TIME || ret <= time){
			if(!tg.times.isEmpty()){
				ret = TimeUtils.getStartNextDayTime(time) + tg.times.get(0).getRandomizedTime();
				
			}
		}
		time = ret;
		return ret;
	}
	
	public long nextTime(){
		long ret = TestGroup.NO_START_TIME;
		long startOfTheDay = TimeUtils.getStartDayTime(time);
		for(TestTime dayTime: tg.times){
			String st = TimeUtils.logString(startOfTheDay + dayTime.getTime());
			String et = TimeUtils.logString(startOfTheDay + dayTime.getEndInterval());
			ret = startOfTheDay + dayTime.getRandomizedTime();
			String rt = TimeUtils.logString(ret);
			Logger.d(this,"Start interval "+st+ " end interval" +et+" time " +rt);
			if(ret > time){
				break;
			}
		}
		
		if(ret == TestGroup.NO_START_TIME || ret <= time){
			if(!tg.times.isEmpty()){
				ret = TimeUtils.getStartNextDayTime(time) + tg.times.get(0).getRandomizedTime();
				
			}
		}
		time = ret;
		return ret;
	}*/
}
