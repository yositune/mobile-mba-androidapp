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

import com.samknows.measurement.util.TimeUtils;

public class TestTime implements Serializable, Comparable<TestTime> {
	public static final long NO_START_TIME = -1;
	private static final long serialVersionUID = 1L;
	public Long mTime;
	public Long mRandomInterval;
	
	public TestTime(Long time){
		mTime = time;
		mRandomInterval = 0l;
	}
	
	public TestTime(Long time, Long random_interval){
		mTime = time;
		mRandomInterval = random_interval;
	}
	
	@Override
	public int compareTo(TestTime another) {
		return this.mTime.compareTo(another.mTime);
	}
	
	public long getTime(){
		return mTime;
	}
	
	public long getEndInterval(){
		return mTime + mRandomInterval;
	}
	
	public long getRandomizedTime(){
		return mTime + getRandom();
	}
	
	public long getRandom(){
		return (long)(Math.random()*mRandomInterval);
	}
	
	public long getNextStart(long time){
		return TimeUtils.getStartDayTime(time) + mTime;
	}
	
	public long getNextStart(){
		return getNextStart(System.currentTimeMillis());
	}
	
	public long getNextEnd(long time){
		
		return TimeUtils.getStartDayTime(time) + mTime + mRandomInterval;
	}
	
	public long getNextEnd(){
		return getNextEnd(System.currentTimeMillis());
	}
	
	public long getNextTime(long time){
		return TimeUtils.getStartDayTime(time) + getRandomizedTime(); 
	}
	
	public long getNextTime(){
		return getNextTime(System.currentTimeMillis());
	}
	
}