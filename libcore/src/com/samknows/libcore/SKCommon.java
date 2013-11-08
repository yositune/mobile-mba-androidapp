package com.samknows.libcore;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

public class SKCommon {
	
	public static String getVersion() {
		return "1.0";
	}
	
	public static String sGetDecimalStringAnyLocaleAs1Pt5LocalisedString(String value) {
		
		//http://stackoverflow.com/questions/4323599/best-way-to-parsedouble-with-comma-as-decimal-separator
		String valueWithDot = value.replaceAll(",",".");
		
		Double theDoubleValue = Double.valueOf(valueWithDot);
		
		// String.format uses the JVM's default locale.
		//String s = String.format(Locale.US, "%.2f", price);
		Locale theLocale = Locale.getDefault();
		String theStringResult1 = String.format(theLocale, "%1.5f", theDoubleValue);
		String theStringResult = String.format("%1.5f", theDoubleValue);
		
		return theStringResult;
	}
	
	public static double sGetDecimalStringAnyLocaleAsDouble (String value) {
		// The string value might be either 99.99 or 99,99, depending on Locale.
		// We can deal with this safely.
 		//http://stackoverflow.com/questions/4323599/best-way-to-parsedouble-with-comma-as-decimal-separator
 		String valueWithDot = value.replaceAll(",",".");
		return Double.valueOf(valueWithDot);
	}

}
