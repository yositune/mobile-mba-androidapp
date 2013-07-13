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

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

import android.content.Context;

import com.samknows.measurement.schedule.ScheduleConfig;
import com.samknows.measurement.test.ExecutionQueue;

public class Storage {
	private Context c;
	
	protected Storage(Context c) {
		super();
		this.c = c;
	}

	public ExecutionQueue loadQueue() {
		return (ExecutionQueue) load(Constants.EXECUTION_QUEUE_FILE_NAME);
	}
	
	public void saveExecutionQueue(ExecutionQueue eq) {
		save(Constants.EXECUTION_QUEUE_FILE_NAME, eq);
	}
	
	public void dropExecutionQueue() {
		drop(Constants.EXECUTION_QUEUE_FILE_NAME);
	}
	
	public void dropParamsManager() {
		drop(Constants.TEST_PARAMS_MANAGER_FILE_NAME);
	}
	
	public void saveScheduleConfig(ScheduleConfig sg) {
		save(Constants.SCHEDULE_CONFIG_FILE_NAME, sg);
	}
	
	public TestParamsManager loadParamsManager() {
		return (TestParamsManager) load(Constants.TEST_PARAMS_MANAGER_FILE_NAME);
	}
	
	public void saveTestParamsManager(TestParamsManager m) {
		save(Constants.TEST_PARAMS_MANAGER_FILE_NAME, m);
	}
	
	public ScheduleConfig loadScheduleConfig() {
		return (ScheduleConfig) load(Constants.SCHEDULE_CONFIG_FILE_NAME);
	}
	
	public void dropScheduleConfig(){
		drop(Constants.SCHEDULE_CONFIG_FILE_NAME);
	}
	
	protected synchronized void save(String id, Object data) {
		ObjectOutputStream dos = null;
		try {
			OutputStream os = c.openFileOutput(id, Context.MODE_PRIVATE);
			dos = new ObjectOutputStream(os);
			dos.writeObject(data);
		} catch (Exception e) {
			Logger.e(this, "failed to save object for id: " + id, e);
		} finally {
			IOUtils.closeQuietly(dos);
		}
	}
	
	protected synchronized Object load(String id) {
		ObjectInputStream dis = null;
		try {
			InputStream is = c.openFileInput(id);
			dis = new ObjectInputStream(is);
			return dis.readObject();
		} catch (Exception e) {
			Logger.e(this, "failed to load data for id: " + id);
		} finally {
			IOUtils.closeQuietly(dis);
		}
		
		return null;
	}
	
	protected synchronized void drop(String id) {
		c.deleteFile(id);
	}
}
