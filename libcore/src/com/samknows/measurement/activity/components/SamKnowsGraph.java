package com.samknows.measurement.activity.components;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.samknows.libcore.SKCommon;
import com.samknows.libcore.SKLogger;
import com.samknows.measurement.util.SKDateFormat;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

public class SamKnowsGraph {
	private String TAG = SamKnowsGraph.class.getSimpleName();
	private ViewGroup containerViewCroup;
	private String json;
	private String date;
	private String tag="tag";

	//
	// achartengine (begin)
	//

	// http://stackoverflow.com/questions/8869854/how-to-implement-timechart-in-achartengine-with-android

    private TimeSeries mTimeSeries;


    private GraphicalView mGraphicalView = null;
    private TextView mCaptionView = null;
	
	XYMultipleSeriesRenderer multipleSeriesRenderer = null;
	
	private void createChartRendererSeriesAndView(Context context) {
		
        // The color values are from the iOS version...
        int areaColor = Color.argb(0xff,  0xb8,  0xd3,  0xe1);
        int areaEndColor = Color.argb(0xff,  0x6d,  0xad,  0xce);
        int lineColor = Color.argb(0xff,  0x2b,  0x6d,  0xa3);
        int gridLineColor = Color.argb((int)(255.0*8),  (int)(255*0.9),  (int)(255.0*0.9),  (int)(255.0*0.9));
		
        // Create the multiple-series renderer... you might have more than one series
        // plotted at once, if you wanted, through this API...
	    // XYMultipleSeriesRenderer multipleSeriesRenderer = new XYMultipleSeriesRenderer();
	    multipleSeriesRenderer = new XYMultipleSeriesRenderer();
        multipleSeriesRenderer.setApplyBackgroundColor(true);
        multipleSeriesRenderer.setBackgroundColor(Color.WHITE); // This is the graph BACKGROUND color
        multipleSeriesRenderer.setMarginsColor(Color.WHITE); // This is the color that SURROUNDS the graph
        multipleSeriesRenderer.setAntialiasing(true);
        multipleSeriesRenderer.setChartTitle("");
        multipleSeriesRenderer.setClickEnabled(false);
        multipleSeriesRenderer.setAxesColor(Color.BLACK);
        multipleSeriesRenderer.setLabelsColor(Color.BLACK);
        multipleSeriesRenderer.setXLabelsColor(Color.BLACK);
        multipleSeriesRenderer.setYLabelsColor(0, Color.BLACK); // 0 is the scale - a mystery value!
        multipleSeriesRenderer.setAxesColor(Color.TRANSPARENT);
        multipleSeriesRenderer.setYLabelsPadding(5);
        multipleSeriesRenderer.setYLabelsAlign(Paint.Align.RIGHT);
       
        // Needs to be fairly, to cater for e.g. 0.008 type values!
        multipleSeriesRenderer.setLabelsTextSize(12);
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(3);
        multipleSeriesRenderer.setLabelFormat(format);
        //multipleSeriesRenderer.setChartValuesFormat(format);
        
        multipleSeriesRenderer.setLegendTextSize(15);
        multipleSeriesRenderer.setPanEnabled(false);
        //multipleSeriesRenderer.setPointSize(3f);
        multipleSeriesRenderer.setShowAxes(true);
        multipleSeriesRenderer.setShowGridX(false);
        multipleSeriesRenderer.setShowGridY(true);
        multipleSeriesRenderer.setGridColor(gridLineColor);
        multipleSeriesRenderer.setShowLegend(false);
        multipleSeriesRenderer.setShowLabels(true);
        multipleSeriesRenderer.setZoomButtonsVisible(false);
        multipleSeriesRenderer.setZoomEnabled(false);
        multipleSeriesRenderer.setAxisTitleTextSize(16);
        multipleSeriesRenderer.setMargins(new int[]{20,40,15,5}); // Pixels: top/left/bottom/right
        multipleSeriesRenderer.setChartTitleTextSize(20);
        
        // The next two lines prevent the Y axis zero from being suppressed!
        multipleSeriesRenderer.setYAxisMin(0.0);
        multipleSeriesRenderer.setYAxisMax(this.corePlotMaxValue);
        
        //multipleSeriesRenderer.setYTitle(mYAxisTitle);
        //multipleSeriesRenderer.setSelectableBuffer(20);
        
        // Create the series renderer - we have just one of these.
        XYSeriesRenderer seriesRenderer = new XYSeriesRenderer();
        seriesRenderer.setColor(lineColor);
        // Tell the line to fill below!
        FillOutsideLine fillOutsideLine = new FillOutsideLine(XYSeriesRenderer.FillOutsideLine.Type.BELOW);
        fillOutsideLine.setColor(areaEndColor);
        seriesRenderer.addFillOutsideLine(fillOutsideLine);

        // Add the series renderer to the multiple series renderer...
        multipleSeriesRenderer.addSeriesRenderer(seriesRenderer);
      
        // Create a series, and it it to the dataset...
        mTimeSeries = new TimeSeries("");
       
        // Create a dataset, and add the series to it...
	    XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(mTimeSeries);

        
        // Finally: create the chart graphical view!
        mGraphicalView = ChartFactory.getTimeChartView(context, dataset, multipleSeriesRenderer, SKDateFormat.sGetGraphDateFormat(context));

        // Now that we have the chart, we'll be able to add-in some data...
        // and add to the ViewGroup... straight after this call!
    }

    //
    // Values extracted from the JSON data!
    //
	JSONObject jsonData = null;
	ArrayList<Double> mpCorePlotDataPoints;
	ArrayList<Date> mpCorePlotDates;
	double corePlotMinValue;
	double corePlotMaxValue;
	String mYAxisTitle = "Mbps";
	
	// For Mock Testing...
	public ArrayList<Double> getCorePlotDataPoints() {
		return mpCorePlotDataPoints;
	}
	
	// For Mock Testing...
	public ArrayList<Date> getCorePlotDates() {
		return mpCorePlotDates;
	}
	
	private void extractCorePlotData() {
		
		ArrayList<Double> theNewArray = new ArrayList<Double>();
		ArrayList<Date> theDateArray =  new ArrayList<Date>();
		
		// The actually calculates the *average* values
		// for any given day.

		// The data points received are from the start day, to the end day.
		// However, values might not be present for any given day.
		// The way that the old graph system seemed to work, is to start from a zero value.
		// If a data point is missing, the value used is interpolated between the last received value,
		// and the next value; if no value has yet been seen, then the value remains at zero.

		// Example JSON value array...
		// Note that the values supplied are not neccessarily time-ordered!!
        //	{
        //			"start_date":"1381499507861",
        //			"end_date":"1382104307862",
        //			"results":[{"value":"0.979312","datetime":"1382099660000"},{"value":"3.570656","datetime":"1382099068000"}],
        //			"type":0,
        //			"y_label":"Mbps"
        //	}
		
		//Log.d(getClass().getName(), "jsonData=" + jsonData.toString());
		
		try {
			String theStartDateString = jsonData.getString("start_date");
			String theEndDateString = jsonData.getString("end_date");
			mYAxisTitle = jsonData.getString("y_label");

			Date theStartDate = new Date((Long.valueOf(theStartDateString)));
			Date theEndDate = new Date((Long.valueOf(theEndDateString)));

			long daysBetween = (Long.valueOf(theEndDateString) - Long.valueOf(theStartDateString)) / (1000L * 60L * 60L * 24L);
			daysBetween++;

	     	//Log.d(getClass().getName(), "daysBetween=" + daysBetween);
		
			theNewArray.ensureCapacity((int) daysBetween);
			theDateArray.ensureCapacity((int) daysBetween);
			
     		ArrayList<Integer> theCountArray =  new ArrayList<Integer>();
			
			for (int i = 0; i < (int)daysBetween; i++) {
				theNewArray.add(null);
				theCountArray.add(1);
				
				Calendar cTheCalendar = Calendar.getInstance();
				cTheCalendar.setTime(theStartDate);
				cTheCalendar.set(Calendar.HOUR_OF_DAY, 0);
				cTheCalendar.set(Calendar.MINUTE, 0);
				cTheCalendar.set(Calendar.SECOND, 0);
				cTheCalendar.set(Calendar.MILLISECOND, 0);
				cTheCalendar.add(Calendar.DAY_OF_MONTH,  i);
				
				theDateArray.add(i, cTheCalendar.getTime());
			}

			// We MUST INCLUDE the actual end date.
			// For example, for one week: start date might be 01 Aug, end date might be 08 Aug ...
			// 01 02 03 04 05 06 07 08
			//   ^  ^  ^  ^  ^  ^  ^   = 7 days!!!

			// TODO - how do we get the test name?! "tests"

			JSONArray theResults = jsonData.getJSONArray("results");

			Calendar cForStartDate = Calendar.getInstance();
			cForStartDate.setTime(theStartDate);
			// Round-off the time data...
			// http://stackoverflow.com/questions/1908387/java-date-cut-off-time-information
			cForStartDate.set(Calendar.HOUR_OF_DAY, 0);
			cForStartDate.set(Calendar.MINUTE, 0);
			cForStartDate.set(Calendar.SECOND, 0);
			cForStartDate.set(Calendar.MILLISECOND, 0);
				
			int lItems = theResults.length();
			int lIndex = 0;
			for (lIndex = 0; lIndex < lItems; lIndex++) {
				JSONObject item = theResults.getJSONObject(lIndex);
				String value = item.getString("value");
				String datetime = item.getString("datetime");

				// Which day does this correspond to?!
				Calendar c = Calendar.getInstance();
				long milliseconds = Long.valueOf(datetime);
   			    Date theDate = new Date(milliseconds);
				c.setTime(theDate);
				// Round-off the time data...
				// http://stackoverflow.com/questions/1908387/java-date-cut-off-time-information
				c.set(Calendar.HOUR_OF_DAY, 0);
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.SECOND, 0);
				c.set(Calendar.MILLISECOND, 0);
				
				Date theCDate = c.getTime();
				
				int dayIndex = 0;
				
//				boolean bCheckDateDayMatch = false;
//				int checkIndex = 0;
//				for (checkIndex = 0; checkIndex < daysBetween; checkIndex++) {
//					if (theCDate.compareTo(theDateArray.get(checkIndex)) == 0) {
//						bCheckDateDayMatch = true;
//						dayIndex = checkIndex;
//						break;
//					}
//				}
//				
//				if (bCheckDateDayMatch) {
//         	     	  Log.d(getClass().getName(), "Found date day match ("+dayIndex+")");
//				} else {
//       	     	    Log.e(getClass().getName(), "ERROR: did NOT find date/day match");
    				long diff = c.getTimeInMillis() - cForStartDate.getTimeInMillis();
    	            float fDayIndex = (float) diff / (24 * 60 * 60 * 1000);
    				dayIndex = (int)fDayIndex;
//				}
				
				//SimpleDateFormat format = new SimpleDateFormat("dd/MM");
     	     	//Log.d(getClass().getName(), "Read date for dayIndex=["+checkIndex+"], =" + format.format(c.getTime()) + ", value=" + value);

    			// Get the double value safely, irrespective of Locale
    		    double doubleValue = SKCommon.sGetDecimalStringAnyLocaleAsDouble(value);
    		    
    		    if (theNewArray.get(dayIndex) == null) {
    		    	// Nothing there yet!
    				theNewArray.set((int) dayIndex, doubleValue);
    				theCountArray.set((int) dayIndex, 1);
    		    } else {
    		    	// Something there already found for this day - add up, and we convert to an average in a moment...
    		    	double currentTotal = theNewArray.get(dayIndex);
    		    	theNewArray.set((int) dayIndex, currentTotal + doubleValue);
    				theCountArray.set((int) dayIndex, 1 + theCountArray.get(dayIndex));
    		    }
			}
			
			// Now, fix the values back to the averages...
			for (lIndex = 0; lIndex < daysBetween; lIndex++) {
				if (theCountArray.get(lIndex) > 1) {
					if (theNewArray.get(lIndex) != null) {
						theNewArray.set(lIndex,  (theNewArray.get(lIndex) / (double)theCountArray.get(lIndex)));
					}
				}
        // achartengine displays weird values if they are too small...
				if (theNewArray.get(lIndex) != null) {
					if (theNewArray.get(lIndex) < 0.01) {
						theNewArray.set(lIndex,  0.01);
					}
				}
			}

			// To reach here, we have an array of items...
			// We must now interpolate!
			int theLastNonNilNumberAtIndex = -1;
			lItems = theNewArray.size();

			// To reach here, we have an array of items...
			// We must now interpolate!
			for (lIndex = 0; lIndex < daysBetween; lIndex++) {

				Double theObject = theNewArray.get(lIndex);
				if (theObject == null) {
					// This is our PLACEHOLDER!
					if (theLastNonNilNumberAtIndex == -1) {
						// Nothing we can do here!
						continue;
					}

					Double theNumberAtLastNonNilIndex = theNewArray.get(theLastNonNilNumberAtIndex);

					// Interpolate. Look FORWARD to the next number!
					// If none found, then simply copy forward.
					boolean bLookForwardFound = false;

					int lLookForwardIndex;
					for (lLookForwardIndex = lIndex + 1; ; lLookForwardIndex++) {
						if (lLookForwardIndex >= lItems)
						{
							break;
						}

						Double theLookForwardObject = theNewArray.get(lLookForwardIndex);
						if (theLookForwardObject != null) {
							Double theLookForwardNumber = theLookForwardObject;
							bLookForwardFound = true;

							// Calculate the value to use!
							double theDecimalLookForward = theLookForwardNumber;
							double theDecimalNumberAtLastNonNilIndex = theNumberAtLastNonNilIndex;
							double theInterpolatedValue = theDecimalNumberAtLastNonNilIndex + (theDecimalLookForward - theDecimalNumberAtLastNonNilIndex) * ((double)(lIndex - theLastNonNilNumberAtIndex)) / ((double)(lLookForwardIndex - theLastNonNilNumberAtIndex));
							theNewArray.set(lIndex, theInterpolatedValue);
							break;
						}
					}

					if (bLookForwardFound == false) {
						theNewArray.set(lIndex, theNewArray.get(theLastNonNilNumberAtIndex));
					}

				} else {
					theLastNonNilNumberAtIndex = lIndex;
				}

			}

			// Finally, find the minimum and maximum values, for scaling the plot!
			corePlotMinValue = 0.0;
			//  bool bMinFound = false;
			corePlotMaxValue = 0.0;
			boolean bMaxFound = false;


			for (lIndex = 0; lIndex < lItems; lIndex++) {
				Double theObject = theNewArray.get(lIndex);
				if (theObject != null) {
					Double theNumber = theObject;
					double theDouble = theNumber;
					if (bMaxFound == false) {
						corePlotMaxValue = theDouble;
						bMaxFound = true;
					}

					if (theDouble > corePlotMaxValue) {
						corePlotMaxValue = theDouble;
						bMaxFound = true;
					}
				}
			}
			
			//Log.d(this.getClass().getName(), "All values extracted!");

		} catch (JSONException e) {
			SKLogger.sAssert(getClass(), false);
		} catch (NullPointerException e) {
			SKLogger.sAssert(getClass(), false);
		}

		mpCorePlotDataPoints = theNewArray;
		mpCorePlotDates = theDateArray;
	}


	private void addCorePlotDataToGraphicalView() {
        
		int lItems = mpCorePlotDataPoints.size();
		
		// If week period, force more label items...
        if (lItems == 8) {
          multipleSeriesRenderer.setXLabels(lItems);
        } else if (multipleSeriesRenderer.getXLabels() < 5) {
          multipleSeriesRenderer.setXLabels(5);
        }
        
		int lIndex = 0;
		for (lIndex = 0; lIndex < lItems; lIndex++) {
			double theDouble = 0.0;
			Double theValue = mpCorePlotDataPoints.get(lIndex);
			if (theValue != null) {
				theDouble = theValue;
			}
			//mCurrentSeries.add(lIndex, theDouble);
			mTimeSeries.add(mpCorePlotDates.get(lIndex), theDouble);
		}
	}
    
    private void attachAchartEngine(Context context, ViewGroup inContainerViewGroup) {
    	
    	// If the chart already exists, remove it!
    	if (mGraphicalView != null) {
    		inContainerViewGroup.removeView(mGraphicalView);
    	}
    
    	// Extract the query data from the jsonData!
      	extractCorePlotData();
    	
        // We can now create and add the chart!
        createChartRendererSeriesAndView(context);
        
        // And now put the data in the chart!
        addCorePlotDataToGraphicalView();
        
        //
        // Finally - add the new chart to our view group!
        //
        inContainerViewGroup.addView(mGraphicalView);
    }
	
	//
	// achartengine (end)
	//
   
    //
    // Update the graph, and set the caption text...
    //
    
    private void updateGraphAndCaption(Context context, ViewGroup inContainerViewGroup) {
    	
    	// Update the graph...
    	attachAchartEngine(context, inContainerViewGroup);
     
    	// And update the caption text... ensuring that it appears in bold.
    	// For some reason, the Html.fromHtml... approach is required for this to work
    	// reliably!
    	// http://stackoverflow.com/questions/1529068/is-it-possible-to-have-multiple-styles-inside-a-textview
        // mCaptionView.setText(mYAxisTitle);
        mCaptionView.setText(Html.fromHtml("<b>" + mYAxisTitle + "</b>"));
    }
    
    Context mContext = null;
	
	public SamKnowsGraph(Context context, ViewGroup inViewGroup, TextView inCaptionView, String inTag){
		mContext = context;
		
		containerViewCroup = inViewGroup;
		
		if (inViewGroup.getClass() == WebView.class){
			// If we're embedded in a web view - ensure the scrollbars are disabled,
			// as they flash-up momentarily and are unsightly!
			((WebView)inViewGroup).setVerticalScrollBarEnabled(false);
			((WebView)inViewGroup).setHorizontalScrollBarEnabled(false);
		}
		
        // Make the Y Axis Label appear manually - the one build into the library always
        // appears vertically, so we use our own, appearing horizontally in the top-left corner
        mCaptionView = inCaptionView;
		
		this.tag=inTag;
	}

	
	/**
	 * Set the data to display
	 * 
	 * @param data JSONObject
	 */
	public void setData(JSONObject data){
    	
		Log.v(TAG, "setData()");
		json = data.toString();
		jsonData = data;
	
		// Attach a new, updated graph!
		updateGraphAndCaption(mContext, containerViewCroup);
	}
	
	/** 
	 * Re-create/update the graph!
	 */
	public void update(){
    	
		// Attach a new, updated graph!
		updateGraphAndCaption(mContext, containerViewCroup);
	}
	
	/**
	 * Return the data in a json string
	 * @return
	 */
	public String getData(){
		Log.v(TAG, "getData()");
		//Log.v(TAG, json);
		return json;
	}
	
	/**
	 * Return the date as a string
	 */
	public String getStartDate(){
		Log.v(TAG, "getStartDate()");
		return (String) date;
	}
}

