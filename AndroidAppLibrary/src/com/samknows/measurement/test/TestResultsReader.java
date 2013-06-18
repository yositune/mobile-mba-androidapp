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


package com.samknows.measurement.test;

import java.io.File;

import android.content.Context;

import com.samknows.measurement.Constants;
import com.samknows.measurement.Logger;

public class TestResultsReader {
	private Context c;
	private String testType;
	private InnerReader r1, r2;
	
	public TestResultsReader(Context c, String testType) {
		super();
		this.testType = testType;
		this.c = c;
		File f1 = c.getFileStreamPath(Constants.TEST_RESULTS_TO_SUBMIT_FILE_NAME);
		File f2 = TestResultsManager.getSubmitedLogsFile(c);
		r1 = new InnerReader(f1);
		r2 = new InnerReader(f2);
	}
	
	public String read() {
		String result = r1.innerRead();
		if (result == null) {
			result = r2.innerRead();
		}
		return result;
	}

	
	private class InnerReader {
		private ReverseFileReader reader;
		private File f;
		
		private InnerReader(File f) {
			super();
			this.f = f;
		}

		private String innerRead() {
			String result = null;
			try {
				if (reader == null) {
					reader = new ReverseFileReader(f);
				}
				
				while ((result = reader.readLine()) != null) {
					if (result.split(Constants.RESULT_LINE_SEPARATOR)[0].startsWith(testType)) {
						break;
					}
				}
				
			} catch (Exception e) {
				Logger.e(this, "failed to parce test results "+e.toString());
			}
			return result;
		}
	}
}
