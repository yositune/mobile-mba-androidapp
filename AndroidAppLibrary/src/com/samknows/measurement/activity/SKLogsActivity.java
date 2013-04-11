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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.samknows.measurement.Constants;
import com.samknows.measurement.R;
import com.samknows.measurement.test.TestResultsManager;

public class SKLogsActivity extends BaseLogoutActivity{
	private TextView tv; 
	private RadioGroup group;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logs_layout);
		tv = (TextView) findViewById(R.id.tvContent);
		tv.setMovementMethod(new ScrollingMovementMethod());
		group = (RadioGroup) findViewById(R.id.radioGroup);
		OnClickListener l = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				populate();
			}
		};
		
		findViewById(R.id.radioSubmited).setOnClickListener(l);
		findViewById(R.id.radioToSubmit).setOnClickListener(l);
	}
	@Override
	protected void onResume() {
		super.onResume();
		populate();
	}
	
	private void populate() {
		int selectedId = group.getCheckedRadioButtonId();
		if (selectedId == R.id.radioSubmited) {
			File sdcard = getExternalCacheDir();
			File file = new File(sdcard,Constants.TEST_RESULTS_SUBMITED_FILE_NAME);
			//Read text from file
			StringBuilder text = new StringBuilder();
			BufferedReader br = null;
			try {
			   br = new BufferedReader(new FileReader(file));
			    String line;

			    int linesParced = 0;
			    while ((line = br.readLine()) != null && linesParced < 5000) {
			    	line = formatDate(line);
			        text.append(line);
			        text.append('\n');
			        linesParced++;
			    }
			}
			catch (IOException e) {
			    e.printStackTrace();
			}finally{
				if(br != null){
					try {
						br.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			//Set the text
			tv.setText(text);
			tv.scrollTo(0, 0);
			
		} else if (selectedId == R.id.radioToSubmit){
			byte[] data = TestResultsManager.getResult(getApplicationContext());
			if (data != null) {
				String s = new String(data);
				tv.setText(s);
			} else {
				tv.setText("");
			}
		} else {
			throw new RuntimeException();
		}
		
		if (tv.getText() == null || tv.getText().toString().isEmpty()) {
			tv.setText("no logs available!");
		}
		
	}
	
	
	public static String formatDate(String s) {
		try {
			String ss[] = s.split(Constants.RESULT_LINE_SEPARATOR);
			long time = Long.parseLong(ss[1]) * 1000;
			String replace = new SimpleDateFormat().format(time);
			return s.replace(ss[1], replace);
		} catch (Exception e) {
			//ignore format exceptions
			return s;
		}
	}
}
