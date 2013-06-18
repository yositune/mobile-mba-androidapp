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
