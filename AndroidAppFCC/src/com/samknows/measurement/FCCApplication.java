package com.samknows.measurement;

import com.samknows.libcore.SKConstants;
import com.samknows.measurement.R;
import com.samknows.measurement.SKApplication;

public class FCCApplication extends SKApplication {

	public FCCApplication() {
		super();
		
		SKConstants.RStringQuit = R.string.quit;
		SKConstants.RStringReallyQuit = R.string.really_quit;
		SKConstants.RStringYes = R.string.yes;
		SKConstants.RStringNoDialog = R.string.no_dialog;
		
		SKConstants.PREF_KEY_USED_BYTES = "used_bytes";
		SKConstants.PREF_DATA_CAP = "data_cap_pref";
		SKConstants.PROP_TEST_START_WINDOW_RTC = "test_start_window_in_millis_rtc";
	}

}
