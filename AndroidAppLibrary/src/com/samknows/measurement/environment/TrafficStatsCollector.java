package com.samknows.measurement.environment;

import android.net.TrafficStats;
import android.os.Process;

import com.samknows.measurement.Constants;
import com.samknows.measurement.Logger;

public class TrafficStatsCollector {
	private long bytesR, bytesT;
	private int uid;
	public void start() {
		uid = Process.myUid();
		bytesR = TrafficStats.getUidRxBytes(uid);
		bytesT = TrafficStats.getUidTxBytes(uid);
	}
	
	public long finish() {
		bytesR = TrafficStats.getUidRxBytes(uid) - bytesR;
		bytesT = TrafficStats.getUidTxBytes(uid) - bytesT;
		return bytesR + bytesT;
	}
	
	public static class Data {
		public long trx;
		public long ttx;
		
		public long sum() {
			return trx + ttx;
		}
	}
	
	public static Data collectAll(long interval) {
		
		try {
			Thread.sleep(Constants.NET_ACTIVITY_CONDITION_WAIT_TIME);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		long startTime = System.currentTimeMillis();
		Logger.d(TrafficStatsCollector.class, "start collecting netData for " + interval/1000 + "s");
		
		Data data = new Data();
		data.trx = TrafficStats.getTotalRxBytes();
		data.ttx = TrafficStats.getTotalTxBytes();
		
		try {
			Thread.sleep(interval);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Logger.d(TrafficStatsCollector.class, "finished collecting netData in: " + (System.currentTimeMillis() - startTime)/1000 + "s");
		
		data.trx = TrafficStats.getTotalRxBytes() - data.trx;
		data.ttx = TrafficStats.getTotalTxBytes() - data.ttx;
		return data;
	}
}
