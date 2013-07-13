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

import android.content.Context;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.samknows.measurement.AppSettings;
import com.samknows.measurement.MainService;
import com.samknows.measurement.R;
import com.samknows.measurement.util.OtherUtils;

public class DataUsedPreference extends DialogPreference{

	private TextView mSplashText,mValueText;
	private Context mContext;
	private String mDialogMessage, mSuffix;
	  
	public DataUsedPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		setTitle(mContext.getString(R.string.data_used_preference_title)+" "+OtherUtils.formatToBytes(AppSettings.getInstance().getUsedBytes()));
	}

	public DataUsedPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mDialogMessage=mContext.getString(R.string.reset_data_cap_question);
		setTitle(mContext.getString(R.string.data_used_preference_title)+" "+OtherUtils.formatToBytes(AppSettings.getInstance().getUsedBytes()));
		 
	}

	
	
	//If data cap was reached once has been reset restart the main service
	@Override
	protected void onDialogClosed(boolean positiveResult){
		super.onDialogClosed(positiveResult);
		AppSettings app = AppSettings.getInstance();
		
		if (positiveResult){
			boolean restart = app.isDataCapReached();
			app.resetDataUsage();
			setTitle(mContext.getString(R.string.data_used_preference_title)+" "+OtherUtils.formatToBytes(app.getUsedBytes()));
			if(restart){
				MainService.poke(mContext);
			}
		}
		
		 		
	}
	
	  @Override 
	  protected View onCreateDialogView() {
	    LinearLayout.LayoutParams params;
	    LinearLayout layout = new LinearLayout(mContext);
	    layout.setOrientation(LinearLayout.VERTICAL);
	    layout.setPadding(6,6,6,6);

	    mSplashText = new TextView(mContext);
	    if (mDialogMessage != null){
	    	mSplashText.setText(mDialogMessage);
	    }
	    layout.addView(mSplashText);

	    mValueText = new TextView(mContext);
	    mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
	    mValueText.setTextSize(32);
	    params = new LinearLayout.LayoutParams(
	        LinearLayout.LayoutParams.FILL_PARENT, 
	        LinearLayout.LayoutParams.WRAP_CONTENT);
	    layout.addView(mValueText, params);

	    return layout;
	  }


	
}
