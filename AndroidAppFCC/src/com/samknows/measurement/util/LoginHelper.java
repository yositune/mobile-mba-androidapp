package com.samknows.measurement.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.util.Base64;
import com.samknows.measurement.FCCAppSettings;
import com.samknows.measurement.activity.FCCActivationActivity;
import com.samknows.measurement.activity.FCCMainResultsActivity;

public class LoginHelper {
	private static final String TAG = LoginHelper.class.getSimpleName();
	
	
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
		Intent intent = new Intent(acc, FCCActivationActivity.class);
		acc.startActivity(intent);
		acc.finish(); 
	}
	
	@SuppressLint("InlinedApi")
	public static void openMainScreen(Activity acc) {
		boolean  bWithTransitionAnimationTrue = true;
    	openMainScreenWithTransitionAnimation(acc, bWithTransitionAnimationTrue);
	}
	
	public static void openMainScreenWithNoTransitionAnimation(Activity acc) {
		boolean  bWithTransitionAnimationFalse = false;
    	openMainScreenWithTransitionAnimation(acc, bWithTransitionAnimationFalse);
	}
	
	public static void openMainScreenWithTransitionAnimation(Activity acc, boolean PWithTransitionAnimation) {
		Intent intent = new Intent(acc, FCCMainResultsActivity.class);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		  intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TASK );
		}
		if (PWithTransitionAnimation) {
			// Default: use the standard transition animation!
		} else {
			// Not the default - do NOT use the transition animation!
		  intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		}
		acc.startActivity(intent);
		acc.finish();
	}
	
	public static String getCredentialsEncoded() {
		return Base64.encodeToString((getCredentials()).getBytes(), Base64.NO_WRAP);
	}
	
	public static String getCredentials() {
		FCCAppSettings appSettings = FCCAppSettings.getFCCAppSettingsInstance(); 
		return appSettings.getUsername() + ":" + appSettings.getPassword();
	}
}
