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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;

import com.samknows.measurement.AppSettings;
import com.samknows.measurement.Constants;
import com.samknows.measurement.R;
import com.samknows.measurement.SamKnowsLoginService;
import com.samknows.measurement.activity.components.Util;
import com.samknows.measurement.util.LoginHelper;

public class SamKnowsLogin extends Activity {

	private static final String TAG = SamKnowsLogin.class.getSimpleName();

	static final int LOGIN_DIALOG = 1;
	static final int LOGIN_ERROR_DIALOG = 2;

	private EditText username;
	private EditText password;
	private Button login;
	private Button register;
	private Button recover;
	private AppSettings appSettings = AppSettings.getInstance();

	private SamKnowsLoginService service = new SamKnowsLoginService();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		login = (Button) findViewById(R.id.login);
		register = (Button) findViewById(R.id.register);
		recover = (Button) findViewById(R.id.btn_recover_password);
		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);

		setupLoginButton();
		setupRegisterButton();
		setupRecoverButton();
		setupUsername();
		setupPassword();

		// Remove Hint when people tap on username
		username.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {

				} else {

				}
			}
		});

		// Remove Hint when people tap on password
		password.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {

				} else {

				}
			}
		});

		if (LoginHelper.isLoggedIn()) {
			LoginHelper.openMainScreen(this);
		}

		// LoginHelper.openMainScreen(this); //bypass login

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constants.INTENT_ACTION_STOP_LOGIN);
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				finish();
			}
		};
		registerReceiver(receiver, intentFilter);

		Util.initializeFonts(this);
		Util.overrideFonts(this, findViewById(android.R.id.content));
	}

	private BroadcastReceiver receiver;

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}

	/* Fill in username if there is one saved */
	private void setupUsername() {
		username.setText(appSettings.getUsername());
	}

	/* Fill in password if there is one saved */
	private void setupPassword() {
		password.setText(appSettings.getPassword());
	}

	/*
	 * Create the click listener for the login button. This handles actually a
	 * couple of things: 1) Show the verifying credentials loader 2) Call check
	 * login 3) If login successful, call mainScreen() 4) If login not
	 * successful, display dialog with error
	 */

	private void setupLoginButton() {
		login.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(TAG, "Login");
				final String _username = username.getText().toString().trim();
				final String _password = password.getText().toString().trim();

				if (validate()) {
					LoginHelper.login(service, SamKnowsLogin.this, _username,
							_password);
				}
			}
		});
	}

	private boolean validate() {
		String user = username.getText().toString().trim();
		String pass = password.getText().toString().trim();

		if (user.equals("")) {
			showErrorDialog(R.string.empty_username_error);
			return false;
		}

		if (pass.equals("")) {
			showErrorDialog(R.string.empty_password_error);
			return false;
		}

		return true;
	}

	private void showErrorDialog(String mess) {
		new AlertDialog.Builder(this).setMessage(mess)
				.setPositiveButton("Ok", null).create().show();
	}

	private void showErrorDialog(int messId) {
		new AlertDialog.Builder(this).setMessage(messId)
				.setPositiveButton("Ok", null).create().show();
	}

	private void setupRegisterButton() {
		register.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(SamKnowsLogin.this,
						SamKnowsRegister.class);
				startActivity(intent);
			}
		});
	}

	private void setupRecoverButton() {
		recover.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(SamKnowsLogin.this,
						SamKnowsRecoverPassword.class);
				startActivity(intent);
			}
		});
	}
}