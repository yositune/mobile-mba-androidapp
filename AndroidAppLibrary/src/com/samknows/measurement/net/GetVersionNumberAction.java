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
import java.util.List;

import org.apache.commons.io.IOUtils;

import android.content.Context;
import android.net.Uri;

import com.samknows.measurement.AppSettings;
import com.samknows.measurement.Logger;

public class GetVersionNumberAction extends NetAction{
	public InputStream content;
	public String version, path;
	public GetVersionNumberAction(Context c, String currentVersion) {
		super();
		String request = new Uri.Builder().scheme("https")
				.authority(AppSettings.getInstance().getServerBaseUrl())
				.path("/mobile/version")
				.appendQueryParameter("current", currentVersion).build().toString();

		setRequest(request);

		addHeader("X-Encryption-Desired", "false");
		addHeader("X-Unit-ID", AppSettings.getInstance().getUnitId());
	}

	@Override
	public boolean isSuccess() {
		return version != null && path != null;
	}

	@Override
	protected void onActionFinished() {
		try {
			content = response.getEntity().getContent();
			List<String> lines = IOUtils.readLines(content);
			version = lines.get(0);
			path = lines.get(1);
		} catch (Exception e) {
			Logger.e(this, "failed to parse response", e);
		}
	}
}
