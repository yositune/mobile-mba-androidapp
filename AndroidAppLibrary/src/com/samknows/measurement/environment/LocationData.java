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
