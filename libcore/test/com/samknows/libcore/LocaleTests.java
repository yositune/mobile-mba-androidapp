package com.samknows.libcore;

import java.util.Locale;

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
public class LocaleTests {

    @Test
    public void testDateFormatingOfDoubles() throws Exception{
    	
    	Locale usLocale = Locale.ENGLISH;
    	Locale.setDefault(usLocale);
    	
     	String theResult = SKCommon.sGetDecimalStringAnyLocaleAs1Pt5LocalisedString("1");
    	assertTrue(theResult.equals("1.00000"));
     	theResult = SKCommon.sGetDecimalStringAnyLocaleAs1Pt5LocalisedString("1.0");
    	assertTrue(theResult.equals("1.00000"));
     	theResult = SKCommon.sGetDecimalStringAnyLocaleAs1Pt5LocalisedString("1.123451");
    	assertTrue(theResult.equals("1.12345"));
     	theResult = SKCommon.sGetDecimalStringAnyLocaleAs1Pt5LocalisedString("1.123456");
    	assertTrue(theResult.equals("1.12346"));
     	theResult = SKCommon.sGetDecimalStringAnyLocaleAs1Pt5LocalisedString("1,0");
    	assertTrue(theResult.equals("1.00000"));
     	theResult = SKCommon.sGetDecimalStringAnyLocaleAs1Pt5LocalisedString("1,123451");
    	assertTrue(theResult.equals("1.12345"));
     	theResult = SKCommon.sGetDecimalStringAnyLocaleAs1Pt5LocalisedString("1,123456");
    	assertTrue(theResult.equals("1.12346"));
    			
    	Locale commaBasedLocale = Locale.GERMAN;
    	Locale.setDefault(commaBasedLocale);
    	
     	theResult = SKCommon.sGetDecimalStringAnyLocaleAs1Pt5LocalisedString("1");
    	assertTrue(theResult.equals("1,00000"));
     	theResult = SKCommon.sGetDecimalStringAnyLocaleAs1Pt5LocalisedString("1,0");
    	assertTrue(theResult.equals("1,00000"));
     	theResult = SKCommon.sGetDecimalStringAnyLocaleAs1Pt5LocalisedString("1.123451");
    	assertTrue(theResult.equals("1,12345"));
     	theResult = SKCommon.sGetDecimalStringAnyLocaleAs1Pt5LocalisedString("1.123456");
    	assertTrue(theResult.equals("1,12346"));
     	theResult = SKCommon.sGetDecimalStringAnyLocaleAs1Pt5LocalisedString("1,0");
    	assertTrue(theResult.equals("1,00000"));
     	theResult = SKCommon.sGetDecimalStringAnyLocaleAs1Pt5LocalisedString("1,123451");
    	assertTrue(theResult.equals("1,12345"));
     	theResult = SKCommon.sGetDecimalStringAnyLocaleAs1Pt5LocalisedString("1,123456");
    	assertTrue(theResult.equals("1,12346"));
    }
    
    @Test
    public void testParsingOfDecimalStrings() throws Exception{
    	
    	Locale usLocale = Locale.ENGLISH;
    	Locale.setDefault(usLocale);
    	
     	double theResult = SKCommon.sGetDecimalStringAnyLocaleAsDouble("1.2345");
    	assertTrue(theResult == 1.2345);
     	theResult = SKCommon.sGetDecimalStringAnyLocaleAsDouble("1,2345");
    	assertTrue(theResult == 1.2345);
    	
    	Locale commaBasedLocale = Locale.GERMAN;
    	Locale.setDefault(commaBasedLocale);
    	
     	theResult = SKCommon.sGetDecimalStringAnyLocaleAsDouble("1.2345");
    	assertTrue(theResult == 1.2345);
     	theResult = SKCommon.sGetDecimalStringAnyLocaleAsDouble("1,2345");
    	assertTrue(theResult == 1.2345);
    }
}
