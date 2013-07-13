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

import org.json.JSONObject;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

import com.samknows.measurement.activity.components.UIUpdate;
import com.samknows.measurement.environment.TrafficStatsCollector;
import com.samknows.measurement.schedule.ScheduleConfig;
import com.samknows.measurement.statemachine.State;
import com.samknows.measurement.statemachine.StateMachine;
import com.samknows.measurement.util.LoginHelper;
import com.samknows.measurement.util.OtherUtils;

public class MainService extends IntentService {
	private PowerManager.WakeLock wakeLock;
	private TrafficStatsCollector collector = new TrafficStatsCollector();
	private AppSettings appSettings;
	private static boolean isExecuting;
	private static Handler mHandler = null;
	private static Object sync = new Object();
	public MainService() {
		super(MainService.class.getName());
	}

	public static boolean isExecuting(){
		synchronized(sync){
			return isExecuting;
		}
	}
	
	public void onBegin() {
		synchronized(sync){
			isExecuting = true;
		}

		// obtain wake lock, other way our service may stop executing
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				MainService.class.getName());
		wakeLock.acquire();

		// reschedule service in the beginning to ensure it will be started if
		// killed.
		OtherUtils.rescheduleRTC(this, appSettings.rescheduleServiceTime);

		collector.start();
	}

	private void onEnd() {
		wakeLock.release();
		long bytes = collector.finish();
		appSettings.appendUsedBytes(bytes);
		synchronized(sync){
			publish(UIUpdate.completed());
			isExecuting = false;
		}
	}

	
	@Override
	protected void onHandleIntent(Intent intent) {
		
		try {
			appSettings = AppSettings.getInstance();
			ScheduleConfig config = CachingStorage.getInstance().loadScheduleConfig();
			onBegin();
			
			
			
			if (appSettings.isServiceEnabled() && LoginHelper.isLoggedIn()) {
				if (!OtherUtils.isRoaming(this)) {
					new StateMachine(this).executeRoutine();
				} else {
					Logger.d(this, "Service disabled(roaming), exiting.");
					OtherUtils.reschedule(this,	Constants.SERVICE_RESCHEDULE_IF_ROAMING);
				}
			} else {
				if (!appSettings.isServiceEnabled())
					Logger.d(this, "Service disabled(manual), exiting.");
				if (!LoginHelper.isLoggedIn())
					Logger.d(this, "Service disabled(login), exiting.");
			}
		} catch (Throwable th) {
			//if an error happened we want to restart from State.NONE
			appSettings.saveState(State.NONE);
			OtherUtils.rescheduleWakeup(this, appSettings.rescheduleTime);
			Logger.e(this, "failed in service ", th);
		}finally{
			onEnd();
		}
	}
	
	//Start service
	public static void poke(Context ctx) {
		ctx.startService(new Intent(ctx, MainService.class));
	}
	
	//Register the handler to update the UI
	public static boolean registerHandler(Handler handler){
		synchronized(sync){
			if(MainService.isExecuting){
				mHandler = handler;
				return true;
			}
			return false;
		}
	}
	
	//Unregister current handler
	public static void unregisterHandler(){
		synchronized(sync){
			mHandler = null;
		}
	}
	
	//Send a JSONObject to the registered handler, if any
	public void publish(JSONObject jobj){
		synchronized(sync){
			if(mHandler != null && jobj != null){
				Message msg = new Message();
				msg.obj = jobj;
				mHandler.sendMessage(msg);
			}
		}
	}

}
