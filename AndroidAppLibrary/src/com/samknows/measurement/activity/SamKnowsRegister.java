package com.samknows.measurement.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.samknows.measurement.Constants;
import com.samknows.measurement.R;
import com.samknows.measurement.SamKnowsLoginService;
import com.samknows.measurement.activity.components.Util;
import com.samknows.measurement.net.RegisterUserAction;
import com.samknows.measurement.util.LoginHelper;

public class SamKnowsRegister extends Activity{
	private TextView tvUsername, tvPass, tvPass2;
	private CheckBox confirm_tc_checkbox;
	private Button btnCreateUser;
	private SamKnowsLoginService service = new SamKnowsLoginService();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		
		tvUsername = (TextView) findViewById(R.id.username);
		tvPass = (TextView) findViewById(R.id.password);
		tvPass2 = (TextView) findViewById(R.id.password2);
		
		confirm_tc_checkbox=(CheckBox) findViewById(R.id.confirm_tc_checkbox);
		
		Button login_button;
		login_button = (Button)findViewById(R.id.login);
		login_button.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View v) {
			finish();
			}
		});
		
		btnCreateUser = (Button) findViewById(R.id.create_user);
		
		btnCreateUser.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (validate()) {
					final String username = tvUsername.getText().toString().trim();
					final String pass = tvPass.getText().toString().trim();
					final Dialog dialog = showRegisterDialog();
					final RegisterUserAction action = new RegisterUserAction(username, pass);
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
								LoginHelper.login(service, SamKnowsRegister.this, username, pass);
								sendBroadcast(new Intent(Constants.INTENT_ACTION_STOP_LOGIN));
							} else {
								showErrorDialog(getString(R.string.error_on_registering) + action.getErrorString());
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
				getString(R.string.registering_in), true);
		dialog.show();
		return dialog;
	}
	
	private boolean validate() {
		String username = tvUsername.getText().toString().trim();
		String pass = tvPass.getText().toString().trim();
		String pass2 = tvPass2.getText().toString().trim();
		
		if (username.equals("")) {
			showErrorDialog(R.string.empty_username_error);
			return false;
		}
		
		if (pass.equals("")) {
			showErrorDialog(R.string.empty_password_error);
			return false;
		}
		
		if (!pass.equals(pass2)) {
			showErrorDialog(R.string.passwords_do_not_match);
			return false;
		}
		
		if (!confirm_tc_checkbox.isChecked()) {
			showErrorDialog(R.string.accept_terms);
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
