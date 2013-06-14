package com.samknows.measurement.environment;

import java.util.List;

import android.content.Context;
import android.os.Looper;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import com.samknows.measurement.Logger;
import com.samknows.measurement.util.LooperThread;

public class CellTowersDataCollector extends BaseDataCollector{

	public CellTowersDataCollector(Context context) {
		super(context);
	}

	@Override
	public CellTowersData collect() {
		final TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		final CellTowersData data = new CellTowersData();
		data.time = System.currentTimeMillis();
		data.cellLocation = manager.getCellLocation();
		data.neighbors = manager.getNeighboringCellInfo();
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
				MyPhoneStateListener listener = new MyPhoneStateListener(manager, data);
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
	
	private class MyPhoneStateListener extends PhoneStateListener {
		private TelephonyManager manager;
		private CellTowersData data;
		public MyPhoneStateListener(TelephonyManager manager, CellTowersData data) {
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
			manager.listen(MyPhoneStateListener.this, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		}
	}
}
