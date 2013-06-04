package com.samknows.measurement;

import java.io.File;

import com.samknows.measurement.test.TestResultsManager;

import android.app.Application;

public class SKApplication extends Application{

	@Override
	public void onCreate() {
		super.onCreate();
		File storage = getExternalCacheDir();
		if (storage == null) {
			storage = getCacheDir();
		}
		
		Logger.setStorageFolder(storage);
		TestResultsManager.setStorage(storage);
		
		AppSettings.create(this);
		CachingStorage.create(this);
	}

}
