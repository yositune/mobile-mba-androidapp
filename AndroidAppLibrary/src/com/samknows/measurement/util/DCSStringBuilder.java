package com.samknows.measurement.util;

import com.samknows.measurement.Constants;

public class DCSStringBuilder {
	private StringBuilder builder = new StringBuilder();
	
	public DCSStringBuilder append(String s) {
		builder.append(s);
		builder.append(Constants.RESULT_LINE_SEPARATOR);
		return this;
	}
	
	public DCSStringBuilder append(long l) {
		builder.append(l);
		builder.append(Constants.RESULT_LINE_SEPARATOR);
		return this;
	}
	
	public DCSStringBuilder append(double db) {
		builder.append(db);
		builder.append(Constants.RESULT_LINE_SEPARATOR);
		return this;
	}
	
	public DCSStringBuilder append(int i) {
		builder.append(i);
		builder.append(Constants.RESULT_LINE_SEPARATOR);
		return this;
	}

	public DCSStringBuilder append(boolean isConnected) {
		builder.append(isConnected);
		builder.append(Constants.RESULT_LINE_SEPARATOR);
		return this;
	}

	public String build() {
		return builder.toString();
	}

}
