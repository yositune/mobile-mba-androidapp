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
