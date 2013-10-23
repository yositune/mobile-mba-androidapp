package com.samknows.measurement.activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.samknows.measurement.R;
import com.samknows.measurement.activity.components.Util;

public class FCCTermsOfUseActivity extends BaseLogoutActivity {
	WebView mWebView;
	WebAppInterface mWebInterface;
	
	@Override
	public void onStart(){
		super.onStart();
		//getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.fcc_terms_of_use_activity);
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
					FCCTermsOfUseActivity.this).create();
			alertDialog.setMessage(toast);
			alertDialog.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

					alertDialog.dismiss();

				}
			});

			Util.overrideFonts(FCCTermsOfUseActivity.this,
					findViewById(android.R.id.content));

			alertDialog.show();

		}
		
	}
}
