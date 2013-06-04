package com.samknows.measurement;

import org.json.JSONObject;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;

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
