package com.samknows.measurement;

import java.io.File;

import com.samknows.libcore.R;
import com.samknows.libcore.SKConstants;
import com.samknows.libcore.SKLogger;
import com.samknows.measurement.test.TestResultsManager;

import android.app.Application;

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
		
		SK2AppSettings.create(this);
		CachingStorage.create(this);
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
}
