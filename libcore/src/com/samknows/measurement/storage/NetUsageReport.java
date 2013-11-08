package com.samknows.measurement.storage;

import com.samknows.measurement.environment.TrafficData;

import android.content.Context;

public class NetUsageReport {
	private Context mContext;
	DBHelper mDataBase;
	
	public NetUsageReport(Context context){
		mContext = context;
		mDataBase = new DBHelper(mContext);
	}
	
	public void save(TrafficData data){
		mDataBase.insertDataConsumption(data);
	}
	
	
	//return the summary of the the usage present in the database
	/*
	private JSONObject getSummary(){
		List<TrafficData> data = mDataBase.getTrafficData();
		
	}
	*/
	
}
