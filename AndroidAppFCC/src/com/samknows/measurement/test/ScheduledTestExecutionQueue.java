package com.samknows.measurement.test;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.PriorityQueue;

import com.samknows.libcore.SKLogger;
import com.samknows.libcore.SKConstants;
import com.samknows.measurement.FCCAppSettings;
import com.samknows.measurement.schedule.TestGroup;
import com.samknows.measurement.schedule.failaction.RetryFailAction;
import com.samknows.measurement.util.TimeUtils;

public class ScheduledTestExecutionQueue implements Serializable{
	private static final long serialVersionUID = 1L;

	private long startTime;
	private long endTime; //end time for the queue
	private PriorityQueue<QueueEntry> entries = new PriorityQueue<QueueEntry>();
	
	private transient TestContext tc;
	
	private ScheduledTestExecutionQueue() {};
	
	public ScheduledTestExecutionQueue(TestContext tc) {
		this.tc = tc;
		startTime = endTime = System.currentTimeMillis();
		
		long daysDiff = TimeUtils.daysToMillis(SKConstants.TEST_QUEUE_MAX_SIZE_IN_DAYS);
		long newEndTime = startTime + daysDiff;
		populate(newEndTime);
	}
	
	private void extendSize() {
		long minSize = TimeUtils.daysToMillis(SKConstants.TEST_QUEUE_NORMAL_SIZE_IN_DAYS);
		long currentSize = endTime - System.currentTimeMillis();
		if (currentSize < minSize) {
			SKLogger.d(this, "extending queue");
			long maxSize = TimeUtils.daysToMillis(SKConstants.TEST_QUEUE_MAX_SIZE_IN_DAYS);
			long newEndSize = System.currentTimeMillis() + maxSize;
			populate(newEndSize);
		} else {
			SKLogger.d(this, "no need to extend queue, endTime: " + TimeUtils.logString(endTime));
		}
	}

	private synchronized void populate(long newEndTime) {
		long timeNow = System.currentTimeMillis();
		startTime = endTime >= timeNow ? endTime : timeNow;
		endTime = newEndTime;
		SKLogger.d(this, "populating test queue from: " + TimeUtils.logString(startTime) + " to " + TimeUtils.logString(endTime));
		for(TestGroup tg: tc.config.testGroups){
			for(Long t:tg.getTimesInInterval(startTime, endTime)){
				SKLogger.d(this, "Add test group id "+ tg.id +" at time: "+TimeUtils.logString(t) );
				addEntry(t, tg);
			}
			
		}
		SKLogger.d(this, "queue populated with: " + entries.size());
	}
	
	public void addEntry(long time, TestGroup tg){
		entries.add(new QueueEntry(time, tg.id, tc.config.testGroups.indexOf(tg)));
		SKLogger.d(this, "scheduling test group at: "+TimeUtils.logString(time));
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
		TestExecutor scheduledTestExecutor = new TestExecutor(tc);
		long time = System.currentTimeMillis();
		
		//drop old tests 
		while (true){
			QueueEntry entry = entries.peek();
			if (entry != null && !canExecute(entry, time) && entry.time < time) {
				entries.remove();
				SKLogger.d(this, "removing test scheduled at: " + new SimpleDateFormat().format(entry.time));
			} else {
				break;
			}
		}
		
		boolean result = true;
		boolean fail = false;
		if (canExecute(time)) {
			scheduledTestExecutor.start();
			
			while (canExecute(time)) {
				QueueEntry entry = entries.remove();
				long maximumTestUsage = tc.config == null ? 0: tc.config.maximumTestUsage;
				//if data cap is going to be breached do not run test
				if(!FCCAppSettings.getFCCAppSettingsInstance().isDataCapReached(maximumTestUsage)){
					TestResult tr = scheduledTestExecutor.executeGroup(entry.groupId);
					result = tr.isSuccess;
				}else{
					SKLogger.d(this, "Active metrics won't be collected due to potential datacap breach");
				}
			}
			
			scheduledTestExecutor.stop();
			scheduledTestExecutor.save("scheduled_tests");
		}
		
		extendSize();
		
		if (fail) {
			RetryFailAction failAction = tc.config.retryFailAction;
			if (failAction != null) {
				return tc.config.retryFailAction.delay; //reschedule
			} else {
				SKLogger.e(this, "can't find on test fail action, just skiping the test.");
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
		return FCCAppSettings.getFCCAppSettingsInstance().getTestStartWindow()/2 > Math.abs(entry.time - time);
	}
	
	private long getSleepTime() {
		if (entries.isEmpty()) {
			return TimeUtils.daysToMillis(SKConstants.TEST_QUEUE_NORMAL_SIZE_IN_DAYS);
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
