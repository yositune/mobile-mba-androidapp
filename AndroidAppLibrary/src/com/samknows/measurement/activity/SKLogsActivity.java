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
