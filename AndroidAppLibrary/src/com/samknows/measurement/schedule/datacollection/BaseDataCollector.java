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


package com.samknows.measurement.schedule.datacollection;

import java.io.Serializable;
import java.util.List;

import org.json.JSONObject;
import org.w3c.dom.Element;

import android.os.Handler;

import com.samknows.measurement.Logger;
import com.samknows.measurement.test.TestContext;

public abstract class BaseDataCollector implements Serializable{
	private static final long serialVersionUID = 1L;
	public boolean isEnabled;
	protected TestContext tc;
	
	
	public void start(TestContext ctx){
		clearData();
		tc = ctx;
	}
	public void stop(TestContext ctx){}
	public abstract List<String> getOutput();
	public abstract List<JSONObject> getJSONOutput();
	public abstract List<JSONObject> getPassiveMetric();
	public abstract void clearData();
	public enum Type {
		Location, Environment
	}
	
	public static BaseDataCollector parseXml(Element node) {
		BaseDataCollector c = null;
		try {
			Type type = Type.valueOf(node.getAttribute("type"));
			switch (type) {
			case Location : {
				c = LocationDataCollector.parseXml(node);
				break;
			}
			case Environment : {
				c = new EnvironmentDataCollector();
				break;
			}
			default : Logger.e(BaseDataCollector.class, "not such data collector: " + type);
			}
		} catch (Exception e) {
			Logger.e(BaseDataCollector.class, "Error in parsing data collector type: "+ e.getMessage());
		}
		
		if (c != null) {
			c.isEnabled = true;
			if(!node.getAttribute("enabled").equals("")){
				c.isEnabled = Boolean.valueOf(node.getAttribute("enabled"));
			}
			
		}
		
		return c;
	}
}
