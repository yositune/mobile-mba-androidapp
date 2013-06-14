package com.samknows.measurement.activity;

import com.samknows.measurement.Constants;
import com.samknows.measurement.MainService;
import com.samknows.measurement.R;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SamKnowsInitialSettings extends BaseLogoutActivity {
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.init_settings);
		final TextView datacap = (TextView) findViewById(R.id.data_cap_init_input);

		long value = Long.valueOf(PreferenceManager.getDefaultSharedPreferences(
				this).getString(Constants.PREF_DATA_CAP, "-1"));
		if (value == -1) {
			value = Constants.DATA_CAP_DEFAULT_VALUE * 1024 * 1024;
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(SamKnowsInitialSettings.this);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(Constants.PREF_DATA_CAP,
					Constants.DATA_CAP_DEFAULT_VALUE + "");
			editor.commit();
			
		}
		datacap.setText((value / (1024 * 1024)) + "");

		Button btn_continue = (Button) findViewById(R.id.btn_continue);
		final Activity ctx = this;
		btn_continue.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				long new_datacap = 0;
				try {
					new_datacap = Long.valueOf(datacap.getText().toString());
				} catch (NumberFormatException nfe) {
					
				}
				if (new_datacap < Constants.DATA_CAP_MIN_VALUE ) {
					Toast t = Toast.makeText(ctx,
							getString(R.string.min_data_cap_message)
									+ " " +Constants.DATA_CAP_MIN_VALUE,
							Toast.LENGTH_SHORT);
					t.show();
					datacap.setText(Constants.DATA_CAP_MIN_VALUE + "");

				} else if (new_datacap > Constants.DATA_CAP_MAX_VALUE ) {
					Toast t = Toast.makeText(ctx,
							getString(R.string.max_data_cap_message)
									+ " " +Constants.DATA_CAP_MAX_VALUE,
							Toast.LENGTH_SHORT);
					t.show();
					datacap.setText(Constants.DATA_CAP_MAX_VALUE + "");
				} else {
					SharedPreferences settings = PreferenceManager
							.getDefaultSharedPreferences(SamKnowsInitialSettings.this);
					SharedPreferences.Editor editor = settings.edit();
					editor.putString(Constants.PREF_DATA_CAP,
							new_datacap + "");
					editor.commit();
					MainService.poke(ctx);
					Intent intent = new Intent(ctx, SamKnowsActivating.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
					ctx.startActivity(intent);
					ctx.finish();
				}
			}
		});
	}

	/*
	 * @Override public void afterTextChanged(Editable arg0) { mValue =
	 * Constants.DATA_CAP_DEFAULT_VALUE*1024*1024; try{ mValue =
	 * Integer.parseInt(arg0.toString())*1024*1024; }catch(NumberFormatException
	 * nfe){ arg0.replace(0, arg0.length(),
	 * Constants.DATA_CAP_DEFAULT_VALUE+""); } if(mValue <
	 * Constants.DATA_CAP_MIN_VALUE*1024*1024){ Toast t =
	 * Toast.makeText(this,getString
	 * (R.string.min_data_cap_message)+Constants.DATA_CAP_MIN_VALUE,
	 * Toast.LENGTH_SHORT); t.show(); arg0.replace(0,
	 * arg0.length(),Constants.DATA_CAP_MIN_VALUE+""); }else if(mValue >
	 * Constants.DATA_CAP_MAX_VALUE*1024*1024){ Toast t =
	 * Toast.makeText(this,getString
	 * (R.string.max_data_cap_message)+Constants.DATA_CAP_MAX_VALUE,
	 * Toast.LENGTH_SHORT); t.show(); arg0.replace(0, arg0.length(),
	 * Constants.DATA_CAP_MAX_VALUE+""); } }
	 * 
	 * @Override public void beforeTextChanged(CharSequence arg0, int arg1, int
	 * arg2, int arg3) {
	 * 
	 * }
	 * 
	 * @Override public void onTextChanged(CharSequence arg0, int arg1, int
	 * arg2, int arg3) {
	 * 
	 * }
	 */
}
