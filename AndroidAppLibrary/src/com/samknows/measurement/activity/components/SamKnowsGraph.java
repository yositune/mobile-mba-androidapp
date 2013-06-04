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

