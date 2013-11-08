package com.samknows.measurement.util;

import java.text.SimpleDateFormat;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.DateUtils;


public class SKDateFormat  {
	private Context mCtx;
	
	public SKDateFormat(Context ctx){
		mCtx = ctx;
	}
	
	public static String sGetGraphDateFormat(Context context){
		char[] order = DateFormat.getDateFormatOrder(context);
		StringBuilder sb = new StringBuilder();
		for(int i =0; i< order.length; i++){
			switch(order[i]){
			case DateFormat.DATE:
				if(i!=0){
					sb.append("/");
				}
				sb.append("dd");
				break;
			case DateFormat.MONTH:
				if(i!=0){
					sb.append("/");
				}
				sb.append("MM");
				break;
			case DateFormat.YEAR:
				//sb.append("yyyy");
				break;
			}
		}
		return sb.toString();
	}
	
	
	private String dateFormat(){
		char[] order = DateFormat.getDateFormatOrder(mCtx);
		StringBuilder sb = new StringBuilder();
		for(int i =0; i< order.length; i++){
			if(i!=0){
				sb.append("/");
			}
			switch(order[i]){
			case DateFormat.DATE:
				sb.append("dd");
				break;
			case DateFormat.MONTH:
				sb.append("MM");
				break;
			case DateFormat.YEAR:
				sb.append("yyyy");
				break;
			}
		}
		return sb.toString();
		
	}
	
	public String UIDate(long millis){
		return new SimpleDateFormat(dateFormat()).format(millis);
	}
	
	public String UITime(long millis){
		return UIDate(millis)+" "+DateUtils.formatDateTime(mCtx, millis, DateUtils.FORMAT_SHOW_TIME);
	}
	
	public String getJSDateFormat(){
		char[] order = DateFormat.getDateFormatOrder(mCtx);
		StringBuilder sb = new StringBuilder();
		for(int i =0; i< order.length; i++){
			if(i!=0){
				sb.append("/");
			}
			switch(order[i]){
			case DateFormat.DATE:
				sb.append("%d");
				break;
			case DateFormat.MONTH:
				sb.append("%m");
				break;
			case DateFormat.YEAR:
				sb.append("%y");
				break;
			}
			
		}
		return sb.toString();
	}
		
}


