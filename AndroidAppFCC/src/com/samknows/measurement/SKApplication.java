package com.samknows.measurement;

import java.io.File;

import com.samknows.libcore.SKLogger;
import com.samknows.measurement.statemachine.State;
import com.samknows.measurement.test.TestResultsManager;

import android.app.Application;

public class SKApplication extends Application{
	
	static private SKApplication sAppInstance = null;

	public SKApplication() {
		super();
		
		sAppInstance = this;
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
		
		FCCAppSettings.create(this);
		CachingStorage.create(this);
	}


	public static SKApplication getAppInstance() {
		return sAppInstance;
	}
}
