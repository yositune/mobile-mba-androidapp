package com.samknows.measurement.environment;

import android.content.Context;

public abstract class BaseDataCollector {
	protected Context context;

	public BaseDataCollector(Context context) {
		super();
		this.context = context;
	}
	
	public abstract DCSData collect();
}
