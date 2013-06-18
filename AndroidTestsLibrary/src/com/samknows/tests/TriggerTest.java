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


package com.samknows.tests;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class TriggerTest extends Test {

	@Override
	public void execute() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getNetUsage() {
		// TODO Auto-generated method stub
		return 0;
	}
//	public enum TESTTYPE {
//		BANDWIDTH, LATENCY
//	};
//
//	List<String> requestParam;
//
//	String cgiPath;
//
//	TESTTYPE testType;
//
//	public TriggerTest(String unitIP) {
//		super(unitIP);
//		requestParam = new ArrayList<String>();
//		cgiPath = "cgi-bin/inhome_cgi";
//		addParam("TYPE", "TEST");
//	}
//
//	public void addParam(String param, String value) {
//		requestParam.add(param + "=" + value);
//	}
//
//	public void bandwidth(String target) {
//		addParam("RUN", "TRIGGER_BANDWIDTH");
//		addParam("SERVER", target);
//		setType(TESTTYPE.BANDWIDTH);
//	}
//
//	@Override
//	public void execute() {
//		start();
//		HttpURLConnection conn = null;
//		URL triggerRequest = null;
//		String output = "";
//		try {
//			triggerRequest = new URL(getRequest());
//			conn = (HttpURLConnection) triggerRequest.openConnection();
//			conn.connect();
//			String line;
//			BufferedReader in = new BufferedReader(new InputStreamReader(
//					conn.getInputStream()));
//			while ((line = in.readLine()) != null) {
//				output += line + "\n";
//			}
//			in.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//			return;
//		}
//		setResult(output);
//		finish();
//	}
//
//	@Override
//	public String getOutputString() {
//		if (getType() == TESTTYPE.BANDWIDTH) {
//			StringBuilder sb = new StringBuilder();
//			Formatter formatter = new Formatter(sb, Locale.US);
//			double value = Double.valueOf(getResult().split(";")[7])
//					/ (1024 * 1024);
//			formatter.format("%.2f MiB/s", value);
//			return sb.toString();
//		} else if (getType() == TESTTYPE.LATENCY) {
//			StringBuilder sb = new StringBuilder();
//			Formatter formatter = new Formatter(sb, Locale.US);
//			double value = Double.valueOf(getResult().split(";")[5]) / 1000;
//			formatter.format("%.2f ms", value);
//			return sb.toString();
//		}
//		return "Unknown test type";
//	}
//	
//	@Override
//	public String getRequest() {
//		String ret = "http://";
//		ret += getServer();
//		if (getPort() != 80) {
//			ret += ":" + getPort();
//		}
//		ret += "/" + cgiPath + "?";
//		Iterator<String> it = requestParam.iterator();
//		while (it.hasNext()) {
//			ret += it.next();
//			if (it.hasNext()) {
//				ret += "&";
//			}
//		}
//		return ret;
//	}
//	private TESTTYPE getType() {
//		return testType;
//	}
//	public void latency(String target) {
//		addParam("SERVER", target);
//		addParam("RUN", "TRIGGER_LATENCY");
//		setType(TESTTYPE.LATENCY);
//	}
//	public void latency(String target, int port, int datagrams, int interval,
//			int delay, int percentile) {
//		addParam("SERVER", target);
//		addParam("RUN", "TRIGGER_LATENCY");
//		addParam("PORT", Integer.toString(port));
//		addParam("NUM_DATAGRAMS", Integer.toString(datagrams));
//		addParam("INTER_PACKET_TIME", Integer.toString(interval));
//		addParam("DELAY_TIMEOUT", Integer.toString(delay));
//		addParam("PERCENTILE", Integer.toString(percentile));
//		setType(TESTTYPE.LATENCY);
//	}
//	private void setType(TESTTYPE testType) {
//		this.testType = testType;
//	}
//
//	@Override
//	boolean setParam(String name, String value) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	boolean isReady() {
//		// TODO Auto-generated method stub
//		return false;
//	}

	@Override
	public boolean isSuccessful() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getHumanReadableResult() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isProgressAvailable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getProgress() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public HumanReadable getHumanReadable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStringID() {
		// TODO Auto-generated method stub
		return null;
	}
}
