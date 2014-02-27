package com.samknows.measurement.schedule.datacollection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;
import org.w3c.dom.Element;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

import com.samknows.libcore.SKLogger;
import com.samknows.measurement.SK2AppSettings;
import com.samknows.measurement.environment.DCSData;
import com.samknows.measurement.environment.LocationData;
import com.samknows.measurement.schedule.ScheduleConfig.LocationType;
import com.samknows.measurement.storage.PassiveMetric;
import com.samknows.measurement.test.TestContext;
import com.samknows.measurement.util.DCSStringBuilder;
import com.samknows.measurement.util.XmlUtils;

public class LocationDataCollector extends BaseDataCollector implements LocationListener{
	private static final long serialVersionUID = 1L;

	private long time;
	private long listenerDelay;
//	private float listenerMinDst;
	private boolean getLastKnown;
	
	private transient List<Location> locations;
	List<DCSData> data = Collections.synchronizedList(new ArrayList<DCSData>());
	DCSData mLastLocation = null;
	private transient LocationManager manager;
	private boolean gotLastLocation;
	private LocationType locationType;
	
	private String lastKnownLocation;
	private transient Location lastKnown;
		
	@Override
	public void start(TestContext tc) {
		super.start(tc);
		locations = Collections.synchronizedList(new ArrayList<Location>());
		manager = (LocationManager) tc.getSystemService(Context.LOCATION_SERVICE);
		
		
		locationType = SK2AppSettings.getSK2AppSettingsInstance().getLocationServiceType();
		//if the provider in the settings is gps but the service is not enable fail over to network provider
		if(locationType == LocationType.gps &&!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			if (manager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)) {
				locationType = LocationType.network;
			}
		}
		
		if(locationType != LocationType.gps && locationType != LocationType.network){
			// throw new RuntimeException("unknown location type: " + locationType);
			
			// Rather than simply crashing the app with an exception - stick to Network type, which will
			// be handled benignly...
			locationType = LocationType.network;
		}
		
		String provider = locationType == LocationType.gps ? LocationManager.GPS_PROVIDER : LocationManager.NETWORK_PROVIDER;
		
		if(getLastKnown){
			lastKnown = manager.getLastKnownLocation(provider);
			if (lastKnown != null) {
				data.add(new LocationData(true, lastKnown, locationType));
				lastKnownLocation = locationToDCSString("LASTKNOWNLOCATION", lastKnown);
			}
		}
		gotLastLocation = false;
		
		// On some devices, this can throw an exception, of the form:
		//   java.lang.IllegalArgumentException: provider doesn't exist: network
		// or (sic!):
		//   java.lang.IllegalArgumentException: provider doesn't exist: null
		// We must not allow that behavior to cause the app to crash.
		try {
			manager.requestLocationUpdates(provider, 0, 0,
					LocationDataCollector.this, Looper.getMainLooper());
			SKLogger.d(this, "start collecting location data from: " + provider);
			
		} catch (java.lang.IllegalArgumentException ex) {
			
			SKLogger.sAssert(getClass(),  false);
			
		}
		
		try {
			SKLogger.d(this, "sleeping: " + time);
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//stop listening for location updates if we are on network. That is done because network location uses network and breaks NetworkCondition
		if (locationType == LocationType.network) {
			manager.removeUpdates(this);
		}
	}

	@Override
	public void clearData(){
		data.clear();
	}
	
	/**
	 * returns true if got location
	 * @param time
	 * @return
	 */
	public synchronized boolean waitForLocation(long time) {
		if (!gotLastLocation) {
			try {
				wait(time);
			} catch (InterruptedException e) {
				SKLogger.e(this, "Interruption while waiting for location", e);
			}
		}
		return gotLastLocation;
	}
	
	@Override
	public void stop(TestContext ctx) {
		if(isEnabled){
			super.stop(ctx);
			manager.removeUpdates(this);
			lastReceivedTime = -1;
			SKLogger.d(this, "location datas: " + locations.size());
			SKLogger.d(this, "stop collecting location data");
		}else{
			SKLogger.d(this, "LocationDataCollector is not enabled");
		}
	}
	
	private long lastReceivedTime=-1;
	@Override
	public synchronized void onLocationChanged(Location location) {
		if (location != null) {
			long timeDiff = System.currentTimeMillis() - lastReceivedTime;
			if (lastReceivedTime == -1 || timeDiff > listenerDelay) {
				lastReceivedTime = System.currentTimeMillis();
				locations.add(location);
				DCSData currLocation = new LocationData(location, locationType);
				data.add(currLocation);
				mLastLocation = currLocation;
				SKLogger.d(this, "received new location");
				gotLastLocation = true;
				notifyAll();
			}
		}
	}
	
	@Override
	public List<String> getOutput() {
		List<String> list = new ArrayList<String>();
		for(DCSData d: data){
			list.addAll(d.convert());
		}
		return list;
	}
	
	@Override
	public List<JSONObject> getPassiveMetric() {
		List<JSONObject> ret = new ArrayList<JSONObject>();
		if(lastKnown != null){
			ret.addAll(locationToPassiveMetric(lastKnown));
		}
		for(Location l: locations){
			ret.addAll(locationToPassiveMetric(l));
		}
		return ret;
	}
	
	private String locationToDCSString(String id, Location loc) {
		return new DCSStringBuilder().append(id)
		.append(loc.getTime() / 1000)
		.append(locationType + "")
		.append(loc.getLatitude())
		.append(loc.getLongitude())
		.append(loc.getAccuracy())
		.build();
	}
	
	//Receive a Location object and returns a JSONObject ready to be inserted in the database
	//and displayed to the interface
	private List<JSONObject> locationToPassiveMetric(Location loc){
		List<JSONObject> ret = new ArrayList<JSONObject>();
		ret.add(PassiveMetric.create(PassiveMetric.METRIC_TYPE.LOCATIONPROVIDER, loc.getTime(), locationType+""));
		ret.add(PassiveMetric.create(PassiveMetric.METRIC_TYPE.LATITUDE, loc.getTime(), String.format("%1.5f", loc.getLatitude())));	
		ret.add(PassiveMetric.create(PassiveMetric.METRIC_TYPE.LONGITUDE, loc.getTime(), String.format("%1.5f", loc.getLongitude())));
		ret.add(PassiveMetric.create(PassiveMetric.METRIC_TYPE.ACCURACY, loc.getTime(), loc.getAccuracy()+" m"));
		return ret;
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}

	@Override
	public void onProviderEnabled(String provider) {}

	@Override
	public void onProviderDisabled(String provider) {}
	
	//---------------------------------------------------------
	public static BaseDataCollector parseXml(Element node) {
		LocationDataCollector c = new LocationDataCollector();
		String time = node.getAttribute("time");
		c.time = XmlUtils.convertTime(time);
		
		String listenerDelay = node.getAttribute("listenerDelay");
		c.listenerDelay = XmlUtils.convertTime(listenerDelay);
//		c.listenerMinDst = Float.valueOf(node.getAttribute("listenerMinDistance"));
		c.getLastKnown = Boolean.parseBoolean(node.getAttribute("lastKnown"));
		return c;
	}
	@Override
	public List<JSONObject> getJSONOutput(){
		List<JSONObject> ret = new ArrayList<JSONObject>();
		for(DCSData d:data){
			ret.addAll(d.convertToJSON());
		}
		return ret;
	}
	
	public List<DCSData> getPartialData(){
		List<DCSData> ret = new ArrayList<DCSData>();
		synchronized(data){
			if( data.isEmpty() && mLastLocation != null){
				ret.add(mLastLocation);
			}
			for(DCSData d:data){
				ret.add(d);
			}
			data.clear();
		}
		return ret;
	}

}
