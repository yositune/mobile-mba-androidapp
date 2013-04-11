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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.samknows.measurement.R;
import com.samknows.measurement.SamKnowsLoginService;
import com.samknows.measurement.activity.components.Util;
import com.samknows.measurement.net.ResetPasswordAction;

public class SamKnowsResetPassword extends Activity{
	private TextView tvPass, tvPass2,tvCode,tvUsername;
	private Button btnResetUserPassword;
	private SamKnowsLoginService service = new SamKnowsLoginService();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.password_reset);
		
		btnResetUserPassword = (Button) findViewById(R.id.btn_reset_password);
		
		tvUsername = (TextView) findViewById(R.id.username);
		tvPass = (TextView) findViewById(R.id.password);
		tvPass2 = (TextView) findViewById(R.id.password2);
		tvCode = (TextView) findViewById(R.id.resetcode);
	
		
		btnResetUserPassword.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (validate()) {
					String pass = tvPass.getText().toString().trim();
					String code = tvCode.getText().toString().trim();
					String email = tvUsername.getText().toString().trim();
					
					final Dialog dialog = showRegisterDialog();
					final ResetPasswordAction action = new ResetPasswordAction(email,pass,code);
					new AsyncTask<Void, Void, Boolean>() {

						@Override
						protected Boolean doInBackground(Void... params) {
							action.execute();
							return action.isSuccess();
						}

						@Override
						protected void onPostExecute(Boolean result) {
							super.onPostExecute(result);
							dialog.dismiss();
							if (result) {
								showErrorDialog(getString(R.string.reset_password_success));
								} else {
								showErrorDialog(getString(R.string.reset_password_error) + action.getErrorString());
							}
						}
						
						
					}.execute();
				}
			}

		});
		
		
		Util.initializeFonts(this);
		Util.overrideFonts(this, findViewById(android.R.id.content));
	}
	
	public Dialog showRegisterDialog() {
		Dialog dialog = ProgressDialog.show(this, 
				"",
				getString(R.string.password_reset_dialog), true);
		dialog.show();
		return dialog;
	}
	
	private boolean validate() {

		String pass = tvPass.getText().toString().trim();
		String pass2 = tvPass2.getText().toString().trim();
		String code = tvCode.getText().toString().trim();
		String email = tvUsername.getText().toString().trim();
		
		if (!pass.equals(pass2)) {
			showErrorDialog(R.string.passwords_do_not_match);
			return false;
		}
		if (email.equals("")) {
			showErrorDialog(R.string.empty_username_error);
			return false;
		}
		if (code.equals("")) {
			showErrorDialog(R.string.empty_code_error);
			return false;
		}
		return true;
	}

	private void showErrorDialog(String mess) {
		new AlertDialog.Builder(this)
			.setMessage(mess)
			.setPositiveButton("Ok", null)
			.create().show();
	}
	
	private void showErrorDialog(int messId) {
		new AlertDialog.Builder(this)
			.setMessage(messId)
			.setPositiveButton("Ok", null)
			.create().show();
	}
}
