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
import android.widget.TextView;

import com.samknows.measurement.R;
import com.samknows.measurement.SamKnowsLoginService;
import com.samknows.measurement.activity.components.Util;
import com.samknows.measurement.net.RecoverPasswordAction;

public class SamKnowsRecoverPassword extends Activity{
	private TextView tvUsername;
	private Button btnRecoverUserPassword,btn_goto_reset;
	private SamKnowsLoginService service = new SamKnowsLoginService();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.password_recovery);
		
		tvUsername = (TextView) findViewById(R.id.username);
		btnRecoverUserPassword = (Button) findViewById(R.id.btn_request_recovery);
		btn_goto_reset = (Button) findViewById(R.id.btn_goto_reset);
		btn_goto_reset.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(SamKnowsRecoverPassword.this, SamKnowsResetPassword.class);
				startActivity(intent);
			}
		});
	
		btnRecoverUserPassword.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (validate()) {
					final String username = tvUsername.getText().toString().trim();
					final Dialog dialog = showRegisterDialog();
					final RecoverPasswordAction action = new RecoverPasswordAction(username, "");
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
								showErrorDialog(getString(R.string.requesting_reset_password_success));
								} else {
								showErrorDialog(getString(R.string.requesting_reset_password_error) + action.getErrorString());
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
				getString(R.string.requesting_reset_password), true);
		dialog.show();
		return dialog;
	}
	
	private boolean validate() {
		String username = tvUsername.getText().toString().trim();
		
		if (username.equals("")) {
			showErrorDialog(R.string.empty_username_error);
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
