package com.samknows.measurement.schedule.datacollection;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.samknows.measurement.SK2AppSettings;
import com.samknows.measurement.environment.CellTowersDataCollector;
import com.samknows.measurement.environment.DCSData;
import com.samknows.measurement.environment.NetUsageCollector;
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
	 		// A consequence of the system "collecting" metrics both when we start *and* stop a test, is that 
	 		// this leads to multiple rows in the passive_metric table, with the same batch_id and metric...!
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
 		// A consequence of the system "collecting" metrics both when we start *and* stop a test, is that 
 		// this leads to multiple rows in the passive_metric table, with the same batch_id and metric...!
		
		data.add(new NetworkDataCollector(ctx.getServiceContext()).collect());
		data.add(new CellTowersDataCollector(ctx.getServiceContext()).collect());
		
		//collect the traffic data if enabled in the properties file
		if(SK2AppSettings.getSK2AppSettingsInstance().collect_traffic_data){
			DCSData nud = new NetUsageCollector(ctx.getServiceContext()).collect();
			if(nud != null){
				data.add(nud);
			}
		}
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
