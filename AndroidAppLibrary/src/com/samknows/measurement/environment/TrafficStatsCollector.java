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
