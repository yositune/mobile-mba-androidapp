package com.samknows.measurement.environment;

import java.util.List;

import android.content.Context;
import android.os.Looper;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import com.samknows.libcore.SKLogger;
import com.samknows.measurement.util.LooperThread;

public class CellTowersDataCollector extends BaseDataCollector{

	public CellTowersDataCollector(Context context) {
		super(context);
	}
	
	TelephonyManager mTelManager;
	CellTowersPhoneStateListener mCellTowersPhoneStateListener;
	

	@Override
	public CellTowersData collect() {
		final TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		final CellTowersData data = new CellTowersData();
		data.time = System.currentTimeMillis();
		data.cellLocation = manager.getCellLocation();
		
		// Note: the following call might return NULL
		data.setNeighbors(manager.getNeighboringCellInfo());
		
		// getAllCellInfo only supported in API version 17 and in some 
		// devices running the that version returns null
		/*  List<CellInfo> cellInfoList = manager.getAllCellInfo();
		if(cellInfoList != null){
			Logger.d(this, "CellInfo  Available");
			for(CellInfo ci: cellInfoList){
				if(ci instanceof CellInfoGsm){
					Logger.d(this, "Cell is Gsm");
					CellInfoGsm cig = (CellInfoGsm) ci;
					Logger.d(this, "ASU level: " +cig.getCellSignalStrength().getAsuLevel()+" dBm: " +cig.getCellSignalStrength().getDbm());
				}
				else if(ci instanceof CellInfoCdma){
					CellInfoCdma cic = (CellInfoCdma) ci;
					Logger.d(this, "Cell is CDMA");
					Logger.d(this, "ASU level: "+ cic.getCellSignalStrength().getAsuLevel() + " dBm "+ cic.getCellSignalStrength().getCdmaDbm());
				}
				else if(ci instanceof CellInfoLte){
					CellInfoLte cil = (CellInfoLte) ci;
					Logger.d(this, "Cell is LTE");
					Logger.d(this, "ASU level: "+ cil.getCellSignalStrength().getAsuLevel() + " dBm "+ cil.getCellSignalStrength().getDbm());
				}
			}
		}*/
		//that's the only way in android to make sync api call instead of async
		LooperThread thread = new LooperThread(new Runnable() {
			@Override
			public void run() {
				SyncPhoneStateListener listener = new SyncPhoneStateListener(manager, data);
				listener.listen();
			}
		});
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return data;
	}
	
	/* 
	 * receives the status change of the phone listener for the cell tower
	 */
	private class CellTowersPhoneStateListener extends PhoneStateListener {
		
		CellTowersData mData;
		
		public CellTowersPhoneStateListener(){
			mData = new CellTowersData();
		}
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength){
			super.onSignalStrengthsChanged(signalStrength);
			mData.time = System.currentTimeMillis();
			mData.signal = signalStrength;
			mData.cellLocation = mTelManager.getCellLocation();
			addData(mData);
		}
		
		@Override
		public void onCellLocationChanged(CellLocation location){
			super.onCellLocationChanged(location);
			mData.time = System.currentTimeMillis();
			mData.cellLocation = location;
			
			addData(mData);
		}
		
		@Override
		public void onCellInfoChanged(List<CellInfo> cellInfo){
			
			List<NeighboringCellInfo> n = mTelManager.getNeighboringCellInfo();
			if( n != null && n.size() > 0){
				CellTowersData neighbours = new CellTowersData();
				neighbours.time = System.currentTimeMillis();
				neighbours.setNeighbors(n);
				addData(neighbours);
			}
		}
	}
	
	/*
	 * performs a synchronous read of the signal strength  
	 */
	private class SyncPhoneStateListener extends PhoneStateListener {
		private TelephonyManager manager;
		private CellTowersData data;
		public SyncPhoneStateListener(TelephonyManager manager, CellTowersData data) {
			this.manager = manager;
			this.data = data;
		}

		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			super.onSignalStrengthsChanged(signalStrength);
			data.signal = signalStrength;
			manager.listen(this, PhoneStateListener.LISTEN_NONE);
			Looper.myLooper().quit();
		}

		public void listen() {
			manager.listen(SyncPhoneStateListener.this, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		}
	}

	
	@Override
	public void start() {
		addData(collect());
		mTelManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		mCellTowersPhoneStateListener = new CellTowersPhoneStateListener();
		int mask = PhoneStateListener.LISTEN_CELL_INFO | PhoneStateListener.LISTEN_CELL_LOCATION | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS;
		mTelManager.listen(mCellTowersPhoneStateListener, mask);
	}

	@Override
	public void stop() {
		mTelManager.listen(mCellTowersPhoneStateListener, PhoneStateListener.LISTEN_NONE);
	}

}
