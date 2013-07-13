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

import com.samknows.measurement.Logger;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SKSQLiteHelper extends SQLiteOpenHelper {
	
	// database and version definition
	private static final String DATABASE_NAME = "sk.db";
	private static final int DATABASE_VERSION = 2;
	
	private Context ctx;
	//tables and columns definition
	
	//test result table
	public static final String TABLE_TESTRESULT = "test_result";
	public static final String TR_COLUMN_ID = "_id";
	public static final String TR_COLUMN_TYPE = "type";
	public static final String TR_COLUMN_DTIME = "dtime";
	public static final String TR_COLUMN_LOCATION = "location";
	public static final String TR_COLUMN_SUCCESS = "success";
	public static final String TR_COLUMN_RESULT = "result";
	public static final String TR_COLUMN_BATCH_ID = "batch_id";
	
	public static final String[] TABLE_TESTRESULT_ALLCOLUMNS = {
		TR_COLUMN_ID, TR_COLUMN_TYPE, TR_COLUMN_DTIME, 
		TR_COLUMN_LOCATION, TR_COLUMN_SUCCESS, TR_COLUMN_RESULT, TR_COLUMN_BATCH_ID
	};
	
	public static String CREATE_TABLE_TESTRESULT = "CREATE TABLE "
			+ TABLE_TESTRESULT + " ( "
			+ TR_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ TR_COLUMN_TYPE + " TEXT NOT NULL, "
			+ TR_COLUMN_DTIME + " INTEGER NOT NULL, "
			+ TR_COLUMN_LOCATION + " TEXT, "
			+ TR_COLUMN_SUCCESS + " INTEGER, "
			+ TR_COLUMN_RESULT + " REAL, "
			+ TR_COLUMN_BATCH_ID + " INTEGER "
			+ " ); ";
	
	public static final String TEST_RESULT_ORDER = TR_COLUMN_DTIME + " DESC";
	
	//Passive metric result table
	public static final String TABLE_PASSIVEMETRIC = "passive_metric";
	public static final String PM_COLUMN_ID = "_id";
	public static final String PM_COLUMN_METRIC = "metric";
	public static final String PM_COLUMN_DTIME = "dtime";
	public static final String PM_COLUMN_VALUE = "value";
	public static final String PM_COLUMN_TYPE = "type";
	public static final String PM_COLUMN_BATCH_ID = "batch_id";
	public static final String[] TABLE_PASSIVEMETRIC_ALLCOLUMNS = {
		PM_COLUMN_ID, PM_COLUMN_METRIC, PM_COLUMN_DTIME, PM_COLUMN_VALUE, PM_COLUMN_TYPE, PM_COLUMN_BATCH_ID	};
	public String CREATE_TABLE_PASSIVEMETRIC = "CREATE TABLE "
			+ TABLE_PASSIVEMETRIC + " ( "
			+ PM_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ PM_COLUMN_METRIC + " TEXT NOT NULL, "
			+ PM_COLUMN_DTIME + " INTEGER NOT NULL, "
			+ PM_COLUMN_VALUE + " TEXT, "
			+ PM_COLUMN_TYPE + " TEXT, "
			+ PM_COLUMN_BATCH_ID + " INTEGER "
			+ " ); ";
	
	//Test batch table
	public static final String TABLE_TESTBATCH = "test_batch";
	public static final String TB_COLUMN_ID = "_id";
	public static final String TB_COLUMN_DTIME = "dtime";
	public static final String TB_COLUMN_MANUAL = "manual";
	public static final String[] TABLE_TESTBATCH_ALLCOLUMNS = {
		TB_COLUMN_ID, TB_COLUMN_DTIME, TB_COLUMN_MANUAL};
	public String CREATE_TABLE_TESTBATCH = "CREATE TABLE "
			+ TABLE_TESTBATCH + " ( "
			+ TB_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ TB_COLUMN_DTIME + " INTEGER NOT NULL, "
			+ TB_COLUMN_MANUAL + " INTEGER "
			+ " ); ";
	
	public static final String TEST_BATCH_ORDER = TB_COLUMN_DTIME + " DESC";
	
	
	public static final String[] TABLES = {TABLE_TESTRESULT, 
		TABLE_PASSIVEMETRIC,TABLE_TESTBATCH };
	
	
			
	//database creation sql statement	
	public String DATABASE_CREATE = CREATE_TABLE_TESTRESULT  
			+ CREATE_TABLE_PASSIVEMETRIC
			+ CREATE_TABLE_TESTBATCH ;
	
	public SKSQLiteHelper(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		ctx = context;
	}
	
	@Override
	public void onCreate(SQLiteDatabase database){
		try{
			database.execSQL(CREATE_TABLE_TESTBATCH);
			Logger.d(SKSQLiteHelper.class, "onCreate: "+CREATE_TABLE_TESTBATCH);
			database.execSQL(CREATE_TABLE_TESTRESULT);
			Logger.d(SKSQLiteHelper.class, "onCreate: "+CREATE_TABLE_TESTRESULT);
			database.execSQL(CREATE_TABLE_PASSIVEMETRIC);
			Logger.d(SKSQLiteHelper.class, "onCreate: "+CREATE_TABLE_PASSIVEMETRIC);
		}catch(SQLException sqle){
			Logger.e(SKSQLiteHelper.class, "Error in creating the database "+ sqle);
		}
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion){
		Logger.d(SKSQLiteHelper.class, "Upgrading database from version "+ oldVersion + " to version "+ newVersion +". All Data will be destroyed.");
		for(String table: TABLES){
			database.execSQL("DROP TABLE IF EXISTS "+ table);
		}
		onCreate(database);
	}
	
}
