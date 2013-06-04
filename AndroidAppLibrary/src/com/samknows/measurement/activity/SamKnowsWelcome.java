package com.samknows.measurement.activity;

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
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.samknows.measurement.AppSettings;
import com.samknows.measurement.Constants;
import com.samknows.measurement.Logger;
import com.samknows.measurement.MainService;
import com.samknows.measurement.R;
import com.samknows.measurement.activity.components.Util;
import com.samknows.measurement.util.LoginHelper;

public class SamKnowsWelcome extends BaseLogoutActivity {


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
		PackageInfo pInfo;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException nnfe) {
			Logger.e(this, "Impossible to find package info", nnfe);
			pInfo = null;
		}
		version_name = pInfo != null ? pInfo.versionName : "";
		final AppSettings appSettings = AppSettings.getInstance();

		if (!appSettings.anonymous) {

			setContentView(R.layout.welcome);

			Util.initializeFonts(this);
			Util.overrideFonts(this, findViewById(android.R.id.content));

			login = (Button) findViewById(R.id.btn_login);
			register = (Button) findViewById(R.id.btn_register);

			// Get the intent that started this activity
			Intent intent = getIntent();
			Uri data = intent.getData();

			if (LoginHelper.isLoggedIn()) {
				LoginHelper.openMainScreen(this);
			}

			// LoginHelper.openMainScreen(this); // bypass login

			if (data != null) {
				String query = data.getQuery();

				if (query != null) {
					Intent resetintent = new Intent(SamKnowsWelcome.this,
							SamKnowsResetPassword.class);
					startActivity(resetintent);
				}
			}

			// LoginHelper.openMainScreen(this); //bypass login

			register.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(SamKnowsWelcome.this,
							SamKnowsRegister.class);
					startActivity(intent);
				}
			});

			login.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(SamKnowsWelcome.this,
							SamKnowsLogin.class);
					startActivity(intent);
				}
			});

		} else {

			final Activity ctx = this;
		//	final Editor e = this.getPreferences(Context.MODE_PRIVATE).edit();

			SharedPreferences prefs = getPreferences(MODE_PRIVATE);
			String agreement = prefs.getString("agreement", null);
			if (agreement != null && agreement.equals(version_name)) {

				if (AppSettings.getInstance().isServiceActivated()) {
					LoginHelper.openMainScreen(ctx);
				} else {
					MainService.poke(ctx);
					startActivity(new Intent(ctx, SamKnowsActivating.class));
				}
			} else {
				setContentView(R.layout.welcome_anonymous);
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
		}

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constants.INTENT_ACTION_STOP_LOGIN);
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

	private void mainScreen() {
		LoginHelper.openMainScreen(this);
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
					SamKnowsWelcome.this).create();
			alertDialog.setMessage(toast);
			alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

					alertDialog.dismiss();

				}
			});

			Util.overrideFonts(SamKnowsWelcome.this,
					findViewById(android.R.id.content));

			alertDialog.show();

		}

		@JavascriptInterface
		public void changePage() {

			final Activity ctx = SamKnowsWelcome.this;
			final Editor e = SamKnowsWelcome.this.getPreferences(
					Context.MODE_PRIVATE).edit();

			if (page_number > total_pages) {
				e.putString("agreement", version_name);
				e.commit();
				
				/*
				MainService.poke(ctx);
				Intent intent = new Intent(ctx, SamKnowsActivating.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				ctx.startActivity(intent);
				ctx.finish();
				*/
				Intent intent = new Intent(ctx, SamKnowsInitialSettings.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				ctx.startActivity(intent);
				ctx.finish();

			}

			if (page_number < total_pages) {
				page_number++;
				ViewGroup vg = (ViewGroup) webview.getParent();
				int index = vg.indexOfChild(webview);
				vg.removeView(webview);
				webview  = new WebView(SamKnowsWelcome.this);
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
				Button btn_next = (Button) findViewById(R.id.btn_continue);
				btn_next.setText(R.string.agree);
				
				

			}
		}
	}
}