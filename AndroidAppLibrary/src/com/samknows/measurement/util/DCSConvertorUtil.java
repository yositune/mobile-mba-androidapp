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

import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;

import com.samknows.measurement.AppSettings;
import com.samknows.measurement.R;
import com.samknows.measurement.schedule.condition.NetworkTypeCondition.ConnectivityType;

public class DCSConvertorUtil {
	public static final String UNKNOWN = "UNKNOWN";

	public static String convertPhoneType(int type) {
		switch (type) {
		case TelephonyManager.PHONE_TYPE_NONE:
			return "NONE";
		case TelephonyManager.PHONE_TYPE_GSM:
			return "GSM";
		case TelephonyManager.PHONE_TYPE_CDMA:
			return "CDMA";
		case TelephonyManager.PHONE_TYPE_SIP:
			return "SIP";
		default:
			return UNKNOWN;
		}
	}

	public static int networkTypeToStringId(int type){
		switch(type){
		case TelephonyManager.NETWORK_TYPE_UNKNOWN: return R.string.unknown;
		case TelephonyManager.NETWORK_TYPE_GPRS: return R.string.gprs;
		case TelephonyManager.NETWORK_TYPE_EDGE: return R.string.edge; 
		case TelephonyManager.NETWORK_TYPE_UMTS: return R.string.umts;
		case TelephonyManager.NETWORK_TYPE_HSDPA: return R.string.hsdpa;
		case TelephonyManager.NETWORK_TYPE_HSUPA: return R.string.hsupa;
		case TelephonyManager.NETWORK_TYPE_HSPA: return R.string.hspa;
		case TelephonyManager.NETWORK_TYPE_CDMA: return R.string.cdma;
		case TelephonyManager.NETWORK_TYPE_EVDO_0: return R.string.evdo_0;
		case TelephonyManager.NETWORK_TYPE_EVDO_A: return R.string.evdo_a;
		case TelephonyManager.NETWORK_TYPE_EVDO_B: return R.string.evdo_b;
		case TelephonyManager.NETWORK_TYPE_1xRTT: return R.string.onexrtt;
		case TelephonyManager.NETWORK_TYPE_IDEN: return R.string.iden;
		case TelephonyManager.NETWORK_TYPE_LTE: return R.string.lte;
		case TelephonyManager.NETWORK_TYPE_EHRPD: return R.string.ehrpd;
		case TelephonyManager.NETWORK_TYPE_HSPAP: return R.string.hspap;
		}
		return R.string.unknown;
	}
	
	public static String convertNetworkType(int type){
		return AppSettings.getInstance().getResourceString(networkTypeToStringId(type));
	}
	
/*
	public static String convertNetworkType(int type) {
		switch (type) {
		case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			return "UNKNOWN";
		case TelephonyManager.NETWORK_TYPE_GPRS:
			return "GPRS";
		case TelephonyManager.NETWORK_TYPE_EDGE:
			return "EDGE";
		case TelephonyManager.NETWORK_TYPE_UMTS:
			return "UMTS";
		case TelephonyManager.NETWORK_TYPE_HSDPA:
			return "HSDPA";
		case TelephonyManager.NETWORK_TYPE_HSUPA:
			return "HSUPA";
		case TelephonyManager.NETWORK_TYPE_HSPA:
			return "HSPA";
		case TelephonyManager.NETWORK_TYPE_CDMA:
			return "CDMA";
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
			return "EVDO_0";
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			return "EVDO_A";
		case TelephonyManager.NETWORK_TYPE_1xRTT:
			return "1xRTT";
		default:
			return UNKNOWN;
		}
	}
*/
	public static String convertActiveConnectivityType(int type) {
		switch (type) {
		case ConnectivityManager.TYPE_MOBILE:
			return "MOBILE";
		case ConnectivityManager.TYPE_WIFI:
			return "WIFI";
		default:
			return UNKNOWN;
		}
	}

	public static String convertGsmSignalStrength(int ss) {
		String ret = "";
		if (ss == 99) {
			ret = "N/A";
		} else {
			ret = (ss * 2 - 113) + "dBm";
		}
		return ret;
	}

	public static String convertGsmBitErroRate(int ber) {
		String ret = "";
		switch(ber){
		case 0: ret = "0 %"; break;
		case 1: ret = "0.2 %"; break;
		case 2: ret = "0.4 %"; break;
		case 3: ret = "0.8 %"; break;
		case 4: ret = "1.6 %"; break;
		case 5: ret = "3.2 %"; break;
		case 6: ret = "6.4 %"; break;
		case 7: ret = "12.8 %"; break;
		default :
			ret = "N/A";
		}
		return ret;

	}

	public static int convertConnectivityType(int type) {
		int string_id = 0;
		switch (type) {
		case ConnectivityManager.TYPE_BLUETOOTH:
			string_id = R.string.bluetooth;
			break;
		case ConnectivityManager.TYPE_ETHERNET:
			string_id = R.string.ethernet;
			break;
		case ConnectivityManager.TYPE_MOBILE_DUN:
			string_id = R.string.mobile_dun;
			break;
		case ConnectivityManager.TYPE_MOBILE_HIPRI:
			string_id = R.string.mobile_hipri;
			break;
		case ConnectivityManager.TYPE_MOBILE_MMS:
			string_id = R.string.mobile_mms;
			break;
		case ConnectivityManager.TYPE_MOBILE_SUPL:
			string_id = R.string.mobile_supl;
			break;
		case ConnectivityManager.TYPE_WIFI:
			string_id = R.string.wifi;
		case ConnectivityManager.TYPE_WIMAX:
			string_id = R.string.wimax;
		}
		return string_id;
	}

	public static String covertConnectivityType(ConnectivityType type) {
		switch (type) {
		case TYPE_MOBILE:
			return "MOBILE";
		case TYPE_WIFI:
			return "WIFI";
		default:
			return UNKNOWN;
		}
	}

	// public static String convertRSSI(int rssi) {
	// if(rssi == NeighboringCellInfo.UNKNOWN_RSSI){
	// return "UNKNOWN";
	// }else{
	// return String.valueOf(-113 + 2 * rssi);
	// }
	// }
}
