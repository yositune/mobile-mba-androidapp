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

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.PriorityQueue;

import com.samknows.measurement.AppSettings;
import com.samknows.measurement.Constants;
import com.samknows.measurement.Logger;
import com.samknows.measurement.schedule.TestDescription;
import com.samknows.measurement.schedule.TestGroup;
import com.samknows.measurement.schedule.failaction.RetryFailAction;
import com.samknows.measurement.util.TimeUtils;

public class ExecutionQueue implements Serializable{
	private static final long serialVersionUID = 1L;

	private long startTime;
	private long endTime; //end time for the queue
	private PriorityQueue<QueueEntry> entries = new PriorityQueue<QueueEntry>();
	
	private transient TestContext tc;
	
	private ExecutionQueue() {};
	
	public ExecutionQueue(TestContext tc) {
		this.tc = tc;
		startTime = endTime = System.currentTimeMillis();
		
		long daysDiff = TimeUtils.daysToMillis(Constants.TEST_QUEUE_MAX_SIZE_IN_DAYS);
		long newEndTime = startTime + daysDiff;
		populate(newEndTime);
	}
	
	private void extendSize() {
		long minSize = TimeUtils.daysToMillis(Constants.TEST_QUEUE_NORMAL_SIZE_IN_DAYS);
		long currentSize = endTime - System.currentTimeMillis();
		if (currentSize < minSize) {
			Logger.d(this, "extending queue");
			long maxSize = TimeUtils.daysToMillis(Constants.TEST_QUEUE_MAX_SIZE_IN_DAYS);
			long newEndSize = System.currentTimeMillis() + maxSize;
			populate(newEndSize);
		} else {
			Logger.d(this, "no need to extend queue, endTime: " + TimeUtils.logString(endTime));
		}
	}

	private synchronized void populate(long newEndTime) {
		long timeNow = System.currentTimeMillis();
		startTime = endTime >= timeNow ? endTime : timeNow;
		endTime = newEndTime;
		Logger.d(this, "populating test queue from: " + TimeUtils.logString(startTime) + " to " + TimeUtils.logString(endTime));
		for(TestGroup tg: tc.config.testGroups){
			for(Long t:tg.getTimesInInterval(startTime, endTime)){
				Logger.d(this, "Add test group id "+ tg.id +" at time: "+TimeUtils.logString(t) );
				addEntry(t, tg);
			}
			
		}
		Logger.d(this, "queue populated with: " + entries.size());
	}
	
	public void addEntry(long time, TestGroup tg){
		entries.add(new QueueEntry(time, tg.id, tc.config.testGroups.indexOf(tg)));
		Logger.d(this, "scheduling test group at: "+TimeUtils.logString(time));
	}
	/*
	public void addEntry(long time, TestDescription td) {
		entries.add(new QueueEntry(time, td.id, tc.config.tests.indexOf(td)));
		Logger.d(this, "scheduling test at: " + new SimpleDateFormat().format(new Date(time)));
	}
	*/
	public void setTestContext(TestContext tc) {
		this.tc  = tc;
	}
	
	/**
	 * @return reschedule time
	 */
	public long execute() {
		TestExecutor executor = new TestExecutor(tc);
		long time = System.currentTimeMillis();
		
		//drop old tests 
		while (true){
			QueueEntry entry = entries.peek();
			if (entry != null && !canExecute(entry, time) && entry.time < time) {
				entries.remove();
				Logger.d(this, "removing test scheduled at: " + new SimpleDateFormat().format(entry.time));
			} else {
				break;
			}
		}
		
		boolean result = true;
		boolean fail = false;
		if (canExecute(time)) {
			executor.start();
			
			while (canExecute(time)) {
				QueueEntry entry = entries.remove();
				//Using the double value to ensure that the data usage never exceed the data cap
				long maximumTestUsage = tc.config == null ? 0: tc.config.maximumTestUsage*2;
				//if data cap is going to be breached do not run test
				if(!AppSettings.getInstance().isDataCapReached(maximumTestUsage)){
					TestResult tr = executor.executeGroup(entry.groupId);
					result = tr.isSuccess;
				}else{
					Logger.d(this, "Active metrics won't be collected due to potential datacap breach");
				}
			}
			
			executor.stop();
			executor.save("scheduled_tests");
		}
		
		extendSize();
		
		if (fail) {
			RetryFailAction failAction = tc.config.retryFailAction;
			if (failAction != null) {
				return tc.config.retryFailAction.delay; //reschedule
			} else {
				Logger.e(this, "can't find on test fail action, just skiping the test.");
				entries.remove();
			}
		} 
		
		return getSleepTime();
	}
	
	public boolean canExecute(long time) {
		QueueEntry entry = entries.peek();
		if (entry == null) {
			return false;
		} else {
			return canExecute(entry, time);
		}
	}
	
	public boolean canExecute(QueueEntry entry, long time) {
		return AppSettings.getInstance().getTestStartWindow()/2 > Math.abs(entry.time - time);
	}
	
	private long getSleepTime() {
		if (entries.isEmpty()) {
			return TimeUtils.daysToMillis(Constants.TEST_QUEUE_NORMAL_SIZE_IN_DAYS);
		} else {
			QueueEntry entry = entries.peek();
			return entry.time - System.currentTimeMillis();
		}
	}
	
	public int size() {
		return entries.size();
	}
	
	class QueueEntry implements Serializable, Comparable<QueueEntry>{
		private static final long serialVersionUID = 1L;
		long time;
		long groupId;
		int orderIdx;
		
		public QueueEntry(long time, long groupId, int orderIdx) {
			super();
			this.time = time;
			this.groupId = groupId;
			this.orderIdx = orderIdx;
		}

		@Override
		public int compareTo(QueueEntry another) {
			if (time == another.time) { //if time is the same we want to the save original order from config
				return Integer.valueOf(orderIdx).compareTo(another.orderIdx);
			}
			return Long.valueOf(time).compareTo(another.time);
		}

		@Override
		public String toString() {
			return groupId + " : " + TimeUtils.logString(time);
		}
			
	}
	
	
}
