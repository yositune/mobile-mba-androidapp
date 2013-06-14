package com.samknows.measurement.environment;

import android.content.Context;
import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;

public class NetworkDataCollector extends BaseDataCollector{

	public NetworkDataCollector(Context context) {
		super(context);
	}

	@Override
	public NetworkData collect() {
		NetworkData data = new NetworkData();
		TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		data.activeNetworkInfo = connManager.getActiveNetworkInfo();
		
		data.time = System.currentTimeMillis();
		
		data.networkOperatorCode = manager.getNetworkOperator();
		data.networkOperatorName = manager.getNetworkOperatorName();
		
		data.simOperatorCode = manager.getSimOperator();
		data.simOperatorName = manager.getSimOperatorName();
		
		data.phoneType = manager.getPhoneType();
		data.isRoaming = manager.isNetworkRoaming();
		data.networkType = manager.getNetworkType();
		if (connManager.getActiveNetworkInfo() == null) {
			data.isConnected = false;
		} else {
			data.isConnected = connManager.getActiveNetworkInfo().isConnected();
		}
		
		return data;
	}
}
