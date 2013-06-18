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


package com.samknows.measurement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.samknows.measurement.util.TimeUtils;

import android.util.Log;

public class Logger {
	private static File folder;
	private static final String ERROR = "Error";
	private static final String WARNING = "Warning";
	private static final String DEBUG = "Debug";
	
	public static void setStorageFolder(File f) {
		folder = f;
	}

	public static void d(String tag, String message) {
		Log.d(tag, message);
		appendLog(DEBUG, tag, message);
	}
	
	public static void d(Object parent, String message) {
		Log.d(parent.getClass().getName(), message);
		appendLog(DEBUG,parent.getClass().getName(), message);
	}

	public static void d(Class clazz, String message) {
		Log.d(clazz.getName(), message);
		appendLog(DEBUG,clazz.getName(), message);
	}
	
	public static void e(String tag, String message) {
		Log.d(tag, message);
		appendLog(ERROR, tag, message);
	}
	
	public static void e(Class clazz, String message) {
		Log.d(clazz.getName(), message);
		appendLog(ERROR, clazz.getName(), message);
	}

	public static void e(Object parent, String message, Throwable t) {
		Log.e(parent.getClass().getName(), message, t);
		appendLog(ERROR,parent.getClass().getName(), message+" "+t.getMessage()+" "+getStackTrace(t));
	}

	public static void e(Object parent, String message) {
		Log.e(parent.getClass().getName(), message);
		appendLog(ERROR,parent.getClass().getName(), message);
	}

	public static void w(Class clazz, String message) {
		Log.w(clazz.getName(), message);
		appendLog(WARNING, clazz.getName(), message);
	}

	private static void appendLog(String severety, String tag, String text) {
		if (Constants.LOG_TO_FILE) {
			File logFile = new File(folder, "log.file");
			if (!logFile.exists()) {
				try {
					logFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				// BufferedWriter for performance, true to set append to file
				// flag
				BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,
						true));
				buf.append(TimeUtils.logString(System.currentTimeMillis()) + " : ");
				buf.append(severety +" : ");
				buf.append(tag + " : ");
				buf.append(text);
				buf.newLine();
				buf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static String getStackTrace(Throwable t){
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString();
	}

}
