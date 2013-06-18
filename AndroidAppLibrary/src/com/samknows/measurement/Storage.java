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
