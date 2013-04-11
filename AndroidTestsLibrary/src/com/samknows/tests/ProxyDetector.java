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



import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class ProxyDetector extends Test{
	
	private static final String X_REMOTE_ADDR = "X-Remote-Addr";
	private static final String HTTP_VIA = "Via";
	private static final String X_FORWARDED_FOR = "X-Forwarded-For";
	private static final String X_PROXY_DETECTOR = "X-Proxy-Detector";
	
	//OUTPUTFORMAT
	//PROXYDETECTOR;<unix timestamp>;<OK|FAIL>;<target>;<target ip>;<x-remote-addr>;<http-via-ip>;<x-forwarded-ip>
	private static final String TESTSTRING = "PROXYDETECTOR";
	
	private static final String checkHeader(String line, String header){
		String ret = null;
		if(line.toLowerCase().startsWith(header.toLowerCase())){
			int start = line.indexOf(' ') + 1;
			if(start !=-1){
				ret = line.substring(start);
			}
		}
		return ret;
	}
	public ProxyDetector(){
		
	}
	private void output(){
		out.add(TESTSTRING);
		out.add(unixTimeStamp()+"");
		if(success){
			out.add("OK");
		}else{
			out.add("FAIL");
		}
		out.add(target);
		out.add(targetIpAddress);
		out.add(seenIp);
		out.add(httpViaIp);
		out.add(forwardedForIp);
		setOutput(out.toArray(new String[1]));
	}
	
	@Override
	public void execute(){
		try{
			Socket conn = null;
			InetAddress addr = InetAddress.getByName(target);
			targetIpAddress = addr.getHostAddress();
			conn = new Socket(addr, port);
			PrintWriter writerOut = new PrintWriter(conn.getOutputStream(), false);
			writerOut.print(getHeaderRequest());
			writerOut.flush();
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			int returnCode = 0;
			String line = reader.readLine();
			
			if( line != null && line.length() != 0 ){
				int s = line.indexOf(' ');
				int f = line.indexOf(' ', ++s);
				returnCode = Integer.parseInt(line.substring(s, f));
			}
			
			if(returnCode != 200){
				success = false;
				output();
				return;
			}
			
			while ((line = reader.readLine()) != null){
				String headerValue = checkHeader(line, X_PROXY_DETECTOR);
				if(headerValue!= null && headerValue.equals("true")){
					success = true;
					continue;
				}
				headerValue = checkHeader(line, X_REMOTE_ADDR);
				if(headerValue != null){
					seenIp = headerValue;
					continue;
				}
				headerValue = checkHeader(line,X_FORWARDED_FOR);
				if(headerValue != null){
					forwardedForIp = headerValue;
					continue;
				}
				headerValue = checkHeader(line, HTTP_VIA);
				if(headerValue != null){
					httpViaIp = headerValue;
					continue;
				}
				if(line.length() == 0 ){
					break;
				}
			}
			conn.close();
			output();
		}catch(Exception e){
			success = false;
			output();
			e.printStackTrace();
		}
	}
	
	private String getHeaderRequest(){
		String request = "GET /%s HTTP/1.1 \r\nHost: %s \r\nACCEPT: */*\r\n\r\n";
		return String.format(request, file, target);
	}
	
	public boolean isProxyDetected(){
		if(forwardedForIp.equals("NONE") && httpViaIp.equals("NONE")){
			return false;
		}
		return true;
	}
	
	@Override
	public boolean isSuccessful() {
		return success;
	}

	@Override
	public void run() {
		start();
		execute();
		finish();
	}

	@Override
	public String getHumanReadableResult() {
		StringBuilder sb = new StringBuilder();
		sb.append("");
		return null;
	}

	@Override
	public boolean isProgressAvailable() {
		return false;
	}

	@Override
	public int getProgress() {		
		return 0;
	}
	
	@Override
	public boolean isReady() {
		if(target.equals("")){
			return false;
		}
		if(file.equals("")){
			return false;
		}
		return true;
	}

	@Override
	public int getNetUsage() {
		return 0;
	}
	
	public void setPort(int p){
		port = p;
	}
	
	public void setTarget(String t){
		target = t;
	}
	
	public void setFile(String f){
		file = f;
	}
	
	ArrayList<String> out = new ArrayList<String>();
	int port = 80;
	private String targetIpAddress = "" ;
	private String seenIp = "NONE";
	private String target = "";
	private boolean success = false;
	private String forwardedForIp = "NONE";
	private String httpViaIp = "NONE";
	private String file = "";

	@Override
	public HumanReadable getHumanReadable() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getStringID() {
		// TODO Auto-generated method stub
		return null;
	}
}
