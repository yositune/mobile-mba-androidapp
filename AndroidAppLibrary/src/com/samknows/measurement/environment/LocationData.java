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


package com.samknows.measurement.environment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.samknows.measurement.Logger;
import com.samknows.measurement.schedule.ScheduleConfig.LocationType;
import com.samknows.measurement.util.DCSStringBuilder;

import android.location.Location;

public class LocationData implements DCSData {
	public static final String LASTKNOWNLOCATION = "LASTKNOWNLOCATION";
	public static final String LOCATION = "LOCATION";
	public static final String JSON_LOCATION  = "location";
	public static final String JSON_LASTKNOWNLOCATION  = "last_known_location";
	public static final String JSON_LOCATION_TYPE = "location_type";
	public static final String JSON_LATITUDE = "latitude";
	public static final String JSON_LONGITUDE = "longitude";
	public static final String JSON_ACCURACY = "accuracy";
	private Location mLocation;
	private boolean mIsLastKnown = false;
	private LocationType mLocType;

	public LocationData(Location loc, LocationType locType) {
		mLocation = loc;
		mLocType = locType;
	}

	public LocationData(boolean isLastKnown, Location loc, LocationType locType) {
		mLocation = loc;
		mIsLastKnown = isLastKnown;
		mLocType = locType;
	}

	@Override
	public List<String> convert() {
		List<String> ret = new ArrayList<String>();
		DCSStringBuilder dcsBuilder = new DCSStringBuilder();
		dcsBuilder.append(mIsLastKnown ? LASTKNOWNLOCATION : LOCATION)
				.append(mLocation.getTime() / 1000).append(mLocType + "")
				.append(mLocation.getLatitude())
				.append(mLocation.getLongitude())
				.append(mLocation.getAccuracy());
		ret.add(dcsBuilder.build());
		return ret;
	}

	@Override
	public List<JSONObject> getPassiveMetric() {
		return new ArrayList<JSONObject>();
	}

	@Override
	public List<JSONObject> convertToJSON() {
		List<JSONObject> ret = new ArrayList<JSONObject>();
		JSONObject obj = new JSONObject();
		try{
			obj.put(JSON_TYPE, mIsLastKnown ? JSON_LASTKNOWNLOCATION : JSON_LOCATION);
			obj.put(JSON_TIMESTAMP, mLocation.getTime() / 1000 +"");
			obj.put(JSON_DATETIME, new java.util.Date(mLocation.getTime() ).toString());
			obj.put(JSON_LOCATION_TYPE, mLocType + "");
			obj.put(JSON_LATITUDE, mLocation.getLatitude()+"");
			obj.put(JSON_LONGITUDE, mLocation.getLongitude()+"");
			obj.put(JSON_ACCURACY, mLocation.getAccuracy()+"");
			ret.add(obj);
		}catch(JSONException je){
			Logger.e(this, "error in creating json location info: " + je.getMessage());
		};
		return ret;
	}

}
