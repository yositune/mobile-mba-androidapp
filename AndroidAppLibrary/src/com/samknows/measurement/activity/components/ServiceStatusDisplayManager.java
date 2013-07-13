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


package com.samknows.measurement.activity.components;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.samknows.measurement.AppSettings;
import com.samknows.measurement.MainService;
import com.samknows.measurement.R;
import com.samknows.measurement.statemachine.State;
import com.samknows.measurement.util.OtherUtils;

public class ServiceStatusDisplayManager {
	private Activity activity;
	private Timer timer;
	private OnServiceActivated listener;
	private View progress;
	private TextView tvStatus, tvFailed;
	private Button btnTryAgain;

	public ServiceStatusDisplayManager(final Activity activity, OnServiceActivated listener) {
		super();
		this.activity = activity;
		this.listener = listener;
		//progress = activity.findViewById(R.id.pb_loading);
		//tvStatus = (TextView) activity.findViewById(R.id.tv_preparing_status);
		//tvFailed = (TextView) activity.findViewById(R.id.tv_failed);
		//btnTryAgain = (Button)activity.findViewById(R.id.btnTryAgain);
		
		btnTryAgain.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				MainService.poke(activity);
				show();
			}
		});
	}
	
	public void show() {
		//activity.findViewById(R.id.root_loading).setVisibility(View.VISIBLE);
		progress.setVisibility(View.VISIBLE);
		tvFailed.setVisibility(View.GONE);
		btnTryAgain.setVisibility(View.GONE);
		
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						State state = AppSettings.getInstance().getState();
						final String text = activity.getString(OtherUtils.getStateDescriptionRId(state));
						
						tvStatus.setText(text);
						if (AppSettings.getInstance().isServiceActivated()) {
							timer.cancel();
							if (listener != null) {
								listener.onActivated();
							}
						} else if (!MainService.isExecuting()) {
							timer.cancel();
							//means an error
							showError();
						}
					}
				});
			}
		}, 500, 500);
	}
	
	private void showError() {
		progress.setVisibility(View.GONE);
		tvFailed.setVisibility(View.VISIBLE);
		btnTryAgain.setVisibility(View.VISIBLE);
	}
	
	public void hide() {
		//activity.findViewById(R.id.root_loading).setVisibility(View.GONE);
	}
	
	public void onStop() {
		if (timer != null) {
			timer.cancel();
		}
	}
	
	public interface OnServiceActivated {
		public void onActivated();
	}
}
