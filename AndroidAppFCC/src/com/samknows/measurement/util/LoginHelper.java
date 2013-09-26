package com.samknows.measurement.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import com.samknows.libcore.SKLogger;
import com.samknows.libcore.SKConstants;
import com.samknows.measurement.FCCAppSettings;
import com.samknows.measurement.CachingStorage;
import com.samknows.measurement.MainService;
import com.samknows.measurement.R;
import com.samknows.measurement.SamKnowsLoginService;
import com.samknows.measurement.SamKnowsResponseHandler;
import com.samknows.measurement.activity.SamKnowsActivating;
import com.samknows.measurement.activity.SamKnowsLogin;
import com.samknows.measurement.activity.SamKnowsAggregateStatViewerActivity;
import com.samknows.measurement.statemachine.State;

public class LoginHelper {
	private static final String TAG = LoginHelper.class.getSimpleName();
	
	
	public static void login(SamKnowsLoginService service, final Activity acc, final String name, final String pass) {
		final Dialog dialogLogin = showLoginDialog(acc);
		
		final FCCAppSettings appSettings = FCCAppSettings.getFCCAppSettingsInstance();
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
				SKLogger.e(TAG, "Login failure", error);
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
	
	@SuppressLint("InlinedApi")
	public static void openMainScreen(Activity acc) {
		Intent intent = new Intent(acc, SamKnowsAggregateStatViewerActivity.class);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		  intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TASK );
		}
		acc.startActivity(intent);
		acc.finish();
	}
	
	public static boolean isLoggedIn() {
		FCCAppSettings appSettings = FCCAppSettings.getFCCAppSettingsInstance();
		if(appSettings.anonymous == true){
			return true;
		}
		return appSettings.getUsername() != null && appSettings.getPassword() != null;
	}
	
	public static String getCredentialsEncoded() {
		return Base64.encodeToString((getCredentials()).getBytes(), Base64.NO_WRAP);
	}
	
	public static String getCredentials() {
		FCCAppSettings appSettings = FCCAppSettings.getFCCAppSettingsInstance(); 
		return appSettings.getUsername() + ":" + appSettings.getPassword();
	}
	
	
	
	private static void onLogin(Context ctx) {
		int size = FCCAppSettings.getInstance().getDevices().size();
		if (size == 0 || 
			(size == 1 && OtherUtils.isPhoneAssosiated(ctx))) {
			MainService.poke(ctx);
		} else {
			FCCAppSettings.getInstance().setServiceEnabled(false);
		}
	}

	public static void logout(Activity parent) {
		parent.startActivity(new Intent(parent, SamKnowsLogin.class));
		
		FCCAppSettings.getInstance().clearAll();
		FCCAppSettings.getInstance().setServiceActivated(false);
		CachingStorage.getInstance().dropExecutionQueue();
		CachingStorage.getInstance().dropParamsManager();
		
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(SKConstants.INTENT_ACTION_LOGOUT);
		parent.sendBroadcast(broadcastIntent);
	}
}
