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


package com.samknows.measurement.activity.components;


public class StatRecord {
	
	// active metrics
	public String active_network_type="";
	public String tests_location;
	public String upload_location="";
	public String upload_result="";
	public String download_location="";
	public String download_result="";
	public String latency_location="";
	public String latency_result="";
	public String packetloss_location="";
	public String packetloss_result="";
	public String jitter_location="";
	public String jitter_result="";
	public String time_stamp="";
	
	//passive metrics
	
	public String passivemetric1="";
	public String passivemetric1_type="";
	public String passivemetric2="";
	public String passivemetric2_type="";
	public String passivemetric3="";
	public String passivemetric3_type="";
	public String passivemetric4="";
	public String passivemetric4_type="";
	public String passivemetric5="";
	public String passivemetric5_type="";
	public String passivemetric6="";
	public String passivemetric6_type="";
	public String passivemetric7="";
	public String passivemetric7_type="";
	public String passivemetric8="";
	public String passivemetric8_type="";
	public String passivemetric9="";
	public String passivemetric9_type="";
	public String passivemetric10="";
	public String passivemetric10_type="";
	public String passivemetric11="";
	public String passivemetric11_type="";
	public String passivemetric12="";
	public String passivemetric12_type="";
	public String passivemetric13="";
	public String passivemetric13_type="";
	public String passivemetric14="";
	public String passivemetric14_type="";
	public String passivemetric15="";
	public String passivemetric15_type="";
	public String passivemetric16="";
	public String passivemetric16_type="";
	public String passivemetric17="";
	public String passivemetric17_type="";
	public String passivemetric18="";
	public String passivemetric18_type="";
	public String passivemetric19="";
	public String passivemetric19_type="";
	public String passivemetric20="";
	public String passivemetric20_type="";
	public String passivemetric21="";
	public String passivemetric21_type="";
	public String passivemetric22="";
	public String passivemetric22_type="";
	public String passivemetric23="";
	public String passivemetric23_type="";
	public String passivemetric24="";
	public String passivemetric24_type="";
	public String passivemetric25="";
	public String passivemetric25_type="";
	public String passivemetric26="";
	public String passivemetric26_type="";
	public String passivemetric27="";
	public String passivemetric27_type="";
	public String passivemetric28="";
	public String passivemetric28_type="";
	public String passivemetric29="";
	public String passivemetric29_type="";
	public String passivemetric30="";
	public String passivemetric30_type="";
	public String passivemetric31="";
	public String passivemetric31_type="";
	public String passivemetric32="";
	public String passivemetric32_type="";
	public String passivemetric33="";
	public String passivemetric33_type="";
	
    public StatRecord() {
 
    }
}