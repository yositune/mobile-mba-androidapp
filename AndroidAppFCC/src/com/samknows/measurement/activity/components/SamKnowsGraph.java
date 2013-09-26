package com.samknows.measurement.activity.components;

import org.json.JSONObject;

import com.samknows.measurement.util.OtherUtils;

import android.os.Build;
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
		Log.v(TAG, "setActiveButton()");
		button = btn;
		onSetActiveButton();
	}
	
	/** 
	 * Called when the currently active button is updated
	 */
	public void onSetActiveButton(){
		Log.v(TAG, "onSetActiveButton()");
		update();
	}
	
	/**
	 * Set the data to display
	 * 
	 * @param data JSONObject
	 */
	public void setData(JSONObject data){
		Log.v(TAG, "setData()");
		json = data.toString();
		onSetData();
	}
	
	public void setStartDate(String start_date){
		Log.v(TAG, "setStartDate()");
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
		Log.v(TAG, "onSetData()");
		update();
	}
	
	
	/** 
	 * Make a call to the javascript to update the graph
	 */
	public void update(){
		Log.v(TAG, "update()");
		
		switch (Build.VERSION.SDK_INT) {
		case Build.VERSION_CODES.GINGERBREAD:
		case Build.VERSION_CODES.GINGERBREAD_MR1:
			if (OtherUtils.isThisDeviceAnEmulator() == true) {
				// Avoid crash on 2.3.x (Gingerbread) Simulator!
				// Note that this does NOT display the graphs properly!
				graphs.reload();
				graphs.requestLayout();
				return;
			}
		}
		
		// Update the graphs!
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
		Log.v(TAG, "getData()");
		//Log.v(TAG, json);
		return json;
	}
	
	/**
	 * Return the date as a string
	 */
	public String getStartDate(){
		Log.v(TAG, "getStartDate()");
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

