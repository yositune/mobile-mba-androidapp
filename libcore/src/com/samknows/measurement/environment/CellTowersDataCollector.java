package com.samknows.measurement.environment;

import java.util.List;

import com.samknows.libcore.SKLogger;

import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

public class CellTowersDataCollector extends BaseDataCollector{

	public CellTowersDataCollector(Context context) {
		super(context);
	}
	
	/*
	 * performs a synchronous read of the signal strength  
	 */
	static TelephonyManager mTelManager = null;
	static CellTowersData mData = new CellTowersData();
	static AndroidPhoneStateListener phoneStateListener = null;
	static CellTowersData neighbours = new CellTowersData();

	@Override
	synchronized public CellTowersData collect() {
		//final TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		final CellTowersData data = new CellTowersData();
		data.time = System.currentTimeMillis();
		data.cellLocation = mTelManager.getCellLocation();
		
		// Note: the following call might return NULL
		
		//data.setNeighbors(mTelManager.getAllCellInfo());
		data.setNeighbors(mTelManager.getNeighboringCellInfo());
	
		SKLogger.sAssert(CellTowersDataCollector.class, mData.signal != null);
		// This following line is actually essential!
		data.signal = mData.signal;
		addData(mData);
		addData(neighbours);
		
		return data;
	}
	
	synchronized static void sOnSignalStrengthsChanged(SignalStrength signalStrength) {
		mData.time = System.currentTimeMillis();
		mData.signal = signalStrength;
		SKLogger.sAssert(CellTowersDataCollector.class, mData.signal != null);
		mData.cellLocation = mTelManager.getCellLocation();		
	}
	
	synchronized static	void sOnCellLocationChanged(CellLocation location){
		mData.time = System.currentTimeMillis();
		mData.cellLocation = location;
	}
	
	synchronized static void sOnCellInfoChanged(List<CellInfo> cellInfo){
		List<NeighboringCellInfo> n = mTelManager.getNeighboringCellInfo();
		if( n != null && n.size() > 0){
			neighbours.time = System.currentTimeMillis();
			neighbours.setNeighbors(n);
		}
	}
	
	static class AndroidPhoneStateListener extends PhoneStateListener {
		
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength){
			super.onSignalStrengthsChanged(signalStrength);

			CellTowersDataCollector.sOnSignalStrengthsChanged(signalStrength);
		}
		
		@Override
		public void onCellLocationChanged(CellLocation location){
			super.onCellLocationChanged(location);
			CellTowersDataCollector.sOnCellLocationChanged(location);
		}
		
		@Override
		public void onCellInfoChanged(List<CellInfo> cellInfo){
			//super.onCellInfoChanged(cellInfo);
			CellTowersDataCollector.sOnCellInfoChanged(cellInfo);
		}
	}
	
	// This is called just once, to start monitoring for cell tower signal strength...
	// We need to do this, as Android does not allow us to query this information synchronously!
	static public void sStartToCaptureCellTowersData(Context context) {
		phoneStateListener = new AndroidPhoneStateListener ();  

		mTelManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		int mask = PhoneStateListener.LISTEN_CELL_INFO | PhoneStateListener.LISTEN_CELL_LOCATION | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS;
		//int mask = PhoneStateListener.LISTEN_SIGNAL_STRENGTHS;
		mTelManager.listen(phoneStateListener, mask);
	}
	
	@Override
	public void start() {
		addData(collect());
	}

	@Override
	public void stop() {
	}

}
