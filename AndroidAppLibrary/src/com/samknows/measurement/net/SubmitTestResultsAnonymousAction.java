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

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.samknows.measurement.AppSettings;
import com.samknows.measurement.Logger;
import com.samknows.measurement.storage.ResultsContainer;
import com.samknows.measurement.test.TestResultsManager;

import android.content.Context;
import android.net.Uri;

public class SubmitTestResultsAnonymousAction {
	protected Context context;
	protected boolean isSuccess = false;

	public SubmitTestResultsAnonymousAction(Context _context) {
		context = _context;
	}

	public void execute() {
		String[] results = TestResultsManager.getJSONData(context);
		List<Integer> fail = new ArrayList<Integer>();
		for (int i=0; i<results.length; i++) {
			byte[] data = results[i].getBytes();
			if (data == null) {
				Logger.d(SubmitTestResultsAnonymousAction.class,
						"no results to be submitted");
				break;
			}
			HttpContext httpContext = new BasicHttpContext();
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(buildUrl());
			httpPost.setEntity(new ByteArrayEntity(data));
			httpContext.setAttribute("Content-Length", data.length);
			try {
				HttpResponse httpResponse = httpClient.execute(httpPost);
				StatusLine sl = httpResponse.getStatusLine();
				isSuccess = sl.getStatusCode() == HttpStatus.SC_OK
						&& sl.getReasonPhrase().equals("OK");
				int code = sl.getStatusCode();
				Logger.d(this, "submiting test results to server: " + isSuccess);
			} catch (Exception e) {
				Logger.e(this, "failed to submit results to server", e);
				isSuccess = false;
			}

			if (!isSuccess) {
				fail.add(i);
				TestResultsManager.clearResults(context);
				TestResultsManager.saveSumbitedLogs(context, data);
			}
		}
		TestResultsManager.clearResults(context);
		for(int i:fail){
			TestResultsManager.saveResult(context, results[i]);
		}
	}

	public String buildUrl() {
		AppSettings settings = AppSettings.getInstance();
		return new Uri.Builder().scheme(settings.protocol_scheme)
				.authority(settings.getServerBaseUrl())
				.path(settings.submit_path).build().toString();
	}
}
