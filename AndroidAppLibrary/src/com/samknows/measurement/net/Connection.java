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


package com.samknows.measurement.net;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;

public class Connection {
	private static final String TAG = Connection.class.getSimpleName();
	
	public static final String USER_AGENT = "SamKnows Android v1.0";
	public static final String BASE_URL = "http://txweb2.samknows.com/mobile.php/";
	public static final String ALLOWED_UNITS = BASE_URL + "user/getAllowedUnits";
	public static final String REPORTS = BASE_URL + "reports/getResults";
	
	public static final String DOWNSTREAM = "downstream_mt";
	public static final String UPSTREAM = "upstream_mt";
	public static final String LATENCY = "latency";
	public static final String PACKETLOSS = "packetloss";
	public static final String WWW_LOAD = "www_load";
	
	public static final String START_DATE = "start_date";
	public static final String END_DATE = "end_date";
	

	
	private String mUsername;
	private String mPassword;
	private String mUnit;
	private DefaultHttpClient mClient = new DefaultHttpClient();
	

	/** 
	 * Constructor 
	 * @param username
	 * @param password
	 */
	public Connection(String username, String password){
		mUsername = username;
		mPassword = password;
	}
	
	/**
	 * constructor
	 * @param username
	 * @param password
	 * @param unit
	 */
	public Connection(String username, String password, String unit){
		mUsername = username; 
		mPassword = password;
		mUnit = unit;
	}
	
	private String url(){
		return REPORTS + "?unit_id=" + mUnit + "&tests=downstream_mt,upstream_mt,latency,packetloss,www_load"; 
	}
	
	private String datesToString(Calendar start, Calendar end){
		CharSequence format = "yyyy-MM-dd";
		
		CharSequence startStr = DateFormat.format(format, start);
		CharSequence endStr = DateFormat.format(format, end);
		
		return "&start_date=" + startStr + "&end_date=" + endStr;
	}
	
	private String dates(int difference){
		Calendar end = Calendar.getInstance();
		Calendar start = (Calendar) end.clone();
		start.add(Calendar.DATE, - difference);
		return datesToString(start, end);
	}
	
	private String week(){
		return dates(7);		
	}
	
	private String month(){
		return dates(30);
	}
	
	private String three_months(){
		return dates(3 * 30);
	}
	
	private String six_months(){
		return dates(6 * 30);
	}
	
	private String year(){
		return dates(365);
	}
	
	public JSONObject getRecent() throws ConnectionException {
		 return get(url());
	}
	
	public JSONObject getWeek() throws ConnectionException {
		return get(url() + week());
	}
	
	public JSONObject getMonth() throws ConnectionException{
		return get(url() + month());
	}
	
	public JSONObject getThreeMonths() throws ConnectionException{
		return get(url() + three_months());
	}
	
	public JSONObject getSixMonths() throws ConnectionException{
		return get(url() + six_months());
	}
	
	public JSONObject getYear() throws ConnectionException {
		return get(url() + year());
	}
	
	/** Returns the devices
	 * 
	 * @return JSONObject
	 * @throws ConnectionException
	 */
	public JSONObject getDevices() throws ConnectionException {
		return get(ALLOWED_UNITS);
	}
	
	/**
	 * Login method
	 * 
	 * @return JSONObject
	 */	
	public JSONObject login() throws LoginException {
		HttpGet getMethod = getRequest(ALLOWED_UNITS);
		HttpResponse response;
		JSONObject result;
		int statusCode;
		try{
			response = doRequest(getMethod);
			statusCode = response.getStatusLine().getStatusCode();
		}catch (ConnectionException e){
			throw new LoginException("Could not connect to host.");
		}
		try{
			result = responseToJSON(response);
		}catch(JSONException e){
			throw new LoginException("Invalid JSON returned");
		}
		return result;		
	}
	
	private HttpGet getRequest(String url){
		HttpGet get = new HttpGet(url);
		get.setHeader("User-Agent", USER_AGENT);
		get.addHeader("Authorization", "Basic " + getCredentials());
		get.addHeader("Accept", "application/json");
		return get;
	}
	
	private HttpResponse doRequest(HttpRequestBase request) 
	throws ConnectionException, LoginException {
		HttpResponse response;
		try{
			Log.i(TAG, request.getURI().toString());
			response = mClient.execute(request);			
		}catch(ClientProtocolException e){
			throw new ConnectionException(e);
		}catch (IOException e){
			throw new ConnectionException(e);
		} finally {
			request.abort();
		}
		if (response.getStatusLine().getStatusCode() != 200){
			throw new LoginException("Could not login.");
		}
		return response;
	}
	
	private JSONObject responseToJSON(HttpResponse response) throws JSONException{
		String content = retrieveInputStream(response.getEntity());
		return new JSONObject(content);
	}
	
	/**
	 * Retrieve the input stream from the HTTP connection.
	 * 
	 * @param httpEntity
	 * @return String
	 */
	private String retrieveInputStream(HttpEntity httpEntity) {
		int length = (int) httpEntity.getContentLength();
		boolean chunked = (boolean) httpEntity.isChunked();
		
		Log.i(TAG, "Chunked: "+ Boolean.toString(chunked) + " - Length: " + Integer.toString(length));
		StringBuffer stringBuffer = new StringBuffer(length);
		try {
			InputStreamReader inputStreamReader = new InputStreamReader(httpEntity.getContent(), HTTP.UTF_8);
			char buffer[] = new char[length];
			int count;
			while ((count = inputStreamReader.read(buffer, 0, length - 1)) > 0) {
				stringBuffer.append(buffer, 0, count);
			}
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, e.toString());
		} catch (IllegalStateException e) {
			Log.e(TAG, e.toString());
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}
		return stringBuffer.toString();
	}
	
	/** Get the HTTP digest authentication. Uses Base64 to encode credentials.
	 * 
	 * @return String
	 */
	public String getCredentials(){
		return new String(Base64.encode((mUsername + ":" + mPassword).getBytes(), Base64.DEFAULT));
	}	
	
	/**
	 * Execute a GET request against the SamKnows REST API.
	 * 
	 * @param url
	 * @return JSONObject
	 */
	public JSONObject getData(HttpRequestBase request) throws ConnectionException {
		try{
			return responseToJSON(doRequest(request));
		}catch (Exception e){
			throw new ConnectionException(e);
		}
	}
	
	private JSONObject get(String url) throws ConnectionException{
		return getData(getRequest(url));
	}
}
