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


package com.samknows.measurement.activity;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.samknows.measurement.R;
import com.samknows.measurement.test.TestResultsReader;
import com.samknows.measurement.test.outparcer.OutPutRawParcer;
import com.samknows.measurement.test.outparcer.ParcerDataType;

public class TestResultsActivity extends BaseLogoutActivity{
	private TableLayout table;
	private String type;
	private List<ParcerDataType> datas;
	
	private int sortColumnIdx = -1;
	private boolean sortDirection = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_results_layout);
		table = (TableLayout) findViewById(R.id.results_table);
		
		type = getIntent().getStringExtra(TestResultsTabActivity.PARAM_RESULT_TYPE);
		if (type == null || type.equals("")) {
			throw new RuntimeException();
		}
		datas = (List<ParcerDataType>) getIntent().getSerializableExtra(TestResultsTabActivity.PARAM_CONTENT_CONFIG);
		buildView(0);
	}
	
	private void buildView(int sortColumnIdx) {
		table.removeAllViews();
		if (sortColumnIdx != this.sortColumnIdx) {
			this.sortColumnIdx = sortColumnIdx;
			sortDirection = false;
		} else {
			sortDirection = !sortDirection;
		}
		
		LinkedList<Holder> data = new LinkedList<Holder>();
		OutPutRawParcer parcer = new OutPutRawParcer(datas);
		TestResultsReader reader = new TestResultsReader(this, type);
		appendTableRow(null, parcer.headersArray());
		for (int i = 0; i < 50; i++) {
			String line = reader.read();
			if (line == null) {
				break;
			}
			parcer.setSource(line);
			appendDataRow(data, parcer.getValue(this.sortColumnIdx), parcer.parce(), !parcer.isSuccess());
		}
		
		for (Holder h : data) {
			appendTableRow(h.isSuccess, h.displayValues);
		}
	}
	
	private void appendDataRow(LinkedList<Holder> data, Comparable<?> value, String displayValues[], boolean isSuccess) {
		int idx = 0;
		for (Holder h : data) {
			if (sortDirection && h.value.compareTo(value) == 1) {
				break;
			} else if (!sortDirection && h.value.compareTo(value) == -1) {
				break;
			}
			idx++;
		}
		data.add(idx, new Holder(value, displayValues, isSuccess));
	}
	
	private void appendTableRow(Boolean red, String... strings) {
		TableRow tr = new TableRow(this);
		int idx = -1;
		for (String s : strings) {
			idx++;
			final TextView label = new TextView(this);
			label.setText(s);
			label.setPadding(5, 5, 5, 5);
			if (red == null) {
				label.setBackgroundResource(R.drawable.table_black_border);
			} else if (red) {
				label.setBackgroundResource(R.drawable.table_red_border);
			} else {
				label.setBackgroundResource(R.drawable.table_green_border);
			}

			final int currentIdx = idx;
			label.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					Toast.makeText(getApplicationContext(), "sorting column: " + currentIdx, Toast.LENGTH_SHORT).show();
					buildView(currentIdx);
				}
				
			});
			tr.addView(label, new TableRow.LayoutParams
					  (TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));
		}
		
		table.addView(tr, new TableLayout.LayoutParams
				  (TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));
		
	}
	
	private class Holder {
		public Holder(Comparable value, String displayValues[], boolean isSuccess) {
			this.value = value;
			this.displayValues = displayValues;
			this.isSuccess = isSuccess;
		}

		Comparable value;
		String displayValues[];
		boolean isSuccess;
	}
}
