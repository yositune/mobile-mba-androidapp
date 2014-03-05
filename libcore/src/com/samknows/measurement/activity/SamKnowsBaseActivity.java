package com.samknows.measurement.activity;

import java.util.List;

import com.samknows.libcore.SKLogger;
import com.samknows.libcore.SKConstants;

//import com.samknows.measurement.Logger;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

public class SamKnowsBaseActivity extends Activity {
	
	public boolean forceBackToAllowClose() {
		return false;
	}

	public boolean wouldBackButtonReturnMeToTheHomeScreen() {
		
		if (forceBackToAllowClose()) {
			return true;
		}
		
		try {

			ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
			List<RunningTaskInfo> tasks = am.getRunningTasks(2);

			RunningTaskInfo currentTask = tasks.get(0);
			RunningTaskInfo nextTask = tasks.get(1);

			// if we're looking at this application's base/launcher Activity,
			// and the next task is the Android home screen, then we know we're
			// about to close the app...
			if (currentTask.topActivity.equals(currentTask.baseActivity)
					&& nextTask.baseActivity.getPackageName().startsWith("com.android.launcher")) {
				Log.d(this.getClass().toString(), "This activity is the top activity, and will return us to the Home screen");
				return true;
			}
		} catch (java.lang.NullPointerException ex) {
			// Seen on some devices!
			SKLogger.sAssert(getClass(),  false);
		}
        
        Log.d(this.getClass().toString(), "This activity is not the top activity, and will not return us to the Home screen");
        
        return false;
	}

	@Override
	public void onBackPressed() {
		// Ask the user if they want to quit the application when the back key is pressed!
		// This is done ONLY if we're the task root!
		// Note that this will keep the MainService running...
		if (wouldBackButtonReturnMeToTheHomeScreen()) {
			new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(SKConstants.RStringQuit)
			.setMessage(SKConstants.RStringReallyQuit)
			.setPositiveButton(SKConstants.RStringYes, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

					// Stop the activity - which will not stop the application, even though we're the
					// root task!
					SamKnowsBaseActivity.this.finish();
				
	               	SKLogger.d(this, "+++++DEBUG+++++ closing the application!");
		
					// http://stackoverflow.com/questions/2033914/quitting-an-application-is-that-frowned-upon?lq=1
					// Exit the application cleanly... with all destructors called properly.
					// This is to try to resolve battery draining issues.
					System.runFinalizersOnExit(true);

	               	SKLogger.d(this, "+++++DEBUG+++++ about to call exit(0)...");
					System.exit(0);
					
	               	SKLogger.d(this, "+++++DEBUG+++++ exit(0) called!");
				}

			})
			.setNegativeButton(SKConstants.RStringNoDialog, null)
			.show();
			return;
		}

		super.onBackPressed();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(this.getClass().toString(), ">>> onCreate " + this.getClass().toString());
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		Log.d(this.getClass().toString(), ">>> onResume " + this.getClass().toString());
	}

	@Override
	public void onStart() {
		super.onStart();
		
		Log.d(this.getClass().toString(), ">>> onStart " + this.getClass().toString());
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		Log.d(this.getClass().toString(), ">>> onDestroy " + this.getClass().toString());
	}

	@Override
	public void onPause() {
		super.onPause();
		
		Log.d(this.getClass().toString(), ">>> onPause " + this.getClass().toString());
	}

	@Override
	public void onRestart() {
		super.onRestart();
		
		Log.d(this.getClass().toString(), ">>> onRestart " + this.getClass().toString());
	}

	@Override
	public void onStop() {
		super.onStop();
		
		Log.d(this.getClass().toString(), ">>> onStop " + this.getClass().toString());
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		
		Log.d(this.getClass().toString(), ">>> onAttachedToWindow " + this.getClass().toString());
	}

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		
		Log.d(this.getClass().toString(), ">>> onDetachedFromWindow " + this.getClass().toString());
	}
}
