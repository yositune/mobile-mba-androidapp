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


package com.samknows.measurement.schedule;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.samknows.measurement.Constants;
import com.samknows.measurement.Logger;
import com.samknows.measurement.schedule.condition.ConditionGroup;
import com.samknows.measurement.schedule.datacollection.BaseDataCollector;
import com.samknows.measurement.schedule.failaction.RetryFailAction;
import com.samknows.measurement.util.OtherUtils;
import com.samknows.measurement.util.XmlUtils;

public class ScheduleConfig implements Serializable {
	//strings found in the schedule config
	public static final String CONFIG = "config";
	public static final String GLOBAL = "global";
	public static final String SCHEDULE_VERSION = "schedule-version";
	public static final String SUBMIT_DCS = "submit-dcs";
	public static final String TESTS_ALARM_TYPE = "tests-alarm-type";
	public static final String LOCATION_SERVICE = "location-service";
	public static final String ONFAIL_TEST_ACTION = "onfail-test-action";
	public static final String INIT = "init";
	public static final String TYPE = "type";
	public static final String HOSTS = "hosts";
	public static final String HOST = "host";
	public static final String DNSNAME = "dnsName";
	public static final String DISPLAYNAME = "displayName";
	public static final String DATA_CAP_DEFAULT = "data-cap-default";
	public static final String VALUE = "value";
	public static final String COMMUNICATIONS = "communications";
	public static final String COMMUNICATION = "communication";
	public static final String DATA_COLLECTOR = "data-collector";
	public static final String TIME = "time";
	public static final String RANDOM_INTERVAL = "random-interval";
	public static final String LISTENERDELAY = "listenerDelay";
	public static final String ENABLED = "enabled";
	public static final String CONDITIONS = "conditions";
	public static final String CONDITION = "condition";
	public static final String CONDITION_GROUP = "condition-group";
	public static final String CONDITION_GROUP_ID = "condition-group-id";
	public static final String ID = "id";
	public static final String TESTS = "tests";
	public static final String TEST = "test";
	public static final String SCHEDULED_TESTS = "scheduled-tests";
	public static final String BATCH = "batch";
	public static final String MANUAL_TESTS = "manual-tests";
	
	private static final long serialVersionUID = 1L;
	
	public String version = "";
	public String submitHost ; // submitHost/mobile/submit
	public long downloadedTime;
	public long dataCapDefault;
	public TestAlarmType testAlamType;
	public LocationType locationType;  //location type for data collectors
	public RetryFailAction retryFailAction;
	
	public List<ConditionGroup> conditionGroups = new ArrayList<ConditionGroup>();
	public List<TestDescription> tests = new ArrayList<TestDescription>();
	public List<TestGroup> testGroups = new ArrayList<TestGroup>();
	public List<TestDescription> manual_tests = new ArrayList<TestDescription>();
	public List<String> initTestTypes = new ArrayList<String>();
	public List<BaseDataCollector> dataCollectors = new ArrayList<BaseDataCollector>();
	public HashMap<String, String> hosts = new HashMap<String, String>();
	public HashMap<String, Communication> communications = new HashMap<String, Communication>();
	public long maximumTestUsage = 0;
	public enum TestAlarmType {
		WAKEUP, NO_WAKEUP
	}
	
	public enum LocationType {
		gps, network
	}
	
	public ConditionGroup getConditionGroup(String conditionGroupId) {
		for (ConditionGroup cg : conditionGroups) {
			if (cg.id.equals(conditionGroupId)) {
				return cg;
			}
		}
		Logger.e(this, "condition group not found for id: " + conditionGroupId);
		return new ConditionGroup();
	}
	
	public TestDescription findTest(long testId) {
		for (TestDescription td : tests) {
			if (td.id == testId) return td;
		}
		return null;
	}
	
	public TestGroup findTestGroup(long id){
		for(TestGroup tg: testGroups){
			if(tg.id == id){
				return tg;
			}
		}
		return null;
	}
	
	public TestDescription findTestById(int id){
		for(TestDescription td: tests){
			if(td.testId == id) return td;
		}
		return null;
	}
	
	public TestDescription findTestForType(String type) {
		for (TestDescription td : tests) {
			if (td.type.equals(type)) return td;
		}
		return null;
	}
	
	/*
	 * Returns the test batch to be run in the RunTestActivity
	 */
	public List<TestDescription> testGroup(){
		List<TestDescription> ret = new ArrayList<TestDescription>();
		//Closest Target
		TestDescription td = findTestForType(Constants.TEST_TYPE_CLOSEST_TARGET);
		if(td != null){
			ret.add(td);
		}
		td = findTestForType(Constants.TEST_TYPE_DOWNLOAD);
		if(td != null){
			ret.add(td);
		}
		td = findTestForType(Constants.TEST_TYPE_UPLOAD);
		if(td != null){
			ret.add(td);
		}
		td = findTestForType(Constants.TEST_TYPE_LATENCY);
		if(td != null){
			ret.add(td);
		}
		return ret;
	}
	
	
	public Communication findCommunication(String id){
		return communications.get(id);
	}
	
	public String findHostName(String dnsName) {
		String result = hosts.get(dnsName);
		if (result == null) {
			return dnsName;
		}
		return result;
	}
	
	//------------------------------------------------------------------------
	//parsing from xml
	public static ScheduleConfig parseXml(InputStream is) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
        	Element root = factory.newDocumentBuilder().parse(is).getDocumentElement();
        	return parseXml(root);
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return null;
	}
	
	public static ScheduleConfig parseXml(Element node) {
		
		ScheduleConfig c = new ScheduleConfig();
		c.downloadedTime = System.currentTimeMillis();
		
		//version 
		c.version = XmlUtils.getNodeAttrValue(node, SCHEDULE_VERSION, VALUE);
		//base properties
		c.submitHost = XmlUtils.getNodeAttrValue(node, SUBMIT_DCS, HOST);
		String dataCapValue = XmlUtils.getNodeAttrValue(node, DATA_CAP_DEFAULT, VALUE);
		if (dataCapValue != null && !dataCapValue.equals("")) {
			c.dataCapDefault = Long.parseLong(dataCapValue);
		} else {
			c.dataCapDefault = -1;
		}
		
		String type = XmlUtils.getNodeAttrValue(node, TESTS_ALARM_TYPE, TYPE);
		c.testAlamType = TestAlarmType.valueOf(type);
		
		c.locationType = LocationType.valueOf(XmlUtils.getNodeAttrValue(node, LOCATION_SERVICE, TYPE));
		c.retryFailAction = RetryFailAction.parseXml((Element)node.getElementsByTagName(ONFAIL_TEST_ACTION).item(0));
		
		//conditions
		c.conditionGroups = new ArrayList<ConditionGroup>();
		NodeList conditionGroups = node.getElementsByTagName(CONDITION_GROUP);
		for (int i = 0; i < conditionGroups.getLength(); i++) {
			Element conditionGroupNode = (Element) conditionGroups.item(i);
			ConditionGroup cg = ConditionGroup.parseXml(conditionGroupNode);
			if (cg != null) {
				c.conditionGroups.add(cg);
			}
		}
		
		//tests
		c.tests = new ArrayList<TestDescription>();
		NodeList tests = null;
		for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling()){
			if(child instanceof Element && child.getNodeName().equals(TESTS)){
				tests = ((Element) child).getElementsByTagName(TEST);
				break;
			}
		}
		
		if (tests!=null) {
			for (int i = 0; i < tests.getLength(); i++) {
				Element e = (Element) tests.item(i);
				c.tests.add(TestDescription.parseXml(e));
			}
		}
		
		//tests groups
		c.testGroups = new ArrayList<TestGroup>();
		NodeList tests_groups = node.getElementsByTagName(SCHEDULED_TESTS);
		if(tests_groups.getLength()==1){
			tests_groups = ((Element) tests_groups.item(0)).getElementsByTagName(BATCH);
			for(int i = 0; i < tests_groups.getLength(); i++){
				TestGroup curr = TestGroup.parseXml((Element) tests_groups.item(i));
				curr.setUsage(c.tests);
				c.maximumTestUsage = Math.max(c.maximumTestUsage, curr.netUsage);
				c.testGroups.add(curr);
			}
		}
		
		
		//tests run manually 
		NodeList manual_tests = node.getElementsByTagName(MANUAL_TESTS);
		if(manual_tests.getLength() == 1){
			manual_tests = ((Element) manual_tests.item(0)).getElementsByTagName(TEST);
			for(int i = 0; i < manual_tests.getLength(); i++){
				int testId =Integer.parseInt(((Element)manual_tests.item(i)).getAttribute(ID));
				for(TestDescription td: c.tests){
					if(td.testId == testId){
						c.manual_tests.add(td);
					}
				}
			}
		}
		
		//data-collectors
		c.dataCollectors = new ArrayList<BaseDataCollector>();
		NodeList dataCollectors = node.getElementsByTagName(DATA_COLLECTOR);
		for (int i = 0; i < dataCollectors.getLength(); i++) {
			Element e = (Element) dataCollectors.item(i);
			c.dataCollectors.add(BaseDataCollector.parseXml(e));
		}
		
		//init tests
		NodeList list = ((Element)node.getElementsByTagName(GLOBAL).item(0)).getElementsByTagName(INIT);
		if (list.getLength() == 1) {
			NodeList initTests = ((Element)list.item(0)).getElementsByTagName(TEST);
			for (int i = 0; i < initTests.getLength(); i++) {
				Element e = (Element) initTests.item(i);
				c.initTestTypes.add(e.getAttribute(TYPE));
			}
		} else {
			throw new RuntimeException("more than one init section or none");
		}
		
		//hosts
		list = ((Element)node.getElementsByTagName(GLOBAL).item(0)).getElementsByTagName(HOSTS);
		if (list.getLength() == 1) {
			NodeList initTests = ((Element)list.item(0)).getElementsByTagName(HOST);
			for (int i = 0; i < initTests.getLength(); i++) {
				Element e = (Element) initTests.item(i);
				c.hosts.put(e.getAttribute(DNSNAME), OtherUtils.stringEncoding(e.getAttribute(DISPLAYNAME)));
			}
		} else {
			throw new RuntimeException("more than one hosts section or none");
		}
		
		//Communications
		list = ((Element) node.getElementsByTagName(GLOBAL).item(0)).getElementsByTagName(COMMUNICATIONS);
		if(list.getLength() == 1){
			NodeList communicationList = ((Element)list.item(0)).getElementsByTagName(COMMUNICATION);
			for(int i = 0; i < communicationList.getLength(); i++ ){
				Communication comm = Communication.parseXml((Element)communicationList.item(i));
				c.communications.put(comm.id, comm);
			}
		}
		
		return c;
	}
	
	public int getNumberOfScheduledBatch(){
		int ret = 0;
		for(TestGroup tg: testGroups){
			ret += tg.times.size();
		}
		return ret;
	}
	
	public boolean toUpdate(ScheduleConfig config){
		if(version.equals("")){
			return true;
		}
		if(config.version.equals("")){
			return true;
		}
		return !version.equals(config.version);
	}
	
	public String getConfigVersion(){
		return version;
	}
}
