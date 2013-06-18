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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.samknows.measurement.R;
import com.samknows.measurement.activity.SamKnowsWelcome.WebAppInterface;
import com.samknows.measurement.activity.components.Util;

public class SamKnowsTermsOfUse extends BaseLogoutActivity {
	WebView mWebView;
	WebAppInterface mWebInterface;
	
	@Override
	public void onStart(){
		super.onStart();
		//getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.terms_of_use);
		setTitle(getString(R.string.terms_of_use_title));
		mWebView = (WebView) findViewById(R.id.webview);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebInterface = new WebAppInterface(this);
		mWebView.addJavascriptInterface(mWebInterface, "Android");

		mWebView.loadUrl("file:///android_asset/terms_of_use.htm");
	}
	
	
	public class WebAppInterface {
		Context mContext;

		/** Instantiate the interface and set the context */
		WebAppInterface(Context c) {
			mContext = c;
		}

		@JavascriptInterface
		public void showToast(String toast) {

			final AlertDialog alertDialog = new AlertDialog.Builder(
					SamKnowsTermsOfUse.this).create();
			alertDialog.setMessage(toast);
			alertDialog.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

					alertDialog.dismiss();

				}
			});

			Util.overrideFonts(SamKnowsTermsOfUse.this,
					findViewById(android.R.id.content));

			alertDialog.show();

		}
		
	}
}
