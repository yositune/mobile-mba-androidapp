package com.samknows.measurement;

import com.samknows.measurement.FCCApplication;
import com.samknows.libcore.*;
import com.samknows.libcore.SKServiceDataCache.CachedValue;
import com.samknows.measurement.*;


import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

// Optionally, can use Mockito!
import static org.mockito.Mockito.*;

import static org.hamcrest.CoreMatchers.equalTo;

@RunWith(RobolectricTestRunner.class)
public class FirstTest {

//    public void testStringResource() throws Exception {
////        String hello = new MyActivity().getResources().getString(R.string.hello);
////        assertThat(hello, equalTo("Hello World, MyActivity!"));
//    }
    
    @Test
	public void testApplication() throws Exception{
    	
//		assertNull(Constants.PREF_KEY_USED_BYTES);
//		assertNull(Constants.PREF_DATA_CAP);
//		assertNull(Constants.PROP_TEST_START_WINDOW_RTC);

    	// Creating this, should populate the values in Constants...
		FCCApplication application = new FCCApplication();
		assertNotNull(application);
		
		assertEquals(SKConstants.PREF_KEY_USED_BYTES,  "used_bytes");
		assertEquals(SKConstants.PREF_DATA_CAP,  "data_cap_pref");
		assertEquals(SKConstants.PROP_TEST_START_WINDOW_RTC,  "test_start_window_in_millis_rtc");
	}
}
