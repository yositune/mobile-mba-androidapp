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

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.net.Uri;

import com.samknows.measurement.AppSettings;
import com.samknows.measurement.Logger;
import com.samknows.measurement.environment.PhoneIdentityDataCollector;
import com.samknows.measurement.test.TestResultsManager;
import com.samknows.measurement.util.DCSStringBuilder;

public class SubmitTestResultsAction extends SubmitTestResultsAnonymousAction{
	private String imei;
	
	public SubmitTestResultsAction(Context context) {
		super(context);
		this.imei = PhoneIdentityDataCollector.getImei(context);
	}

	public void execute() {
		byte[] data = TestResultsManager.getResult(context);
		int code = -1;
		if (data != null) {
			HttpContext httpContext = new BasicHttpContext();
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(buildUrl());
			httpPost.addHeader("X-Unit-ID", AppSettings.getInstance().getUnitId());
			httpPost.addHeader("X-Encrypted", "false");
			httpPost.addHeader("X-IMEI", imei);
			httpPost.setEntity(new ByteArrayEntity(data));
			httpContext.setAttribute("Content-Length", data.length);
			try {
				HttpResponse httpResponse = httpClient.execute(httpPost);
				StatusLine sl = httpResponse.getStatusLine();
				isSuccess = sl.getStatusCode() == HttpStatus.SC_OK && sl.getReasonPhrase().equals("OK");
				code = sl.getStatusCode();
				Logger.d(this, "submiting test results to server: " + isSuccess);
			} catch(Exception e){
				Logger.e(this, "failed to submit results to server", e);
			}
			
			if (isSuccess) {
				TestResultsManager.clearResults(context);
				TestResultsManager.saveSumbitedLogs(context, data);
			} else {
				//TestResultsManager.saveResult(context, getFailedString(code));
			}
		}
	}
	
	public String getFailedString(int code) {
		return new DCSStringBuilder().append("SUBMITRESULT").append(System.currentTimeMillis()/1000).append("FAIL").append(code).build();
	}
	
}
