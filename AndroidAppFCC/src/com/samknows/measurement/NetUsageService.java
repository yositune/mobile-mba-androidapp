package com.samknows.measurement;

import com.samknows.libcore.SKLogger;
import com.samknows.measurement.environment.TrafficData;
import com.samknows.measurement.environment.TrafficStatsCollector;
import com.samknows.measurement.storage.DBHelper;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class NetUsageService extends IntentService{

	public NetUsageService() {
		super(NetUsageService.class.getName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		TrafficData td = TrafficStatsCollector.collectTraffic();
		DBHelper db = new DBHelper(this);
		db.insertDataConsumption(td);
		SKLogger.d(this, String.format("%d %d %d %d %d %d %d",td.time, td.totalRxBytes, td.totalTxBytes, td.mobileRxBytes, td.mobileTxBytes,td.appRxBytes, td.appTxBytes));
	}
	
	public static void init(Context ctx,long millis){
		Intent intent = new Intent(ctx, NetUsageService.class);
		ctx.startService(intent);
		AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		PendingIntent p_intent = PendingIntent.getService(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		am.setRepeating(AlarmManager.ELAPSED_REALTIME, millis, millis, p_intent);
	}

}
