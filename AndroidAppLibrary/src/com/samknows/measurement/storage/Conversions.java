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


package com.samknows.measurement.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.samknows.measurement.Constants;
import com.samknows.measurement.Logger;

public class Conversions {
	//test id to test string conversion
	public static final int UPLOAD_TEST_ID = 0;
	public static final int DOWNLOAD_TEST_ID = 1;
	public static final int LATENCY_TEST_ID = 2;
	public static final int PACKETLOSS_TEST_ID = 3;
	public static final int JITTER_TEST_ID = 4;
	public static final String UPLOAD_TEST_STRING = "upload";
	public static final String DOWNLOAD_TEST_STRING = "download";
	public static final String LATENCY_TEST_STRING = "latency";
	public static final String PACKETLOSS_TEST_STRING = "packet loss";
	public static final String JITTER_TEST_STRING = "jitter";
	
	public static final String DOWNSTREAMTHROUGHPUT = "JHTTPGET"; 
	public static final String UPSTREAMTHROUGHPUT = "JHTTPPOST";
	public static final String LATENCY = "JUDPLATENCY";
	public static final String JITTER = "JUDPJITTER";
	
	
	
	public static String testIdToString(int test_id){
		switch(test_id){
		case UPLOAD_TEST_ID: return UPLOAD_TEST_STRING;
		case DOWNLOAD_TEST_ID: return DOWNLOAD_TEST_STRING;
		case LATENCY_TEST_ID: return LATENCY_TEST_STRING;
		case PACKETLOSS_TEST_ID: return PACKETLOSS_TEST_STRING;
		case JITTER_TEST_ID: return JITTER_TEST_STRING;
		}
		return "";
	}
	
	public static int testStringToId(String testString){
		int ret = -1;
		if(UPLOAD_TEST_STRING.equals(testString)){
			ret = UPLOAD_TEST_ID;
		}else if(DOWNLOAD_TEST_STRING.equals(testString)){
			ret = DOWNLOAD_TEST_ID;
		}else if(LATENCY_TEST_STRING.equals(testString)){
			ret = LATENCY_TEST_ID;
		}else if(PACKETLOSS_TEST_STRING.equals(testString)){
			ret = PACKETLOSS_TEST_ID;
		}else if(JITTER_TEST_STRING.equals(testString)){
			ret = JITTER_TEST_ID;
		}
		return ret;
	}
	
	public static String testMetricToString(int test_id, double value){
		String ret = "";
		switch(test_id){
		case UPLOAD_TEST_ID:
		case DOWNLOAD_TEST_ID:
			ret = throughputToString(value);
			break;
		case LATENCY_TEST_ID:
		case JITTER_TEST_ID:
			ret = timeToString(value);
			break;
		case PACKETLOSS_TEST_ID:
			ret = String.format("%.2f %", value);
			break;
		}
		return ret;
	}
	
	private static String throughputToString(double value){
		String ret = "";
		if(value < 1000){
			ret = String.format("%.0f bps", value);
		}else if(value < 1000000 ){
			ret = String.format("%.2f Kbps", (double)(value/1000.0));
		}else{
			ret = String.format("%.2f Mbps", (double)(value/1000000.0));
		}
		return ret;
	}
	
	private static String timeToString(double value){
		String ret = "";
		if(value < 1000){
			ret = String.format("%.0f microseconds", value); 
		}else if(value < 1000000 ){
			ret = String.format("%.0f ms", value);
		}else {
			ret = String.format("%.2f s", value);
		}
		return ret;
	}
	
	
	//Method for converting a testoutput string in a JSNObject suitable for the database
	public static List<JSONObject> testToJSON(String data){
		return testToJSON(data.split(Constants.RESULT_LINE_SEPARATOR));
	}
	
	public static List<JSONObject> testToJSON(String[] data){
		List<JSONObject> ret = new ArrayList<JSONObject>();
		String test_id = data[0];
		if(test_id.startsWith(DOWNSTREAMTHROUGHPUT)){
			ret.add(convertThroughputTest(DOWNLOAD_TEST_STRING, data));
		}else if(test_id.startsWith(UPSTREAMTHROUGHPUT)){
			ret.add(convertThroughputTest(DOWNLOAD_TEST_STRING, data));
		}else if(test_id.startsWith(LATENCY)){
			
		}
		
		return ret;
	}
	
	private static JSONObject convertThroughputTest(String test, String[] data){
		JSONObject ret = new JSONObject();
		
		return ret;
	}
	
	private static void put(JSONObject obj, String key, String value){
			try{
				obj.put(key, value);
			}catch(JSONException je){
				Logger.e(TestResult.class, "JSONException "+ key +" "+ value);
			}
	}
	
	
}
