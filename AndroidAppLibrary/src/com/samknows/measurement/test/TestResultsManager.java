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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;

import android.content.Context;

import com.samknows.measurement.Constants;
import com.samknows.measurement.Logger;
import com.samknows.measurement.storage.ResultsContainer;

public class TestResultsManager {
	
	private static File storage;
	
	public static void setStorage(File storage) {
		TestResultsManager.storage = storage;
	}

	public static void saveResult(Context c, ResultsContainer rc){
		saveResult(c, rc.getJSON().toString());
		
	}
	
	public static void saveResult(Context c, List<String> results) {
		//if there is nothing to save returns immediately
		if(results.size() == 0){
			return;
		}
		DataOutputStream dos = openOutputFile(c);
		if( dos == null){
			Logger.e(TestResultsManager.class, "Impossible to save results");
			return;
		}
		try {
			for (String outRes : results) {
				dos.writeBytes(outRes);
				dos.writeBytes("\r\n");
			}
		} catch (IOException ioe) {
			Logger.e(TestResultsManager.class, "Error while saving results: " + ioe.getMessage());
		} finally {
			IOUtils.closeQuietly(dos);
		}
	}
	
	//Tries to open output file, in case of failures returns null
	private static DataOutputStream openOutputFile(Context c){
		DataOutputStream ret = null;
		try{
			FileOutputStream os = c.openFileOutput(Constants.TEST_RESULTS_TO_SUBMIT_FILE_NAME, Context.MODE_APPEND);
			ret = new DataOutputStream(os);
		}catch(FileNotFoundException fnfe){
			Logger.e(TestResultsManager.class, Constants.TEST_RESULTS_TO_SUBMIT_FILE_NAME +" not found!");
			ret = null;
		}
		return ret;

	}
	
	public static void saveResult(Context c, String result) {
		DataOutputStream dos = openOutputFile(c);
		if( dos == null){
			Logger.e(TestResultsManager.class, "Impossible to save results");
			return;
		}
		try {
			dos.writeBytes(result);
			dos.writeBytes("\r\n");
		} catch (IOException ioe) {
			Logger.e(TestResultsManager.class, "Error while saving results: " + ioe.getMessage());
		} finally {
			IOUtils.closeQuietly(dos);
		}
	}

	public static void saveResult(Context c, TestResult[] result) {
		for (TestResult r : result) saveResult(c, r.results);
	}
	
	public static byte[] getResult(Context c) {
		InputStream is = null;
		try {
			is = c.openFileInput(Constants.TEST_RESULTS_TO_SUBMIT_FILE_NAME);
			return IOUtils.toByteArray(is);
		} catch (Exception e) {
			Logger.e(TestResultsManager.class, "no tests result file available");
			return null;
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	public static void clearResults(Context context) {
		context.deleteFile(Constants.TEST_RESULTS_TO_SUBMIT_FILE_NAME);
	}
	
	public static void saveSumbitedLogs(Context c, byte[] logs) {
		File logFile = new File(storage, Constants.TEST_RESULTS_SUBMITED_FILE_NAME);
		FileOutputStream is = null;
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				Logger.e("TestResultsManager", "failed to save submitted logs to file", e);
				return;
			}
		}
		try {
			if (!logFile.exists()) {
				logFile.createNewFile();
			}
			is = new FileOutputStream(logFile, true);
			is.write(logs);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(is);
		}
		
		verifyReduceSize(logFile);
	}
	
	public static File getSubmitedLogsFile(Context c) {
		return new File(storage, Constants.TEST_RESULTS_SUBMITED_FILE_NAME);
	}

	private static void verifyReduceSize(File logFile) {
		if (logFile.length() > Constants.SUBMITED_LOGS_MAX_SIZE) {
			File temp = new File(logFile.getAbsolutePath() + "_tmp");
			BufferedReader reader = null;
			FileWriter writer = null;
			try {
				reader = new BufferedReader(new FileReader(logFile));
				reader.skip(logFile.length() - Constants.SUBMITED_LOGS_MAX_SIZE / 2);
				reader.readLine();
				
				writer = new FileWriter(temp);
				IOUtils.copy(reader, writer);
				writer.close();
				reader.close();
				
				logFile.delete();
				temp.renameTo(logFile);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				IOUtils.closeQuietly(reader);
				IOUtils.closeQuietly(writer);
			}
		}
	}
	

	
	public static String[] getJSONData(Context context) {
		byte[] data = getResult(context);
		if(data == null){
			return new String[] {};
		}
		String results = new String(data);
		return results.split("\r\n");
	}
}	
