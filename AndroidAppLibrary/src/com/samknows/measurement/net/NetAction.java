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

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.util.Log;

import com.samknows.measurement.Constants;
import com.samknows.measurement.Logger;


public class NetAction {
	private String request, errorString;
	private List<Header> headers = new ArrayList<Header>();
	private HttpParams params = new BasicHttpParams();
	private String body;
	protected HttpResponse response;
	
	private boolean isPost = false;
	private boolean isSuccess = false;
	
	public void setPost(boolean isPost) {
		this.isPost = isPost;
	}
	
	public void execute() {
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, Constants.CONNECTION_TIMEOUT_MILLIS);
		HttpConnectionParams.setSoTimeout(httpParameters, Constants.CONNECTION_TIMEOUT_MILLIS);
		
		
		HttpClient httpclient = new DefaultHttpClient(httpParameters);
		HttpRequestBase mess = null;
		if (isPost) {
			mess = new HttpPost(request);
			if (body != null) {
				try {
					((HttpPost)mess).setEntity(new StringEntity(body));
				} catch (UnsupportedEncodingException e) {
					Logger.e(this, "error creating http message", e);
				}
			}
		} else {
			mess = new HttpGet(request);
		}
		mess.setParams(params);
		for (Header h : headers) {
			mess.addHeader(h);
		}
		
		try{
			Logger.d(this, "net request: " + request);
			response = httpclient.execute(mess);
		} catch (Exception e) {
			Logger.e(this, "failed to execute request: " + request, e);
		}
		
		if (isResponseOk()) {
			onActionFinished();
		} else if (response != null && response.getStatusLine() != null){
			isSuccess = false;
			Logger.e(this, "failed request, response code: " + response.getStatusLine().getStatusCode());
			try {
			InputStream content = response.getEntity().getContent();
			List<String> lines = IOUtils.readLines(content);
			errorString = "";
			for (String s : lines) {
				errorString += "\n" + s;
			}
			} catch (Exception e) {}
			Logger.e(this, errorString);
		}
	};
	
	public String getErrorString() {
		return errorString;
	}
	
	protected boolean isResponseOk() {
		return response != null && response.getStatusLine() != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
	}
	
	public boolean isSuccess() {
		return isSuccess;
	}

	protected void onActionFinished() {isSuccess = true;}
	
	public void addHeader(Map<String,String> _headers){
		for(String name: _headers.keySet()){
			addHeader(name, _headers.get(name));
		}
	}
	
	public void addHeader(String name, String value) {
		headers.add(new BasicHeader(name, value));
	}
	
	public void addParam(String name, String value) {
		params.setParameter(name, value);
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}
	
	public void setBodyString(String body) {
		this.body = body;
	}
}
