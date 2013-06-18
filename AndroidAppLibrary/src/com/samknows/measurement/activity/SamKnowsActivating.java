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


package com.samknows.measurement.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.samknows.measurement.Logger;
import com.samknows.measurement.MainService;
import com.samknows.measurement.R;
import com.samknows.measurement.activity.components.UIUpdate;
import com.samknows.measurement.activity.components.Util;
import com.samknows.measurement.util.LoginHelper;

public class SamKnowsActivating extends BaseLogoutActivity {

	public Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activating);
		Util.initializeFonts(this);
		Util.overrideFonts(this, findViewById(android.R.id.content));

		/*
		 * {"type":"mainprogress", "value":"42"} {"type":"activating"}
		 * {"type":"download"} {"type":"inittests", "total":"24",
		 * "finished":"21", "currentbest":"london", "besttime": "25 ms"}
		 * {"type":"completed"}
		 */

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				TextView tv;

				JSONObject message_json;
				if (msg.obj == null) {
					return;
				}
				message_json = (JSONObject) msg.obj;

				try {
					String type = message_json.getString(UIUpdate.JSON_TYPE);

					if (type == UIUpdate.JSON_MAINPROGRESS) {
						String value = message_json
								.getString(UIUpdate.JSON_VALUE);
						ProgressBar progressbar = (ProgressBar) findViewById(R.id.activation_progress);
						int progress = Integer.parseInt(value);
						progressbar.setProgress(progress);
					} else if (type == UIUpdate.JSON_ACTIVATED) {
						ProgressBar pb = (ProgressBar) findViewById(R.id.activating_progress);
						pb.setVisibility(View.GONE);
						ImageView iv = (ImageView) findViewById(R.id.activating_complete);
						iv.setVisibility(View.VISIBLE);
					} else if (type == UIUpdate.JSON_DOWNLOADED) {
						ProgressBar pb = (ProgressBar) findViewById(R.id.download_progress);
						pb.setVisibility(View.GONE);
						ImageView iv = (ImageView) findViewById(R.id.download_complete);
						iv.setVisibility(View.VISIBLE);
					} else if (type == UIUpdate.JSON_INITTESTS) {
						String total = message_json
								.getString(UIUpdate.JSON_TOTAL);
						String finished = message_json
								.getString(UIUpdate.JSON_FINISHED);
						String currentbest = message_json
								.getString(UIUpdate.JSON_CURRENTBEST);
						String besttime = message_json
								.getString(UIUpdate.JSON_BESTTIME);
						tv = (TextView) findViewById(R.id.currentbest);
						tv.setText(currentbest);
						tv = (TextView) findViewById(R.id.besttime);
						tv.setText(besttime);
						tv = (TextView) findViewById(R.id.server_status);
						tv.setText(finished + " " + getString(R.string.of)
								+ " " + total);

					} else if (type == UIUpdate.JSON_COMPLETED) {
						LoginHelper.openMainScreen(SamKnowsActivating.this);
						SamKnowsActivating.this.finish();
					}

				} catch (JSONException e) {
					Logger.e(SamKnowsActivating.class,
							"Error in parsing JSONObject: " + e.getMessage());

				}

			}
		};
		if (MainService.registerHandler(handler)) {
			Logger.d(this, "handler registered");
		} else {
			Logger.d(this, "MainService is not executing");
			LoginHelper.openMainScreen(SamKnowsActivating.this);
			SamKnowsActivating.this.finish();

		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		MainService.unregisterHandler();
	}

	@Override
	public void onBackPressed() {

	}
}
