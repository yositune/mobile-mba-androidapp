package com.samknows.measurement;

import org.json.JSONObject;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;

import com.samknows.libcore.SKLogger;
import com.samknows.libcore.SKConstants;
import com.samknows.measurement.activity.components.UIUpdate;
import com.samknows.measurement.environment.TrafficStatsCollector;
import com.samknows.measurement.schedule.ScheduleConfig;
import com.samknows.measurement.statemachine.State;
import com.samknows.measurement.statemachine.ScheduledTestStateMachine;
import com.samknows.measurement.storage.DBHelper;
import com.samknows.measurement.util.OtherUtils;

public class MainService extends IntentService {
	public static final String FORCE_EXECUTION_EXTRA	= "force_execution";		
	private PowerManager.WakeLock wakeLock;
	private TrafficStatsCollector collector;
	private SK2AppSettings appSettings;
	private static boolean isExecuting;
	private static Handler mActivationHandler = null;
	private static Object sync = new Object();
	public MainService() {
		super(MainService.class.getName());
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		SKLogger.d(this, "+++++DEBUG+++++ MainService onBind");
		return super.onBind(intent);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		SKLogger.d(this, "+++++DEBUG+++++ MainService onCreate");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		SKLogger.d(this, "+++++DEBUG+++++ MainService onDestroy");
	}

	
	
	@Override
	protected void onHandleIntent(Intent intent) {
		
		SKLogger.d(this, "+++++DEBUG+++++ MainService onHandleIntent" + intent.toString());
		
		boolean force_execution = intent.getBooleanExtra(FORCE_EXECUTION_EXTRA, false);
		
		try {
			appSettings = SK2AppSettings.getSK2AppSettingsInstance();
			
			ScheduleConfig config = CachingStorage.getInstance().loadScheduleConfig();
			boolean backgroundTest = config == null ? true : config.backgroundTest;
			onBegin();
			
			/* 
			 * The state machine has to be executed when background test is set in the config file and the service
			 * is enabled in the app settings.
			 * Moreover the state machine has to be executed whenever the user force the activation (force_execution = true)
			 * In case the device is in roaming the state machine shouldn't run any test
			 */
			
			if((backgroundTest && appSettings.isServiceEnabled()) || force_execution ) {
				if (!OtherUtils.isRoaming(this)) {
					new ScheduledTestStateMachine(this).executeRoutine();
				} else {
					SKLogger.d(this, "+++++DEBUG+++++ Service disabled(roaming), exiting.");
					OtherUtils.reschedule(this,	SKConstants.SERVICE_RESCHEDULE_IF_ROAMING_OR_DATACAP);
				}
			} else {
				if(!backgroundTest)
					SKLogger.d(this, "+++++DEBUG+++++ Service disabled(config file), exiting.");
				if (!appSettings.isServiceEnabled())
					SKLogger.d(this, "+++++DEBUG+++++ Service disabled(manual), exiting.");
			}
		} catch (Throwable th) {
			//if an error happened we want to restart from State.NONE
			appSettings.saveState(State.NONE);
			SKLogger.d(this, "+++++DEBUG+++++ caught throwable, th=" + th.toString());
			SKLogger.d(this, "+++++DEBUG+++++ call OtherUtils.rescheduleWakeup");
			OtherUtils.rescheduleWakeup(this, appSettings.rescheduleTime);
			SKLogger.e(this, "failed in service ", th);
		}finally{
			onEnd();
		}
	}
	

	public static boolean isExecuting(){
		synchronized(sync){
			return isExecuting;
		}
	}
	
	public void onBegin() {
		SKLogger.d(this, "+++++DEBUG+++++ MainService onBegin (begin)");
		
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
		
		SKLogger.d(this, "+++++DEBUG+++++ MainService onBegin - create collector and call start()");
		collector = new TrafficStatsCollector(this);
		collector.start();
		new DBHelper(this).insertDataConsumption(TrafficStatsCollector.collectTraffic());
		
		SKLogger.d(this, "+++++DEBUG+++++ MainService onBegin (end)");
	}

	private void onEnd() {
		SKLogger.d(this, "+++++DEBUG+++++ MainService onEnd (begin)");
		
		wakeLock.release();
		long bytes = collector.finish();
		appSettings.appendUsedBytes(bytes);
		if(!appSettings.isServiceEnabled()){
			SKLogger.d(this, "+++++DEBUG+++++ MainService onEnd, service not enabled - cancelling alarm");
			OtherUtils.cancelAlarm(this);
		}
		synchronized(sync){
			publish(UIUpdate.completed());
			isExecuting = false;
		}
		new DBHelper(this).insertDataConsumption(TrafficStatsCollector.collectTraffic());
		
		SKLogger.d(this, "+++++DEBUG+++++ MainService onEnd... (end)");
	}
	
	//Start service
	public static void poke(Context ctx) {
		ctx.startService(new Intent(ctx, MainService.class));
	}
	
	public static void force_poke(Context ctx){
		Intent intent = new Intent(ctx, MainService.class);
		intent.putExtra(FORCE_EXECUTION_EXTRA,true);
		ctx.startService(intent);
	}
	
	//Register the handler to update the UI
	public static boolean registerActivationHandler(Context ctx, Handler handler){
		// The Main Service MUST be running for activation to work.
		// However, there is a delay from the request to start the activity, to 
		// actually registering the activity.
		
		synchronized(sync){
			mActivationHandler = handler;
			force_poke(ctx);
		}
		
		return true;
	}
	
	//Unregister current handler
	public static void unregisterActivationHandler(){
		synchronized(sync){
			mActivationHandler = null;
		}
	}
	
	//Send a JSONObject to the registered handler, if any
	public void publish(JSONObject jobj){
		synchronized(sync){
			if(mActivationHandler != null && jobj != null){
				Message msg = new Message();
				msg.obj = jobj;
				mActivationHandler.sendMessage(msg);
			}
		}
	}

}
