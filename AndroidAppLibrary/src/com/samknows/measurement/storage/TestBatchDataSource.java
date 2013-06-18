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


package com.samknows.measurement.storage;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class TestBatchDataSource {
	private SQLiteDatabase database;
	private SKSQLiteHelper dbhelper;
	
	
	
	private static final String order =  SKSQLiteHelper.TB_COLUMN_DTIME + " ASC";
	
	public TestBatchDataSource(Context context){
		dbhelper = new SKSQLiteHelper(context);
	}
	
	public void open() throws SQLException {
		database = dbhelper.getWritableDatabase();
	}
	
	public void close(){
		database.close();
	}
	
	public TestBatch insertTestBatch(long dtime, boolean manual, List<TestResult> tr, List<PassiveMetric> pm){
		TestBatch ret = new TestBatch(dtime, manual);
		ContentValues values = new ContentValues();
		values.put(SKSQLiteHelper.TB_COLUMN_DTIME, dtime);
		values.put(SKSQLiteHelper.TB_COLUMN_MANUAL, manual ? 1 : 0 );
		long insertId = database.insert(SKSQLiteHelper.TABLE_TESTBATCH, null, values);
		
		return ret;
	}
	
	
	/*
	public TestBatch createTestGroup(long dtime, String result){
		ContentValues values = new ContentValues();
		values.put(SKSQLiteHelper.TG_COLUMN_DTIME, dtime);
		values.put(SKSQLiteHelper.TG_COLUMN_RESULT, result);
		long insertId = database.insert(SKSQLiteHelper.TABLE_TESTGROUP, null, values);
		String where = String.format("%s = %d", SKSQLiteHelper.TG_COLUMN_ID, insertId);
		Cursor cursor = database.query(SKSQLiteHelper.TABLE_TESTGROUP, allColumns,
				where, null, null, null, null);
		TestBatch ret = cursorToTestGroup(cursor);
		cursor.close();
		return ret;
	}
	*/
	
	//Return all the TestGroup in the database ordered by the starttime field
	//in ascendic order
	/*
	public List<TestBatch> getAllTestGroups(){
		List<TestBatch> ret = new ArrayList<TestBatch>();
		Cursor cursor = database.query(SKSQLiteHelper.TABLE_TESTGROUP, allColumns,
				null, null, null, null, order);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			ret.add(cursorToTestGroup(cursor));
			cursor.moveToNext();
		}
		cursor.close();
		return ret;
	}
	
	//Return all the TestGroup in the database between starttime and endtime
	//in ascendic order
	public List<TestBatch> getTestGroupsInterval(long starttime, long endtime){
		List<TestBatch> ret = new ArrayList<TestBatch>();
		String where = String.format("%s BETWEEN %d AND %d",SKSQLiteHelper.TG_COLUMN_DTIME, starttime, endtime);
		Cursor cursor = database.query(SKSQLiteHelper.TABLE_TESTGROUP, allColumns,
				where, null, null, null, order);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			ret.add(cursorToTestGroup(cursor));
			cursor.moveToNext();
		}
		cursor.close();
		return ret;
	}
	
	//Return the first TestGroup after a specific time
	//if there are no TestGroup after that time returns null 
	public TestBatch getFirstAfter(long dtime){
		TestBatch ret = null;
		String where = String.format("%s >= %d", SKSQLiteHelper.TG_COLUMN_DTIME, dtime);
		String limit ="1";
		Cursor cursor = database.query(SKSQLiteHelper.TABLE_TESTGROUP, allColumns, 
				where, null, null, order, limit); 
		cursor.moveToFirst();
		if(!cursor.isAfterLast()){
			ret = cursorToTestGroup(cursor);
		}
		cursor.close();
		return ret;
	}
	
	private TestBatch cursorToTestGroup(Cursor cursor){
		TestBatch ret = new TestBatch();
		ret.id = cursor.getLong(0);
		ret.dtime = cursor.getLong(1);
		ret.result = cursor.getString(3);
		return ret;
	}
*/
}
