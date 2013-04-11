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
