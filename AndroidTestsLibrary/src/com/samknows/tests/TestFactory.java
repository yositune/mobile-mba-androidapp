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

import com.samknows.tests.Param;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;

public class TestFactory {

	public static final String DOWNSTREAMTHROUGHPUT = "downstreamthroughput";
	public static final String UPSTREAMTHROUGHPUT = "upstreamthroughput";
	public static final String LATENCY = "latency";
	public static final String PROXYDETECTOR = "proxydetector";
	public static final String CLOSESTTARGET = "closesttarget";
	/*
	 * constants shared among different tests
	 */
	private static final String TESTTYPE = "testType";
	private static final String TARGET = "target";
	private static final String PORT = "port";
	private static final String FILE = "file";

	/*
	 * constants for creating a http test
	 */
	private static final String DOWNSTREAM = "downStream";
	private static final String UPSTREAM = "upStream";

	private static final String WARMUPMAXTIME = "warmupMaxTime";
	private static final String WARMUPMAXBYTES = "warmupMaxBytes";
	private static final String TRANSFERMAXTIME = "transferMaxTime";
	private static final String TRANSFERMAXBYTES = "transferMaxBytes";
	private static final String NTHREADS = "numberOfThreads";
	private static final String BUFFERSIZE = "bufferSize";
	private static final String SENDBUFFERSIZE = "sendBufferSize";
	private static final String RECEIVEBUFFERSIZE = "receiveBufferSize";
	private static final String POSTDATALENGTH = "postDataLength";
	private static final String SENDDATACHUNK = "sendDataChunk";

	public static final String[] HTTPTESTPARAMLIST = { TESTTYPE, TARGET, PORT,
			FILE, WARMUPMAXTIME, WARMUPMAXBYTES, TRANSFERMAXTIME,
			TRANSFERMAXBYTES, NTHREADS, BUFFERSIZE, SENDBUFFERSIZE,
			RECEIVEBUFFERSIZE, POSTDATALENGTH, SENDDATACHUNK };

	/*
	 * constants for creating a latency test
	 */
	private static final String NUMBEROFPACKETS = "numberOfPackets";
	private static final String DELAYTIMEOUT = "delayTimeout";
	private static final String INTERPACKETTIME = "interPacketTime";
	private static final String PERCENTILE = "percentile";
	private static final String MAXTIME = "maxTime";

	public static final String[] LATENCYTESTPARAMLIST = { TESTTYPE, TARGET,
			PORT, NUMBEROFPACKETS, DELAYTIMEOUT, INTERPACKETTIME, PERCENTILE,
			MAXTIME };

	public static final String[] CLOSESTTARGETPARAMLIST = LATENCYTESTPARAMLIST;

	public static Test create(List<Param> params) {
		Param testType = null;
		for (Param p : params) {
			if (Test.paramMatch(p.getName(), TESTTYPE)) {
				testType = p;
			}
		}
		if (testType == null) {
			return null;
		}
		params.remove(testType);
		return create(testType.getValue(), params);
	}

	public static Test create(String testType, List<Param> params) {
		Test ret = null;
		if (Test.paramMatch(testType, DOWNSTREAMTHROUGHPUT)) {
			ret = createHttpTest(DOWNSTREAM, params);
		} else if (Test.paramMatch(testType, UPSTREAMTHROUGHPUT)) {
			ret = createHttpTest(UPSTREAM, params);
		} else if (Test.paramMatch(testType, LATENCY)) {
			ret = createLatencyTest(params);
		} else if (Test.paramMatch(testType, CLOSESTTARGET)) {
			ret = createClosestTarget(params);
		} else if (Test.paramMatch(testType, PROXYDETECTOR)) {
			ret = createProxyDetector(params);
		}
		if (ret != null && !ret.isReady()) {
			ret = null;
		}
		return ret;
	}

	public static ClosestTarget createClosestTarget(List<Param> params) {
		ClosestTarget ret = new ClosestTarget();
		try {
			for (Param curr : params) {
				String param = curr.getName();
				String value = curr.getValue();
				if (Test.paramMatch(param, TARGET)) {
					ret.addTarget(value);
				} else if (Test.paramMatch(param, PORT)) {
					ret.setPort(Integer.parseInt(value));
				} else if (Test.paramMatch(param, NUMBEROFPACKETS)) {
					ret.setNumberOfDatagrams(Integer.parseInt(value));
				} else if (Test.paramMatch(param, DELAYTIMEOUT)) {
					ret.setDelayTimeout(Integer.parseInt(value));
				} else if (Test.paramMatch(param, INTERPACKETTIME)) {
					ret.setInterPacketTime(Integer.parseInt(value));
				} else {
					ret = null;
					break;
				}
			}
		} catch (NumberFormatException nfe) {
			ret = null;
		}
		return ret;
	}

	private static ProxyDetector createProxyDetector(List<Param> params) {
		ProxyDetector ret = new ProxyDetector();
		try {
			for (Param curr : params) {
				String param = curr.getName();
				String value = curr.getValue();
				if (Test.paramMatch(param, TARGET)) {
					ret.setTarget(value);
				} else if (Test.paramMatch(param, PORT)) {
					ret.setPort(Integer.parseInt(value));
				} else if (Test.paramMatch(param, FILE)) {
					ret.setFile(value);
				} else {
					ret = null;
					break;
				}
			}
		} catch (NumberFormatException nfe) {
			ret = null;
		}
		return ret;
	}

	private static LatencyTest createLatencyTest(List<Param> params) {
		LatencyTest ret = new LatencyTest();

		try {
			for (Param curr : params) {
				String param = curr.getName();
				String value = curr.getValue();
				if (Test.paramMatch(param, TARGET)) {
					ret.setTarget(value);
				} else if (Test.paramMatch(param, PORT)) {
					ret.setPort(Integer.parseInt(value));
				} else if (Test.paramMatch(param, NUMBEROFPACKETS)) {
					ret.setNumberOfDatagrams(Integer.parseInt(value));
				} else if (Test.paramMatch(param, DELAYTIMEOUT)) {
					ret.setDelayTimeout(Integer.parseInt(value));
				} else if (Test.paramMatch(param, INTERPACKETTIME)) {
					ret.setInterPacketTime(Integer.parseInt(value));
				} else if (Test.paramMatch(param, PERCENTILE)) {
					ret.setPercentile(Integer.parseInt(value));
				} else if (Test.paramMatch(param, MAXTIME)) {
					ret.setMaxExecutionTime(Long.parseLong(value));
				} else {
					ret = null;
					break;
				}
			}
		} catch (NumberFormatException nfe) {
			ret = null;
		}
		return ret;
	}

	private static HttpTest createHttpTest(String direction, List<Param> params) {
		HttpTest ret = new HttpTest(direction);
		try {
			for (Param curr : params) {
				String param = curr.getName();
				String value = curr.getValue();
				if (Test.paramMatch(param, TARGET)) {
					ret.setTarget(value);
				} else if (Test.paramMatch(param, PORT)) {
					ret.setPort(Integer.parseInt(value));
				} else if (Test.paramMatch(param, FILE)) {
					ret.setFile(value);
				} else if (Test.paramMatch(param, WARMUPMAXTIME)) {
					ret.setWarmupMaxTime(Integer.parseInt(value));
				} else if (Test.paramMatch(param, WARMUPMAXBYTES)) {
					ret.setWarmupMaxBytes(Integer.parseInt(value));
				} else if (Test.paramMatch(param, TRANSFERMAXTIME)) {
					ret.setTransferMaxTime(Integer.parseInt(value));
				} else if (Test.paramMatch(param, TRANSFERMAXBYTES)) {
					ret.setTransferMaxBytes(Integer.parseInt(value));
				} else if (Test.paramMatch(param, NTHREADS)) {
					ret.setNumberOfThreads(Integer.parseInt(value));
				} else if (Test.paramMatch(param, BUFFERSIZE)) {
					ret.setBufferSize(Integer.parseInt(value));
				} else if (Test.paramMatch(param, SENDBUFFERSIZE)) {
					ret.setSendBufferSize(Integer.parseInt(value));
				} else if (Test.paramMatch(param, RECEIVEBUFFERSIZE)) {
					ret.setReceiveBufferSize(Integer.parseInt(value));
				} else if (Test.paramMatch(param, SENDDATACHUNK)) {
					ret.setSendDataChunk(Integer.parseInt(value));
				} else if (Test.paramMatch(param, POSTDATALENGTH)) {
					ret.setPostDataLenght(Integer.parseInt(value));
				} else {
					ret = null;
					break;
				}

			}
		} catch (NumberFormatException nfe) {
			ret = null;
		}
		return ret;
	}

	public static final ArrayList<Param> testConfiguration(List<Param> allParam) {
		ArrayList<Param> ret = null;
		Param testType = null;
		for (Param p : allParam) {
			if (p.getName().equals("testid")) {
				testType = p;
			}
		}
		if (testType != null) {
			allParam.remove(testType);
			ret = testConfiguration(allParam, testType.getValue());
		}
		return ret;
	}

	public static final ArrayList<Param> testConfiguration(
			List<Param> allParam, String testType) {
		ArrayList<Param> ret = new ArrayList<Param>();
		if (testType.equalsIgnoreCase(DOWNSTREAMTHROUGHPUT)) {
			ret = testConfiguration(allParam, HTTPTESTPARAMLIST);
		} else if (testType.equalsIgnoreCase(LATENCY)) {
			ret = testConfiguration(allParam, LATENCYTESTPARAMLIST);
		} else {
			ret = null;
		}
		return ret;
	}

	public static final ArrayList<Param> testConfiguration(
			List<Param> allParam, String[] configKey) {
		HashSet<String> toInclude = new HashSet<String>();
		ArrayList<Param> ret = new ArrayList<Param>();
		for (String k : configKey) {
			toInclude.add(k);
		}
		for (Param curr : allParam) {
			if (toInclude.contains(curr.getName())) {
				ret.add(curr);
			}
		}
		return ret;
	}
	
	public static final long getMaxUsage(String type, List<Param> params){
		long ret = 0;
		if(type.equalsIgnoreCase(DOWNSTREAMTHROUGHPUT)|| type.equalsIgnoreCase(UPSTREAMTHROUGHPUT)){
			ret = getMaxUsageHttp(params);
		}else if(type.equalsIgnoreCase(LATENCY)){
			ret = getMaxUsageLatency(params);
		}
		return ret;
	}
	
	private static long getMaxUsageHttp(List<Param> params){
		long ret = 0;
		for(Param p:params){
			if(p.isName(WARMUPMAXBYTES) || p.isName(TRANSFERMAXBYTES )){
				ret += Long.parseLong(p.getValue());
			}
		}
		return ret;
	}
	
	private static long getMaxUsageLatency(List<Param> params){
		long ret = 0;
		for(Param p: params){
			if(p.isName(NUMBEROFPACKETS)){
				ret = Long.parseLong(p.getValue()) * LatencyTest.getPacketSize(); 
			}
		}
		return ret;
	}
	
}
