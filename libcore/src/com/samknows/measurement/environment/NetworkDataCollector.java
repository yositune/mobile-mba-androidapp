package com.samknows.measurement.environment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class NetworkDataCollector extends BaseDataCollector{

	public NetworkDataCollector(Context context) {
		super(context);
	}
	
	TelephonyManager mTelManager;
	ConnectivityManager mConnManager;
	NetworkDataListener mNetworkDataListener; 
	
	private static NetworkData extractData(TelephonyManager telManager, ConnectivityManager connManager){
		NetworkData ret = new NetworkData();
		ret.activeNetworkInfo = connManager.getActiveNetworkInfo();
		
		ret.time = System.currentTimeMillis();
		
		ret.networkOperatorCode = telManager.getNetworkOperator();
		ret.networkOperatorName = telManager.getNetworkOperatorName();
		
		ret.simOperatorCode = telManager.getSimOperator();
		ret.simOperatorName = telManager.getSimOperatorName();
		
		ret.phoneType = telManager.getPhoneType();
		ret.isRoaming = telManager.isNetworkRoaming();
		ret.networkType = telManager.getNetworkType();
		if (connManager.getActiveNetworkInfo() == null) {
			ret.isConnected = false;
		} else {
			ret.isConnected = connManager.getActiveNetworkInfo().isConnected();
		}
		return ret;
	}
	
	void collectData(){
		addData(extractData(mTelManager, mConnManager));
	}
	
	
	@Override
	public NetworkData collect() {
	
		TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	
		return extractData(manager, connManager);
	}
	
	@Override
	public void start() {
		mTelManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		mConnManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		mNetworkDataListener = new NetworkDataListener(this);
		collectData();
		mTelManager.listen(mNetworkDataListener, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
	}

	@Override
	public void stop() {
		mTelManager.listen(mNetworkDataListener, PhoneStateListener.LISTEN_NONE);
	}
	
	private class NetworkDataListener extends PhoneStateListener{
		NetworkDataCollector mNetworkDataCollector;
		public NetworkDataListener(NetworkDataCollector ndc){
			mNetworkDataCollector = ndc;
		}
		@Override
		public void onDataConnectionStateChanged(int state){
			mNetworkDataCollector.collectData();
		}
		
	}
}
