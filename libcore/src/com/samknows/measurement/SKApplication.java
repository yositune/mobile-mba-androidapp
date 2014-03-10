package com.samknows.measurement;

import java.io.File;

import com.samknows.libcore.R;
import com.samknows.libcore.SKConstants;
import com.samknows.libcore.SKLogger;
import com.samknows.measurement.environment.CellTowersDataCollector;
import com.samknows.measurement.storage.ExportFile;
import com.samknows.measurement.test.TestResultsManager;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SKApplication extends Application{

	static private SKApplication sAppInstance = null;
	
	public SKApplication() {
		super();

		sAppInstance = this;

		SKConstants.RStringQuit = R.string.quit;
		SKConstants.RStringReallyQuit = R.string.really_quit;
		SKConstants.RStringYes = R.string.yes;
		SKConstants.RStringNoDialog = R.string.no_dialog;

		SKConstants.PREF_KEY_USED_BYTES = "used_bytes";
		SKConstants.PREF_DATA_CAP = "data_cap_pref";
		SKConstants.PROP_TEST_START_WINDOW_RTC = "test_start_window_in_millis_rtc";
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		File storage = getExternalCacheDir();
		if (storage == null) {
			storage = getCacheDir();
		}

		SKLogger.setStorageFolder(storage);
		TestResultsManager.setStorage(storage);
		ExportFile.setStorage(storage);
		SK2AppSettings.create(this);
		CachingStorage.create(this);
		
		// Start monitoring for cell tower signal strength etc....
		// We need to do this, as Android does not allow us to query this information synchronously.
		CellTowersDataCollector.sStartToCaptureCellTowersData(this);
	}


	public static SKApplication getAppInstance() {
		return sAppInstance;
	}


	// Network type results querying...
	public enum eNetworkTypeResults {
		eNetworkTypeResults_Any,
		eNetworkTypeResults_Mobile,
		eNetworkTypeResults_WiFi
	};

	private static eNetworkTypeResults sNetworkTypeResults = eNetworkTypeResults.eNetworkTypeResults_Mobile;

	public static eNetworkTypeResults getNetworkTypeResults() {
		return sNetworkTypeResults;
	}

	public static void setNetworkTypeResults(eNetworkTypeResults networkTypeResults) {
		sNetworkTypeResults = networkTypeResults;
	}

	// Override this, if you want your application to support a 1-day result view.
	public boolean supportOneDayResultView() {
		return false;
	}

	// Get the class of the main activity!
	public Class getTheMainActivityClass() {
		return null;
	}

	// Return the About screen title.
	public String getAboutScreenTitle() {
		return getApplicationContext().getString(R.string.about);
	}

	public boolean hideJitter() {
		return false;
	}

	public boolean hideJitterLatencyAndPacketLoss() {
		return false;
	}

	public boolean allowUserToSelectTestToRun() {
		// Run all tests!
		return false;
	}

	public boolean isExportMenuItemSupported() {
		return false;
	}
	
	// Datacap enabling/disabling
	
	public boolean canDisableDataCap () {
		return false;
	}
	
	// Datacap - enable/disable (managed via the SKAPreferenceActivity)
	final public boolean getIsDataCapEnabled() {
    	if (canDisableDataCap () == false) {
    		// Can't disable the datacap in this version of the app - so in this
    		// case, the data cap is always enabled.
    		return true;
    	}
    	
		// The value is saved/restored automatically through PreferenceManager.
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		return p.getBoolean(SKConstants.PREF_DATA_CAP_ENABLED, true);
	}
	
	public boolean isSocialMediaExportSupported() {
		return false;
	}
}
