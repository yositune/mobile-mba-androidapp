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

import org.json.JSONObject;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Button;

public class SamKnowsGraph {
	private String TAG = SamKnowsGraph.class.getSimpleName();
	private WebView graphs;
	private String json;
	private Button button;
	private String date;
	private String tag="tag";
	private String date_format="%d-%m-%y";
	
	public SamKnowsGraph(WebView graphsView){
		graphs = graphsView;
	}
	
	/**
	 * Set the currently active button
	 * @param btn Button
	 */
	public void setActiveButton(Button btn){
		button = btn;
		onSetActiveButton();
	}
	
	/** 
	 * Called when the currently active button is updated
	 */
	public void onSetActiveButton(){
		update();
	}
	
	/**
	 * Set the data to display
	 * 
	 * @param data JSONObject
	 */
	public void setData(JSONObject data){
		json = data.toString();
		onSetData();
	}
	
	public void setStartDate(String start_date){
		date = start_date;
	}
	
	public void setDateFormat(String format){
		this.date_format=format;
	}
	
	@JavascriptInterface
	public String getDateFormat(){
		return this.date_format;
	}
	
	/**
	 * Called when the data is updated
	 */
	public void onSetData(){
		update();
	}
	
	
	/** 
	 * Make a call to the javascript to update the graph
	 */
	public void update(){
		graphs.loadUrl("javascript:update();");
	}
	
	/** 
	 * Return the tag of the button which is also the key
	 * in the JSON data structure
	 * 
	 * @return String
	 */
	@JavascriptInterface
	public String getTag(){
		return this.tag;
	}
	
	public void setTag(String tagtoset){
		this.tag=tagtoset;
	}
	
	/**
	 * Return the data in a json string
	 * @return
	 */
	@JavascriptInterface
	public String getData(){
		return json;
	}
	
	/**
	 * Return the date as a string
	 */
	public String getStartDate(){
		return (String) date;
	}
		
	/** 
	 * Logging interface for the javascript
	 * @param log String
	 */
	@JavascriptInterface
	public void log(String log){
		Log.v(TAG, "log(): " + log);
	}
}

