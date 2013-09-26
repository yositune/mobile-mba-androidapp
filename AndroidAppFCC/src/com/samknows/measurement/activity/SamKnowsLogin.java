package com.samknows.measurement.activity;

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

import com.samknows.libcore.SKConstants;
import com.samknows.measurement.FCCAppSettings;
import com.samknows.measurement.R;
import com.samknows.measurement.SamKnowsLoginService;
import com.samknows.measurement.activity.components.Util;
import com.samknows.measurement.util.LoginHelper;

public class SamKnowsLogin extends SamKnowsBaseActivity {

	private static final String TAG = SamKnowsLogin.class.getSimpleName();

	static final int LOGIN_DIALOG = 1;
	static final int LOGIN_ERROR_DIALOG = 2;

	private EditText username;
	private EditText password;
	private Button login;
	private Button register;
	private Button recover;
	private FCCAppSettings appSettings = FCCAppSettings.getFCCAppSettingsInstance();

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
		intentFilter.addAction(SKConstants.INTENT_ACTION_STOP_LOGIN);
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