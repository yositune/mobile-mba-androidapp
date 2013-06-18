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


package com.samknows.measurement.test;

import java.io.File;
import java.io.RandomAccessFile;

public class ReverseFileReader {
	private RandomAccessFile randomfile;	
	private long position;
	
	public ReverseFileReader (File file) throws Exception {		
		// Open up a random access file
		this.randomfile=new RandomAccessFile(file,"r");
		// Set our seek position to the end of the file
		this.position=this.randomfile.length();
			
		// Seek to the end of the file
		this.randomfile.seek(this.position);
		//Move our pointer to the first valid position at the end of the file.
		String thisLine=this.randomfile.readLine();
		while(thisLine == null ) {
			this.position--;
			this.randomfile.seek(this.position);
			thisLine=this.randomfile.readLine();
			this.randomfile.seek(this.position);
		}
	}	
	
	// Read one line from the current position towards the beginning
	public String readLine() throws Exception {		
		int thisCode;
		char thisChar;
		String finalLine="";
		
		// If our position is less than zero already, we are at the beginning
		// with nothing to return.
		if ( this.position < 0 ) {
				return null;
		}
		
		for(;;) {
			// we've reached the beginning of the file
			if ( this.position < 0 ) {
				break;
			}
			// Seek to the current position
			this.randomfile.seek(this.position);
			
			// Read the data at this position
			thisCode=this.randomfile.readByte();
			thisChar=(char)thisCode;
			
			// If this is a line break or carrige return, stop looking
			if (thisCode == 13 || thisCode == 10 ) {
				// See if the previous character is also a line break character.
				// this accounts for crlf combinations
				this.randomfile.seek(this.position-1);
				int nextCode=this.randomfile.readByte();
				if ( (thisCode == 10 && nextCode == 13) || (thisCode == 13 && nextCode == 10) ) {
					// If we found another linebreak character, ignore it
					this.position=this.position-1;
				}
				// Move the pointer for the next readline
				this.position--;
				break;
			} else {
				// This is a valid character append to the string
				finalLine=thisChar + finalLine;
			}
			// Move to the next char
			this.position--;
		}
		// return the line
		return finalLine;
	}	
}
