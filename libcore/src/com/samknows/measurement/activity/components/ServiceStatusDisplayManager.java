package com.samknows.measurement.activity.components;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.samknows.measurement.SK2AppSettings;
import com.samknows.measurement.MainService;
import com.samknows.measurement.statemachine.State;
import com.samknows.measurement.util.OtherUtils;

public class ServiceStatusDisplayManager {
	private Activity activity;
	private Timer timer;
	private OnServiceActivated listener;
	private View progress = null;
	private TextView tvStatus = null;
	private TextView tvFailed = null;
	private Button btnTryAgain = null;

	public ServiceStatusDisplayManager(final Activity activity, OnServiceActivated listener) {
		super();
		this.activity = activity;
		this.listener = listener;
		//progress = activity.findViewById(R.id.pb_loading);
		//tvStatus = (TextView) activity.findViewById(R.id.tv_preparing_status);
		//tvFailed = (TextView) activity.findViewById(R.id.tv_failed);
		//btnTryAgain = (Button)activity.findViewById(R.id.btnTryAgain);
	
		if (btnTryAgain != null) {
			btnTryAgain.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					MainService.poke(activity);
					show();
				}
			});
		}
	}
	
	public void show() {
		//activity.findViewById(R.id.root_loading).setVisibility(View.VISIBLE);
		if (progress != null) {
			progress.setVisibility(View.VISIBLE);
		}
		
		if (tvFailed != null) {
			tvFailed.setVisibility(View.GONE);
		}
		
		if (btnTryAgain != null) {
			btnTryAgain.setVisibility(View.GONE);
		}
		
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						State state = SK2AppSettings.getSK2AppSettingsInstance().getState();
						final String text = activity.getString(OtherUtils.getStateDescriptionRId(state));
						
						tvStatus.setText(text);
						if (SK2AppSettings.getInstance().isServiceActivated()) {
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
		if (progress != null) {
			progress.setVisibility(View.GONE);
		}
		
		if (tvFailed != null) {
			tvFailed.setVisibility(View.VISIBLE);
		}
		
		if (btnTryAgain != null) {
			btnTryAgain.setVisibility(View.VISIBLE);
		}
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
