package com.samknows.measurement.activity;

import com.samknows.libcore.SKConstants;
import com.samknows.measurement.MainService;
import com.samknows.measurement.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SamKnowsInitialSettings extends BaseLogoutActivity {
	

	@SuppressLint("InlinedApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.init_settings);
		final TextView datacap = (TextView) findViewById(R.id.data_cap_init_input);

		long value = Long.valueOf(PreferenceManager.getDefaultSharedPreferences(
				this).getString(SKConstants.PREF_DATA_CAP, "-1"));
		if (value == -1) {
			value = SKConstants.DATA_CAP_DEFAULT_VALUE;
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(SamKnowsInitialSettings.this);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(SKConstants.PREF_DATA_CAP,
					SKConstants.DATA_CAP_DEFAULT_VALUE + "");
			editor.commit();
			
			
		}
		datacap.setText(value + "");

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
				if (new_datacap < SKConstants.DATA_CAP_MIN_VALUE ) {
					Toast t = Toast.makeText(ctx,
							getString(R.string.min_data_cap_message)
									+ " " +SKConstants.DATA_CAP_MIN_VALUE,
							Toast.LENGTH_SHORT);
					t.show();
					datacap.setText(SKConstants.DATA_CAP_MIN_VALUE + "");

				} else if (new_datacap > SKConstants.DATA_CAP_MAX_VALUE ) {
					Toast t = Toast.makeText(ctx,
							getString(R.string.max_data_cap_message)
									+ " " +SKConstants.DATA_CAP_MAX_VALUE,
							Toast.LENGTH_SHORT);
					t.show();
					datacap.setText(SKConstants.DATA_CAP_MAX_VALUE + "");
				} else {
					SharedPreferences settings = PreferenceManager
							.getDefaultSharedPreferences(SamKnowsInitialSettings.this);
					SharedPreferences.Editor editor = settings.edit();
					editor.putString(SKConstants.PREF_DATA_CAP,
							new_datacap + "");
					editor.commit();
					MainService.force_poke(ctx);
					Intent intent = new Intent(ctx, SamKnowsActivating.class);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TASK );
					}
					ctx.startActivity(intent);
					ctx.finish();
				}
			}
		});
	}

}
