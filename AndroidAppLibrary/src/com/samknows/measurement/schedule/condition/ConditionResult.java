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


package com.samknows.measurement.schedule.condition;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.samknows.measurement.Constants;
import com.samknows.measurement.Logger;
import com.samknows.measurement.util.DCSStringBuilder;

public class ConditionResult {
	public boolean isSuccess;
	public String outString;
	public static final String JSON_TYPE = "type";
	public static final String JSON_TIMESTAMP = "timestamp";
	public static final String JSON_DATETIME = "datetime";
	public static final String JSON_SUCCESS = "success";
	private String[] json_fields = null;
	public JSONObject outJSON;
	private boolean failQuiet = false;
	
	public ConditionResult(boolean isSuccess) {
		super();
		this.isSuccess = isSuccess;
	}
	
	public ConditionResult(boolean isSuccess, String outString) {
		super();
		this.isSuccess = isSuccess;
		this.outString = outString;
	}

	public void generateOut(String id, String... data) {
		DCSStringBuilder b = new DCSStringBuilder();
		b.append(id);
		long time = System.currentTimeMillis();
		b.append(time);
		if (isSuccess) {
			b.append(Constants.RESULT_OK);
		} else {
			b.append(Constants.RESULT_FAIL);
		}
		
		for (String s : data) {
			if (s != null) {
				b.append(s);
			}
		}
		
		outString = b.build();
		
		outJSON = new JSONObject();
		try{
			
			outJSON.put(JSON_TYPE, id);
			outJSON.put(JSON_TIMESTAMP, time/1000+"");
			outJSON.put(JSON_DATETIME, new java.util.Date(time).toString());
			outJSON.put(JSON_SUCCESS, Boolean.toString(isSuccess));
			if(json_fields != null && json_fields.length == data.length){
				for(int i =0; i<json_fields.length ; i++){
					outJSON.put(json_fields[i], data[i]);
				}
			}
		}catch(JSONException je){
			Logger.e(this, "error in updating JSONObject: " +je.getMessage() );
		}
		
	}


	public boolean isFailQuiet() {
		return failQuiet;
	}

	public void setFailQuiet(boolean failQuiet) {
		this.failQuiet = failQuiet;
	}
	
	public void setJSONFields(String... fields){
		json_fields= fields ;
	}
}
