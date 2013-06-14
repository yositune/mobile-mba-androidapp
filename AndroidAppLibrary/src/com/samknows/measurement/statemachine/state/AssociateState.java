package com.samknows.measurement.statemachine.state;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

import com.samknows.measurement.AppSettings;
import com.samknows.measurement.Logger;
import com.samknows.measurement.MainService;
import com.samknows.measurement.environment.PhoneIdentityDataCollector;
import com.samknows.measurement.net.AssociateAction;
import com.samknows.measurement.statemachine.StateResponseCode;
import com.samknows.measurement.util.OtherUtils;

public class AssociateState extends BaseState{

	public AssociateState(MainService c) {
		super(c);
	}

	@Override
	public StateResponseCode executeState() {
		if (!OtherUtils.isPhoneAssosiated(ctx)) {
			String unitId = AppSettings.getInstance().getUnitId();
			String imei = new PhoneIdentityDataCollector(ctx).collect().imei;
			AssociateAction action = new AssociateAction(unitId, imei); 
			action.execute();
			return action.isSuccess() ? StateResponseCode.OK : StateResponseCode.FAIL;
		} else {
			Logger.d(this, "already assosiated...skiping");
			return StateResponseCode.OK;
		}
	}

}
