package com.samknows.measurement.environment;

import java.io.Serializable;

import android.content.Context;
import android.net.TrafficStats;
import android.os.Process;

import com.samknows.libcore.SKLogger;
import com.samknows.libcore.SKConstants;

public class TrafficStatsCollector extends BaseDataCollector implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public TrafficStatsCollector(Context context) {
		super(context);
	}

	private TrafficData start = new TrafficData();
	private TrafficData end = new TrafficData();
	private int uid;
	
	
	public void start() {
		start = collectTraffic();
		uid = Process.myUid();
	}
	
	public long finish() {
		end = collectTraffic();
		return end.appRxBytes - start.appRxBytes + end.appTxBytes - start.appTxBytes;
	}
	
	
	public static TrafficData collectAll(long interval) {
		
		try {
			Thread.sleep(SKConstants.NET_ACTIVITY_CONDITION_WAIT_TIME);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		TrafficData a = collectTraffic();
		
		SKLogger.d(TrafficStatsCollector.class, "start collecting netData for " + interval/1000 + "s");
		
		try {
			Thread.sleep(interval);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		TrafficData b = collectTraffic();
		SKLogger.d(TrafficStatsCollector.class, "finished collecting netData in: " + (b.time - a.time)/1000 + "s");
		
		return TrafficData.interval(a, b);
	}
	
	public DCSData collect(){
		return collectTraffic();
	}
	
	
	public static TrafficData collectTraffic(){
		TrafficData ret = new TrafficData();
		ret.time = System.currentTimeMillis();
		ret.mobileRxBytes = TrafficStats.getMobileRxBytes();
		ret.mobileTxBytes = TrafficStats.getMobileTxBytes();
		ret.totalRxBytes = TrafficStats.getTotalRxBytes();
		ret.totalTxBytes = TrafficStats.getTotalTxBytes();
		int uid = Process.myUid();
		ret.appRxBytes = TrafficStats.getUidRxBytes(uid);
		ret.appTxBytes = TrafficStats.getUidTxBytes(uid);
		return ret;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

		
}
