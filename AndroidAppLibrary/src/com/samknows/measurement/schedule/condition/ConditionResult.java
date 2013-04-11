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
