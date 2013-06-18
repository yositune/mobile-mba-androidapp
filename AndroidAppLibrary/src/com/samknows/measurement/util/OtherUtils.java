/*
2013 Measuring Broadband America Program
Mobile Measurement Android Application
Copyright (C) 2012  SamKnows Ltd.

The FCC Measuring Broadband America (MBA) Program's Mobile Measurement Effort developed in cooperation with SamKnows Ltd. and diverse stakeholders employs an client-server based anonymized data collection approach to gather broadband performance data in an open and transparent manner with the highest commitment to protecting participants privacy.  All data collected is thoroughly analyzed and processed prior to public release to ensure that subscribers’ privacy interests are protected.

Data related to the radio characteristics of the handset, information about the handset type and operating system (OS) version, the GPS coordinates available from the handset at the time each test is run, the date and time of the observation, and the results of active test results are recorded on the handset in JSON(JavaScript Object Notation) nested data elements within flat files.  These JSON files are then transmitted to storage servers at periodic intervals after the completion of active test measurements.

This Android application source code is made available under the GNU GPL2 for testing purposes only and intended for participants in the SamKnows/FCC Measuring Broadband American program.  It is not intended for general release and this repository may be disabled at any time.


This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/


package com.samknows.measurement.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.samknows.measurement.AppSettings;
import com.samknows.measurement.DeviceDescription;
import com.samknows.measurement.Logger;
import com.samknows.measurement.MainService;
import com.samknows.measurement.R;
import com.samknows.measurement.environment.PhoneIdentityDataCollector;
import com.samknows.measurement.statemachine.State;

public class OtherUtils {
	public static String formatToBytes(long bytes) {
		double data = bytes;
		if (data > 1024*1024) { 
			data /= 1024d;
			data /= 1024d;
			return String.format("%.2fMB", data);
		} else if (data > 1024) {
			data /= 1024d;
			return String.format("%.2fKB", data);
		} else {
			return bytes + "B";
		}
	}
	
	public static String formatToBits(long bytes) {
		double data = bytes;
		data *= 8;
		if (data > 1000*1000) { 
			data /= 1000d;
			data /= 1000d;
			return String.format("%.2fMb", data);
		} else if (data > 1000) {
			data /= 1000d;
			return String.format("%.2fKb", data);
		} else {
			return bytes + "b";
		}
	}
	
	public static void reschedule(Context ctx, long time){
		long actualTime = 0;
		if(AppSettings.getInstance().isWakeUpEnabled()){
			actualTime = rescheduleWakeup(ctx, time);
		}else{
			actualTime = rescheduleRTC(ctx, time);
		}
	}
	
	public static long rescheduleRTC(Context ctx, long time) {
		time = checkRescheduleTime(time);
		Logger.d(OtherUtils.class, "schedule RTC for " + time/1000 + "s from now");
		PendingIntent intent = PendingIntent.getService(ctx, 0, new Intent(ctx, MainService.class), PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager manager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		long millis = System.currentTimeMillis() + time;
		manager.set(AlarmManager.RTC, millis, intent);
		AppSettings.getInstance().saveNextRunTime(millis);
		return millis;
	}
	
	public static long rescheduleWakeup(Context ctx, long time) {
		time = checkRescheduleTime(time);
		Logger.d(OtherUtils.class, "schedule RTC_WAKEUP for " + time/1000 + "s from now");
		PendingIntent intent = PendingIntent.getService(ctx, 0, new Intent(ctx, MainService.class), PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager manager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		long millis = System.currentTimeMillis() + time;
		manager.set(AlarmManager.RTC_WAKEUP, millis, intent);
		AppSettings.getInstance().saveNextRunTime(millis);
		return millis;
	}
	
	//if the reschedule time is less than the testStart window play it safe an reschedule the main service for the 
	//the rescheduleTime
	private static long checkRescheduleTime(long time){
		AppSettings a = AppSettings.getInstance();
		long ret = time;
		if(time <= a.getTestStartWindow()){
			Logger.w(OtherUtils.class, "reschedule time less than testStartWindow ("+a.getTestStartWindow()+"), changing it to: "+ a.rescheduleTime/1000+"s.");
			ret = a.rescheduleTime;
		}
		return ret;
	}
	
	public String getLocalIpAddress() {
	    try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress()) {
	                    return inetAddress.getHostAddress().toString();
	                }
	            }
	        }
	    } catch (SocketException ex) {
	        Logger.e(OtherUtils.class, "failed to get ip address", ex);
	    }
	    return null;
	}
	
	public static boolean isRoaming(Context ctx) {
		TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.isNetworkRoaming();
	}
	
	public static boolean isWifi(Context ctx) {
		NetworkInfo info = ((ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		if (info != null) {
			return info.getType() == ConnectivityManager.TYPE_WIFI;
		}
		return false;
	}
	
	public static int getStateDescriptionRId(State state) {
		switch(state) {
		case NONE : return R.string.st_none_descr;
		case INITIALISE : return R.string.st_initialise_descr;
		case ACTIVATE : return R.string.st_activate_descr;
		case ASSOCIATE : return R.string.st_assosiate_descr;
		case CHECK_CONFIG_VERSION : return R.string.st_check_config_version_descr;
		case DOWNLOAD_CONFIG : return R.string.st_download_config_descr;
		case EXECUTE_QUEUE : return R.string.st_execute_queue_descr;
		case RUN_INIT_TESTS : return R.string.st_run_init_test_descr;
		case SUBMIT_RESULTS : return R.string.st_submit_results_descr;
		case SHUTDOWN : return R.string.st_shutdown_descr;
		default : throw new RuntimeException("no such state: " + state);
		}
	}
	
	public static boolean isPhoneAssosiated(Context ctx) {
		String imei = PhoneIdentityDataCollector.getImei(ctx);
		for (DeviceDescription dd : AppSettings.getInstance().getDevices()) {
			if (dd.isCurrentDevice(imei)) return true;
		}
		return false;
	}
	
	public static void removeDeviceForImei(String imei, List<DeviceDescription> list) {
		for (int i = 0; i < list.size(); i++) {
			DeviceDescription dd = list.get(i);
			if (dd.getMac().equals(imei)) {
				list.remove(i);
				return;
			}
		}
	}
	
	public static  String stringEncoding(String value){
		Pattern p = Pattern.compile("%u([a-zA-Z0-9]{4})");
		Matcher m = p.matcher(value);
		StringBuffer sb = new StringBuffer();
		while(m.find()){
			m.appendReplacement(sb, String.valueOf((char)Integer.parseInt(m.group(1), 16)));
		}
		m.appendTail(sb);
		return sb.toString();
	}
}
