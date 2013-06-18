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

import java.util.Calendar;

import org.apache.http.client.HttpClient;
import org.apache.http.params.HttpParams;

import android.text.format.DateFormat;
import android.util.Base64;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.samknows.measurement.AppSettings;

public class SamKnowsClient{
	private static final String TAG = Connection.class.getSimpleName();
	
	private String username;
	private String password;
	private String device;
	
	private AsyncHttpClient client = new AsyncHttpClient();
	private HttpClient httpClient = client.getHttpClient();
	private HttpParams httpParams = httpClient.getParams();
	
	public final String ALLOWED_UNITS = AppSettings.getInstance().reportingServerPath + "user/getAllowedUnits";
	public final String REPORTS = AppSettings.getInstance().reportingServerPath + "reports/getResults";
	
	public SamKnowsClient(String _username, String _password){
		username = _username;
		password = _password;
		setParams();
		
	}
	
	public SamKnowsClient(String _username, String _password, String _device){
		username = _username;
		password = _password;
		device = _device;
		setParams();
	}
	
	private void setParams(){
		client.addHeader("Authorization", "Basic " + getCredentials());
	}
	
	private String getCredentials(){
		return Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP);
	}
	
	public void getDevices(AsyncHttpResponseHandler responseHandler){
		client.get(ALLOWED_UNITS, responseHandler);
	}
	
	public void getRecent(AsyncHttpResponseHandler responseHandler){
		client.get(recent(), responseHandler);
	}
	
	public void getWeek(AsyncHttpResponseHandler responseHandler){
		client.get(week(), responseHandler);
	}
	
	public void getMonth(AsyncHttpResponseHandler responseHandler){
		client.get(month(), responseHandler);
	}
	
	public void getThreeMonths(AsyncHttpResponseHandler responseHandler){
		client.get(three_months(), responseHandler);
	}
	
	public void getSixMonths(AsyncHttpResponseHandler responseHandler){
		client.get(six_months(), responseHandler);
	}
	
	public void getYear(AsyncHttpResponseHandler responseHandler){
		client.get(year(), responseHandler);
	}
	
	
	// URL Assemblage
	private String url(){
		return REPORTS + "?unit_id=" + device + "&tests=downstream_mt,upstream_mt,latency,packetloss,voip_jitter"; 
	}
	
	public String dateToString(Calendar date){
		CharSequence format = "yyyy-MM-dd";
		CharSequence dateStr = DateFormat.format(format, date);
		return "" + dateStr;
	}
	
	private String datesToString(Calendar start, Calendar end){
		return "&start_date=" + dateToString(start) + "&end_date=" + dateToString(end);
	}
	
	public Calendar getStartDate(int difference){
		Calendar end = Calendar.getInstance();
		Calendar start = (Calendar) end.clone();
		start.add(Calendar.DATE, - difference);
		return start;
	}
	
	public String dates(int difference){		
		return datesToString(getStartDate(difference), Calendar.getInstance());
	}
	
	private String recent(){
		return url();
	}
	
	private String week(){
		return url() + dates(7);		
	}
	
	private String month(){
		return url() + dates(30);
	}
	
	private String three_months(){
		return url() + dates(3 * 30);
	}
	
	private String six_months(){
		return url() + dates(6 * 30);
	}
	
	private String year(){
		return url() + dates(365);
	}

	public String getDevice() {
		return device;
	}
	
	
}