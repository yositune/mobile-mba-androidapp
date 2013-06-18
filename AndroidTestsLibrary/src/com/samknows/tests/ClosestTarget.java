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


package com.samknows.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONObject;

public class ClosestTarget extends Test {

	public static final String TESTSTRING = "CLOSESTTARGET";
		
	/*
	 * Default values for the LatancyTest
	 */
	private static final int NPACKETS = 20;
	private static final int INTERPACKETTIME = 1000000;
	private static final int DELAYTIMEOUT = 2000000;
	private static final int PORT = 6000;

	/*
	 * Constraints for the test parameters This values are needed to avoid to
	 * misconfigure the latency test and hence to make the test useless or worst
	 * to get stuck with the closest target test execution
	 */
	private static final int NUMBEROFPACKETSMAX = 100;
	private static final int NUMBEROFPACKETSMIN = 10;
	private static final int INTERPACKETIMEMAX = 60000000;
	private static final int INTERPACKETIMEMIN = 10000;
	private static final int DELAYTIMEOUTMIN = 1000000;
	private static final int DELAYTIMEOUTMAX = 5000000;
	private static final int NUMBEROFTARGETSMAX = 50;
	private static final int NUMBEROFTARGETSMIN = 2;
	
	public static final String JSON_CLOSETTARGET = "closest_target";
	public static final String JSON_IPCLOSESTTARGET = "ip_closest_target";
	
	public class Result{
		public int total;
		public int completed;
		public String currbest_target;
		public long curr_best_time;
	}
	
	//Used to collect the results from the individual LatencyTests as soon as the finish
	public BlockingQueue<LatencyTest.Result> bq_results = new LinkedBlockingQueue<LatencyTest.Result>();
	
	public ClosestTarget() {
	}

	@Override
	public boolean isReady() {
		if (!between(nPackets, NUMBEROFPACKETSMIN, NUMBEROFPACKETSMAX)) {
			return false;
		}
		if (!between(interPacketTime, INTERPACKETIMEMIN, INTERPACKETIMEMAX)) {
			return false;
		}
		if (!between(delayTimeout, DELAYTIMEOUTMIN, DELAYTIMEOUTMAX)) {
			return false;
		}
		if (!between(targets.size(), NUMBEROFTARGETSMIN, NUMBEROFTARGETSMAX)) {
			return false;
		}
		return true;
	}

	@Override
	public void execute() {
		find();
	}

	@Override
	public void run() {
		find();
	}

	@Override
	public int getNetUsage() {
		return 0;
	}

	@Override
	public boolean isSuccessful() {
		return success;
	}

	public void setNumberOfDatagrams(int n) {
		nPackets = n;
	}

	public void setInterPacketTime(int t) {
		interPacketTime = t;
	}

	public void setDelayTimeout(int t) {
		delayTimeout = t;
	}

	public void setPort(int p) {
		port = p;
	}

	public void addTarget(String target) {
		targets.add(target);
	}

	public void setTargetListEmpty() {
		targets = new ArrayList<String>();
	}

	public boolean find() {
		boolean ret = false;
		if (targets.size() == 0) {
			output();
			return ret;
		}
		ArrayList<Thread> threads = new ArrayList<Thread>();
		latencyTests = new LatencyTest[targets.size()];
		
		for (int i = 0; i < targets.size(); i++) {
			LatencyTest lt = new LatencyTest(targets.get(i), port, nPackets,
					interPacketTime, delayTimeout);
			lt.setBlockingQueueResult(bq_results);
			latencyTests[i] = lt;
			if (latencyTests[i].isReady()) {
				Thread t = new Thread(latencyTests[i]);
				threads.add(t);
				t.start();
			}
		}

		for (int i = 0; i < targets.size(); i++) {
			try {
				threads.get(i).join();

			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
		int minDist = Integer.MAX_VALUE;
		for (int i = 0; i < targets.size(); i++) {
			
			if (latencyTests[i].getOutputField(LatencyTest.STATUSFIELD).equals(
					"OK")) {
				success = true;
				reachableTargets.add(targets.get(i));
				int avg = Integer.parseInt(latencyTests[i]
						.getOutputField(LatencyTest.AVERAGEFIELD));
				if (avg < minDist) {
					closestTarget = targets.get(i);
					ipClosestTarget = latencyTests[i]
							.getOutputField(LatencyTest.IPTARGETFIELD);
					minDist = avg;
				}
			}
		}
		if (reachableTargets.size() > 0) {
			ret = true;
		}
		output();
		return ret;
	}

	public ArrayList<String> reachableTargets() {
		return reachableTargets;
	}
	
	public String getClosest() {
		if (reachableTargets.size() == 0) {
			return null;
		}
		return closestTarget;
	}

	public String getHumanReadableResult(){
		String ret ="";
		if(reachableTargets.size() > 0){
			ret = String.format("The closest target is %s.", closestTarget);
		}else{
			ret = "Impossible to find the best target.";
		}
		return ret;
	}
	
	private void output() {
		ArrayList<String> o = new ArrayList<String>();
		Map<String, String> output= new HashMap<String,String>();
		//string id
		o.add(TESTSTRING);
		output.put(Test.JSON_TYPE, TESTSTRING);
		//TIME
		long time_stamp = unixTimeStamp();
		o.add(time_stamp+"");
		output.put(Test.JSON_TIMESTAMP, time_stamp+"");
		output.put(Test.JSON_DATETIME, new java.util.Date(time_stamp*1000).toString());
		//status
		boolean status = reachableTargets.size() > 0;
		o.add(status ? "OK" : "FAIL");
		output.put(Test.JSON_SUCCESS, Boolean.toString(status));
		//closesttaget
		o.add(closestTarget);
		output.put(JSON_CLOSETTARGET, closestTarget);
		//ip closest target
		o.add(ipClosestTarget);
		output.put(JSON_IPCLOSESTTARGET, ipClosestTarget);
		
		setOutput(o.toArray(new String[1]));
		setJSONOutput(output);
	}

	private Test[] latencyTests = null;
	private ArrayList<String> targets = new ArrayList<String>();
	private ArrayList<String> reachableTargets = new ArrayList<String>();
	private int nPackets = NPACKETS;
	private int interPacketTime = INTERPACKETTIME;
	private int delayTimeout = DELAYTIMEOUT;
	private int port = PORT;
	private String closestTarget = "-";
	boolean success = false;
	private String ipClosestTarget = "-";
	private int finished = 0;
	private long curr_best = Long.MAX_VALUE;
	private String curr_best_target;

	@Override
	public boolean isProgressAvailable() {
		return true;
	}

	@Override
	public int getProgress() {
		if(latencyTests == null){
			return 0;
		}
		int min = 100;
		for(Test t: latencyTests){
			if(t != null){
				int curr = t.getProgress();
				min = curr < min ? curr : min;
			}
		}
		return min;
	}

	public Result getPartialResults(){
		//first result when no test is finished yet
		if(finished == 0 ){
			Result ret = new Result();
			ret.completed = 0;
			ret.total = targets.size();
			ret.curr_best_time = 0;
			ret.currbest_target = "";
			finished++;
			return ret;
		}
		
		if(finished == targets.size() + 1){
			return null;
		}
		Result ret = new Result();
		try{
			LatencyTest.Result r = bq_results.take();
			ret.completed = finished;
			finished++;
			ret.total = targets.size();
			if(r.rtt > 0 && r.rtt < curr_best ){
				curr_best = r.rtt;
				curr_best_target = r.target;
			}
			ret.curr_best_time = curr_best;
			ret.currbest_target = curr_best_target;
		}catch(InterruptedException ie){
			ie.printStackTrace();
			ret = null;
		}
		
		return ret;
	}
	
	@Override
	public HumanReadable getHumanReadable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStringID() {
		return TESTSTRING;
	}
}