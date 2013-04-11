package com.samknows.measurement.environment;

import android.content.Context;
import android.os.Looper;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

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
