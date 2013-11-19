package com.samknows.libcore;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class SKCommon {
	
	public static String getVersion() {
		return "1.0";
	}
	
	public static String sGetDecimalStringAnyLocaleAs1Pt5LocalisedString(String value) {
		
		Locale theLocale = Locale.getDefault();
		
		NumberFormat numberFormat = DecimalFormat.getInstance(theLocale);
		Number theNumber;
		try {
			theNumber = numberFormat.parse(value);
		} catch (ParseException e) {
			
			// Retry, with US locale!
		    numberFormat = DecimalFormat.getInstance(Locale.ENGLISH);
		    
			try {
				theNumber = numberFormat.parse(value);
				
			} catch (ParseException e2) {
				
				// Give up!
				SKLogger.sAssert(SKCommon.class,  false);
				return value;
			}
		}
		
		// String.format uses the JVM's default locale.
		//String s = String.format(Locale.US, "%.2f", price);
		NumberFormat outputFormat = DecimalFormat.getNumberInstance();
		outputFormat.setMinimumIntegerDigits(1);
		outputFormat.setMinimumFractionDigits(5);
		outputFormat.setMaximumFractionDigits(5);
		String theStringResult = outputFormat.format(theNumber);
		//String theStringResult = String.format("%1.5f", theDoubleValue.doubleValue());
		
		return theStringResult;
	}

	
	public static String sGetDecimalStringUSLocaleAs1Pt5LocalisedString(String value) {
		
		Locale theLocale = Locale.ENGLISH;
		
		NumberFormat numberFormat = DecimalFormat.getInstance(theLocale);
		Number theNumber;
		try {
			theNumber = numberFormat.parse(value);
		} catch (ParseException e) {
			SKLogger.sAssert(SKCommon.class,  false);
			return value;
		}
		
		// String.format uses the JVM's default locale.
		//String s = String.format(Locale.US, "%.2f", price);
		NumberFormat outputFormat = DecimalFormat.getNumberInstance();
		outputFormat.setMinimumIntegerDigits(1);
		outputFormat.setMinimumFractionDigits(5);
		outputFormat.setMaximumFractionDigits(5);
		String theStringResult = outputFormat.format(theNumber);
		//String theStringResult = String.format("%1.5f", theDoubleValue.doubleValue());
		
		return theStringResult;
	}
	
	public static double sGetDecimalStringAnyLocaleAsDouble (String value) {
		
		Locale theLocale = Locale.getDefault();
		NumberFormat numberFormat = DecimalFormat.getInstance(theLocale);
		Number theNumber;
		try {
			theNumber = numberFormat.parse(value);
			return theNumber.doubleValue();
		} catch (ParseException e) {
			SKLogger.sAssert(SKCommon.class,  false);
			
			// The string value might be either 99.99 or 99,99, depending on Locale.
			// We can deal with this safely.
	 		//http://stackoverflow.com/questions/4323599/best-way-to-parsedouble-with-comma-as-decimal-separator
	 		String valueWithDot = value.replaceAll(",",".");
			return Double.valueOf(valueWithDot);
		}
	}
}
