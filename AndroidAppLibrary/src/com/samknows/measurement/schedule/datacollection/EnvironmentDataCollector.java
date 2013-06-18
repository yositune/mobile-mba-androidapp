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

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.samknows.measurement.environment.CellTowersDataCollector;
import com.samknows.measurement.environment.DCSData;
import com.samknows.measurement.environment.NetworkDataCollector;
import com.samknows.measurement.environment.PhoneIdentityDataCollector;
import com.samknows.measurement.test.TestContext;

public class EnvironmentDataCollector extends BaseDataCollector{
	private static final long serialVersionUID = 1L;
	//List<String> result = new ArrayList<String>();
	List<DCSData> data = new ArrayList<DCSData>();
	
	@Override
	public void start(TestContext ctx) {
		super.start(ctx);
		if(isEnabled){
			data.add(new PhoneIdentityDataCollector(ctx.getServiceContext()).collect());
			data.add(new NetworkDataCollector(ctx.getServiceContext()).collect());
			data.add(new CellTowersDataCollector(ctx.getServiceContext()).collect());
		}
	}
	
	@Override
	public void clearData(){
		data.clear();
	}
	
	@Override
	public void stop(TestContext ctx){
		data.add(new NetworkDataCollector(ctx.getServiceContext()).collect());
		data.add(new CellTowersDataCollector(ctx.getServiceContext()).collect());
	}

	@Override
	public List<String> getOutput() {
		List<String> ret = new ArrayList<String>();
		for(DCSData d:data){
			ret.addAll(d.convert());
		}
		return ret;
	}

	@Override
	public List<JSONObject> getPassiveMetric() {
		List<JSONObject> ret = new ArrayList<JSONObject>();
		for(DCSData d:data){
			ret.addAll(d.getPassiveMetric());
		}
		return ret;
	}
	
	@Override
	public List<JSONObject> getJSONOutput(){
		List<JSONObject> ret = new ArrayList<JSONObject>();
		for(DCSData d:data){
			ret.addAll(d.convertToJSON());
		}
		return ret;
	}
}
