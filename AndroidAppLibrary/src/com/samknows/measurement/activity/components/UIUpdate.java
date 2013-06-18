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


package com.samknows.measurement.activity.components;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;

import com.samknows.measurement.Logger;
import com.samknows.measurement.statemachine.State;
import com.samknows.measurement.storage.TestResult;
import com.samknows.tests.ClosestTarget;
import com.samknows.tests.LatencyTest;

/*
 * Utility class that translates several events in JSONObject in order 
 * to update the UI  
 */
public class UIUpdate {
	//TYPE entry and possible values
	public static final String JSON_TYPE = "type";
	public static final String JSON_VALUE = "value";
	public static final String JSON_MAINPROGRESS = "mainprogress";
	public static final String JSON_ACTIVATED = "activated";
	public static final String JSON_DOWNLOADED = "downloaded";
	public static final String JSON_INITTESTS = "inittests";
	public static final String JSON_COMPLETED = "completed";
	
	//type inittest
	public static final String JSON_TOTAL = "total";
	public static final String JSON_FINISHED = "finished";
	public static final String JSON_CURRENTBEST = "currentbest";
	public static final String JSON_BESTTIME = "besttime";
	
	
	public UIUpdate(){}
	
	
	public static JSONObject stateFailure(){
		return completed();
	}
	
	public static JSONObject completed(){
		JSONObject ret = new JSONObject();
		try{
			ret.put(JSON_TYPE, JSON_COMPLETED);
		}catch(JSONException je){
			Logger.e(UIUpdate.class, "Error in createing JSONObject: "+ je.getMessage());
		}
		return ret;
	}
	
	
	
	//Generates the a JSONObject used to update the the SamKnowsActivating interface
	public static JSONObject machineState(State state){
		JSONObject ret = new JSONObject();
		String type = "";
		switch(state){
		case NONE:
		case INITIALISE:
		case ACTIVATE:
			break;
		case ASSOCIATE:
			type = JSON_ACTIVATED;
			break;
		case CHECK_CONFIG_VERSION:
		case DOWNLOAD_CONFIG:
			break;
		case RUN_INIT_TESTS:
			type = JSON_DOWNLOADED;
			break;
		case EXECUTE_QUEUE:
		case SUBMIT_RESULTS:
		case SHUTDOWN:
			break;
		}
		try{
			ret.put(JSON_TYPE, type);
		}catch(JSONException je){
			Logger.e(UIUpdate.class, "Error in creating JSONObject: " + je.getMessage());
		}
		return ret;
	}
	
	

	//Generates the a JSONObject used to update the progress bar in the SamKnowsActivating interface
	public static JSONObject progress(State state){
		JSONObject ret = new JSONObject();
		String type = "";
		String value = "";
		switch(state){
		case NONE:
			type = JSON_MAINPROGRESS;
			value = "10";
			break;
		case INITIALISE_ANONYMOUS:
		case INITIALISE:
			type = JSON_MAINPROGRESS;
			value = "20";
			break;
		case ACTIVATE:
			type = JSON_MAINPROGRESS;
			value = "30";
			break;
		case ASSOCIATE:
			type = JSON_MAINPROGRESS;
			value = "40";
			break;
		case CHECK_CONFIG_VERSION:
			type = JSON_MAINPROGRESS;
			value = "50";
			break;
		case DOWNLOAD_CONFIG_ANONYMOUS:
		case DOWNLOAD_CONFIG:
			type = JSON_MAINPROGRESS;
			value = "60";
			break;
		case RUN_INIT_TESTS:
			type = JSON_MAINPROGRESS;
			value = "70";
			break;
		case EXECUTE_QUEUE:
			type = JSON_COMPLETED;
		case SUBMIT_RESULTS:
		case SUBMIT_RESULTS_ANONYMOUS:
		case SHUTDOWN:
			break;
		}
		
		try{
			ret.put(JSON_TYPE, type);
			ret.put(JSON_VALUE, value);
		}catch(JSONException je){
			Logger.e(UIUpdate.class, "Error in creating JSONObject: "+ je.getMessage());
		}
		
		return ret;
	}
	
	public static JSONObject getClosestTargetPartialResult(ClosestTarget.Result res){
		if(res== null){
			return null;
		}
		JSONObject ret = new JSONObject();
		try{
			ret.put(JSON_TYPE, JSON_INITTESTS);
			ret.put(JSON_TOTAL, res.total);
			ret.put(JSON_FINISHED, res.completed);
			if(res.currbest_target.equals("")){
				ret.put(JSON_CURRENTBEST, "-");
				ret.put(JSON_BESTTIME, "-");
			}else{
				ret.put(JSON_CURRENTBEST, res.currbest_target);
				ret.put(JSON_BESTTIME, TestResult.timeToString(res.curr_best_time));
			}
		}catch(JSONException je){
			Logger.e(UIUpdate.class,"Error in creating JSONObject: "+ je.getMessage());
			ret = null;
		}
		return ret;
	}
	
	
}
