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


package com.samknows.measurement.util;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;

import com.samknows.measurement.AppSettings;
import com.samknows.measurement.CachingStorage;
import com.samknows.measurement.Constants;
import com.samknows.measurement.DeviceDescription;
import com.samknows.measurement.Logger;
import com.samknows.measurement.MainService;
import com.samknows.measurement.R;
import com.samknows.measurement.SamKnowsLoginService;
import com.samknows.measurement.SamKnowsResponseHandler;
import com.samknows.measurement.activity.SKPerformanceActivity;
import com.samknows.measurement.activity.SamKnowsActivating;
import com.samknows.measurement.activity.SamKnowsLogin;
import com.samknows.measurement.activity.SamKnowsAggregateStatViewerActivity;
import com.samknows.measurement.environment.PhoneIdentityDataCollector;
import com.samknows.measurement.statemachine.State;

public class LoginHelper {
	private static final String TAG = LoginHelper.class.getSimpleName();
	
	
	public static void login(SamKnowsLoginService service, final Activity acc, final String name, final String pass) {
		final Dialog dialogLogin = showLoginDialog(acc);
		
		final AppSettings appSettings = AppSettings.getInstance();
		service.checkLogin(name, pass, new SamKnowsResponseHandler(){
			public void onSuccess(JSONObject responce){
				Log.i(TAG, "Login success");
				dialogLogin.dismiss();
				appSettings.saveState(State.NONE);
				try{
					JSONArray devices = responce.getJSONArray("units");
					if (devices.length() > 0) {
						appSettings.saveDevices(devices.toString());
					}
					appSettings.saveUsername(name);
					appSettings.savePassword(pass);
					
					onLogin(acc);
					openActivatingScreen(acc);
				}catch(JSONException e){
					e.printStackTrace();
					showErrorDialog(acc, R.string.login_incorrect);
				}
			}
			public void onFailure(Throwable error){
				Logger.e(TAG, "Login failure", error);
				dialogLogin.dismiss();
				showErrorDialog(acc, R.string.login_incorrect);
			}
		});
	}
	
	public static Dialog showLoginDialog(Context c) {
		Dialog dialog = ProgressDialog.show(c, 
				"",
				c.getString(R.string.logging_in), true);
		dialog.show();
		return dialog;
	}
	
	public static void showErrorDialog(Context c, int messId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(c);
		builder.setMessage(messId)
			.setCancelable(false)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
				}
			});
		
		builder.create().show();
	}
	
	public static void openActivatingScreen(Activity acc) {
		Intent intent = new Intent(acc, SamKnowsActivating.class);
		acc.startActivity(intent);
		acc.finish(); 
	}
	
	public static void openMainScreen(Activity acc) {
		Intent intent = new Intent(acc, SamKnowsAggregateStatViewerActivity.class);
		intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TASK );
		acc.startActivity(intent);
		acc.finish();
	}
	
	public static boolean isLoggedIn() {
		AppSettings appSettings = AppSettings.getInstance();
		if(appSettings.anonymous == true){
			return true;
		}
		return appSettings.getUsername() != null && appSettings.getPassword() != null;
	}
	
	public static String getCredentialsEncoded() {
		return Base64.encodeToString((getCredentials()).getBytes(), Base64.NO_WRAP);
	}
	
	public static String getCredentials() {
		AppSettings appSettings = AppSettings.getInstance(); 
		return appSettings.getUsername() + ":" + appSettings.getPassword();
	}
	
	
	
	private static void onLogin(Context ctx) {
		int size = AppSettings.getInstance().getDevices().size();
		if (size == 0 || 
			(size == 1 && OtherUtils.isPhoneAssosiated(ctx))) {
			MainService.poke(ctx);
		} else {
			AppSettings.getInstance().setServiceEnabled(false);
		}
	}

	public static void logout(Activity parent) {
		parent.startActivity(new Intent(parent, SamKnowsLogin.class));
		
		AppSettings.getInstance().clearAll();
		AppSettings.getInstance().setServiceActivated(false);
		CachingStorage.getInstance().dropExecutionQueue();
		CachingStorage.getInstance().dropParamsManager();
		
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(Constants.INTENT_ACTION_LOGOUT);
		parent.sendBroadcast(broadcastIntent);
	}
}
