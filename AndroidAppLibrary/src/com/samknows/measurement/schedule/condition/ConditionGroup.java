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


package com.samknows.measurement.schedule.condition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.samknows.measurement.schedule.failaction.RetryFailAction;
import com.samknows.measurement.test.TestContext;

public class ConditionGroup extends Condition implements Serializable{
	private static final long serialVersionUID = 1L;

	public String id;
	public List<Condition> conditions = new ArrayList<Condition>();
	public RetryFailAction failAction;
	
	
	public static ConditionGroup parseXml(Element node) {
		ConditionGroup cg = new ConditionGroup();
		cg.id = node.getAttribute("id");
		
		NodeList conditions = node.getElementsByTagName("condition");
		for (int i = 0; i < conditions.getLength(); i++) {
			Element condition = (Element) conditions.item(i);
			cg.conditions.add(Condition.parseXml(condition));
		}
		
		NodeList list = node.getElementsByTagName("action");
		if (cg != null && list.getLength() > 0) {
			cg.failAction = RetryFailAction.parseXml((Element) list.item(0));
		}
		return cg;
	}


	@Override
	public ConditionGroupResult doTestBefore(TestContext tc) {
		Executor executor = Executors.newCachedThreadPool();
		ConditionGroupResult result = new ConditionGroupResult();
		List<Future<ConditionResult>> futureResults = new ArrayList<Future<ConditionResult>>();
		try {
			//request for result from all conditions
			for (Condition c : conditions) {
				Future<ConditionResult> cr = c.testBefore(tc);  
				futureResults.add(cr);
				if (!cr.isDone()) {
					executor.execute((FutureTask<?>)cr);
				} else if (!cr.get().isSuccess) { // if we have result and it fails than skip all the rest conditions
					break;
				}
			}
			
			for (Future<ConditionResult> future : futureResults) {
				result.add(future.get());
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.isSuccess = false;
		} 
		
		return result;
	}


	@Override
	public ConditionGroupResult testAfter(TestContext tc) {
		ConditionGroupResult result = new ConditionGroupResult();
		for (Condition c : conditions) {
			result.add(c.testAfter(tc));
		}
		return result;
	}
	
	@Override
	public void release(TestContext tc) {
		for (Condition c : conditions) c.release(tc);
	}

	@Override
	public boolean needSeparateThread() {
		return false;
	}
}
