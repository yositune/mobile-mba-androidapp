package com.samknows.measurement.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.crittercism.app.Crittercism;
import com.samknows.libcore.SKLogger;
import com.samknows.libcore.SKConstants;
import com.samknows.measurement.FCCAppSettings;
import com.samknows.measurement.MainService;
import com.samknows.measurement.NetUsageService;
import com.samknows.measurement.R;
import com.samknows.measurement.activity.components.Util;
import com.samknows.measurement.util.LoginHelper;
import com.samknows.measurement.util.OtherUtils;

@SuppressLint("InlinedApi")
public class FCCMainAndTermsAndConditionsActivity extends BaseLogoutActivity {


	private Button login;
	private Button register;
	private Button next;
	private TextView title;

	WebView webview;
	int page_number;
	int total_pages = 3;
	String version_name;
	WebAppInterface webinterface;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (OtherUtils.isDebuggable(this)) {
			Log.d(this.getClass().toString(), "OtherUtils.isDebuggable(), not running Crittercism");
		} else {
			Log.d(this.getClass().toString(), "This app is NOT debuggable, so setting-up Crittercism!");
			Crittercism.init(getApplicationContext(), "51f7c8b4d0d8f76787000003");
		}

		PackageInfo pInfo;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException nnfe) {
			SKLogger.e(this, "Impossible to find package info", nnfe);
			pInfo = null;
		}
		version_name = pInfo != null ? pInfo.versionName : "";
		final FCCAppSettings appSettings = FCCAppSettings.getFCCAppSettingsInstance();

		final Activity ctx = this;
		//	final Editor e = this.getPreferences(Context.MODE_PRIVATE).edit();

		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		String agreement = prefs.getString("agreement", null);
		if (agreement != null && agreement.equals(version_name)) {
			
			// Go straight to the main screen!
			
			this.setTheme(android.R.style.Theme_NoDisplay);
			if (appSettings.isServiceActivated()) {
				LoginHelper.openMainScreenWithNoTransitionAnimation(ctx);
			} else {
				MainService.poke(ctx);
				if(appSettings.collect_traffic_data){
					NetUsageService.init(ctx, appSettings.collect_traffic_data_interval);
				}

				startActivity(new Intent(ctx, FCCActivationActivity.class));
			}
		} else {
			// The Activity starts invisible (see the AndroidManifest.xml) - we need
			// to make the Activity visible at this point, so that we can show the T&C!
			//this.setTheme(android.R.style.Theme_Holo_Light);¤
			//this.setTheme(R.style.ApplicationStyle);

			setContentView(R.layout.fcc_main_and_terms_and_conditions_activity);
			appSettings.setForceDownload();
			page_number = 1;

			webview = (WebView) findViewById(R.id.webview);
			webview.getSettings().setJavaScriptEnabled(true);

			webview.measure(LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT);

			int target_height = webview.getMeasuredHeight();

			webinterface = new WebAppInterface(this);
			webview.addJavascriptInterface(webinterface, "Android");

			webview.loadUrl("file:///android_asset/notice" + page_number
					+ ".htm");

			Button next_page = (Button) findViewById(R.id.btn_continue);

			next_page.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					webinterface.changePage();
				}
			});

			Util.initializeFonts(this);
			Util.overrideFonts(this, findViewById(android.R.id.content));
		}

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(SKConstants.INTENT_ACTION_STOP_LOGIN);
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				finish();
			}
		};
		registerReceiver(receiver, intentFilter);
	}

	private BroadcastReceiver receiver;

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}

	// Pressing back in this screen, always allows back to close the application.
	public boolean forceBackToAllowClose() {
		if (page_number > total_pages) {
			return true;
		}
		
		if (webinterface == null) {
			return true;
		}
		
		return false;
	}
	
	public boolean wouldBackButtonReturnMeToTheHomeScreen() {
		if (page_number == 1) {
			return true;
		}

		return false;
	}
	
	public void onBackPressed() {
		if (page_number == 1) {
			super.onBackPressed();
		} else {
			if (webinterface == null) {
				super.onBackPressed();
			} else {
				webinterface.changePage(-1);
			}
		}

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
					FCCMainAndTermsAndConditionsActivity.this).create();
			alertDialog.setMessage(toast);
			alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

					alertDialog.dismiss();

				}
			});

			Util.overrideFonts(FCCMainAndTermsAndConditionsActivity.this,
					findViewById(android.R.id.content));

			alertDialog.show();

		}

		@JavascriptInterface
		public void changePage() {
			
			changePage(1);
		}
		
		public void changePage(int PAddThisAmount) {

			final Activity ctx = FCCMainAndTermsAndConditionsActivity.this;
			final Editor e = FCCMainAndTermsAndConditionsActivity.this.getPreferences(
					Context.MODE_PRIVATE).edit();

			if (page_number > total_pages) {
				e.putString("agreement", version_name);
				e.commit();
				FCCAppSettings a = FCCAppSettings.getFCCAppSettingsInstance();
				MainService.force_poke(ctx);
				if(a.collect_traffic_data){
					NetUsageService.init(ctx, a.collect_traffic_data_interval);
				}
				Intent  intent = new Intent(ctx, FCCActivationActivity.class);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TASK );
				}
				ctx.startActivity(intent);
				ctx.finish();
			}
			
			if ( ((page_number < total_pages) && (PAddThisAmount > 0)) ||
			     ((page_number > 0) && (PAddThisAmount < 0))) {
				page_number += PAddThisAmount;
				ViewGroup vg = (ViewGroup) webview.getParent();
				int index = vg.indexOfChild(webview);
				vg.removeView(webview);
				webview  = new WebView(FCCMainAndTermsAndConditionsActivity.this);
				
				webview.getSettings().setJavaScriptEnabled(true);
				
				webview.loadUrl("file:///android_asset/notice" + page_number
						+ ".htm");
				vg.addView(webview,index);
				webview.addJavascriptInterface(webinterface, "Android");
				webview.setVisibility(View.GONE);
				webview.setVisibility(View.VISIBLE);
				ScrollView scrollView = (ScrollView) findViewById(R.id.webscrollview);
				scrollView.fullScroll(View.FOCUS_UP);
			}

			if (page_number == total_pages) {
				page_number++;
				if(!FCCAppSettings.getFCCAppSettingsInstance().data_cap_welcome){
					Button btn_next = (Button) findViewById(R.id.btn_continue);
					btn_next.setText(R.string.agree);
				}
			}
			
						
		}
	}

}
