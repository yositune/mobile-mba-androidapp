package com.samknows.measurement.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.samknows.measurement.R;
import com.samknows.measurement.activity.components.GPSPlace;
import com.samknows.measurement.activity.components.MapPopup;

import com.samknows.measurement.storage.DBHelper;
import com.samknows.measurement.storage.TestResult;
import com.samknows.measurement.util.SKDateFormat;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class SamKnowsMapActivity extends FragmentActivity implements OnInfoWindowClickListener {

	private MapController mapControll;
	private LatLng geoPoint = null;
	private MapView mapview;

	public static Context context;
	private DBHelper dbHelper;
	private int item_selected=0;

	@Override
	protected void onCreate(Bundle icicle) {
		// TODO Auto-generated method stub
		super.onCreate(icicle);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		context = getApplicationContext();
		setContentView(R.layout.map_layout);
		GoogleMap map = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();

		// map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		map.setMyLocationEnabled(true);

		dbHelper = new DBHelper(SamKnowsMapActivity.this);
		addMarkers();
	}

	public GPSPlace readArchiveItem(int i) {

		

		GeoPoint point;
		double lon = 0.0;
		double lat = 0.0;
		String allResults = "";

		JSONObject archive = dbHelper.getArchiveData(i);

		Log.d("", archive.toString());

		// read headers of json
		String datetime = "";
		String dtime_formatted;
		try {
			datetime = archive.getString("dtime");
			dtime_formatted = new SKDateFormat(this).UITime(Long.parseLong(datetime));

		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		String location;
		String testnumber;
		String hrresult;
		String success;
		JSONObject user = null;
		JSONArray results = null;
		int itemcount = 0;

		// unpack activemetrics

		try {
			results = archive.getJSONArray("activemetrics");
		} catch (JSONException e) {

			e.printStackTrace();
		}

		for (itemcount = 0; itemcount < results.length(); itemcount++) {
			location = "";
			testnumber = "";
			hrresult = "";
			success = "";
			user = null;
			try {
				user = results.getJSONObject(itemcount);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			try {
				testnumber = user.getString("test");
				location = user.getString("location");
				success = user.getString("success");
				if (success.equals("0")) {
					success = " Fail";
				}
				if (success.equals("1")) {
					success = "";
				}
				hrresult = user.getString("hrresult");

				if (testnumber.equals("" + TestResult.UPLOAD_TEST_ID)) {
					allResults = allResults + "\nU " + hrresult;
				}
				if (testnumber.equals("" + TestResult.DOWNLOAD_TEST_ID)) {
					allResults = allResults + "\nD " + hrresult;
				}

				if (testnumber.equals("" + TestResult.LATENCY_TEST_ID)) {
					allResults = allResults + "\nL " + hrresult;
				}

				if (testnumber.equals("" + TestResult.PACKETLOSS_TEST_ID)) {
					allResults = allResults + "\nP " + hrresult;
				}

				if (testnumber.equals("" + TestResult.JITTER_TEST_ID)) {
					allResults = allResults + "\nJ " + hrresult;
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		// unpack passivemetrics
		results = null;
		try {
			results = archive.getJSONArray("passivemetrics");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String metric;
		String value;
		String type;

		for (itemcount = 0; itemcount < results.length(); itemcount++) {
			metric = "";
			value = "";
			type = "";
			user = null;
			try {
				user = results.getJSONObject(itemcount);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			try {
				metric = user.getString("metric");
				value = user.getString("value");
				type = user.getString("type");

				if (metric.equals("latitude")) {
					lat = user.getDouble("value");
				}
				if (metric.equals("longitude")) {
					lon = user.getDouble("value");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

		

		GPSPlace GPSPlace_result = new GPSPlace();
		GPSPlace_result.geopoint = new LatLng(lat, lon);
		GPSPlace_result.result = allResults;
		return GPSPlace_result;

	}
	
	@Override 
    public void finish() { 

		Intent returnIntent = new Intent();
		
		
		if (item_selected==0)
		{

		}
		else
		{
			setResult(item_selected,returnIntent);
		}
        super.finish(); 
    
            
    } 

	public void addMarkers() {
		GoogleMap map = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();

		try {

			JSONObject jsonObject;
			String dtime_formatted = "";
			String result = "";

			for (int i = 0; i < 20; i++) {
				GPSPlace GPSPlace_result = readArchiveItem(i);
				geoPoint = GPSPlace_result.geopoint;
				result = GPSPlace_result.result;

				Bitmap bmpOriginal = BitmapFactory.decodeResource(
						this.getResources(), R.drawable.measurement_icon);
				Bitmap bmResult = Bitmap.createBitmap(bmpOriginal.getWidth(),
						bmpOriginal.getHeight(), Bitmap.Config.ARGB_8888);
				Canvas tempCanvas = new Canvas(bmResult);

				// tempCanvas.scale(0.5f,0.5f);
				tempCanvas.drawBitmap(bmpOriginal, 0, 0, null);
				int index=i+1;
				map.addMarker(new MarkerOptions().position(geoPoint)
						.snippet(result)
						.title(""+index)
						.icon(BitmapDescriptorFactory.fromBitmap(bmResult)));
				
				map.setInfoWindowAdapter(new MapPopup(getLayoutInflater()));
				
			    map.setOnInfoWindowClickListener(this);

				List<LatLng> points = new ArrayList<LatLng>();
				int totalPonts = 30; // number of corners of the pseudo-circle
				for (int c = 0; c < totalPonts; c++) {
					points.add(getPoint(geoPoint, 30, c * 2 * Math.PI
							/ totalPonts));
				}
				
				map.addPolygon(new PolygonOptions().addAll(points)
						.fillColor(0x0550A050).strokeWidth(2)
						.strokeColor(0x100A050));
						

			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		if (geoPoint != null){

			CameraPosition camPos = new CameraPosition.Builder().target(geoPoint)
					.zoom(18f).build();
	
			CameraUpdate camUpdate = CameraUpdateFactory.newCameraPosition(camPos);
			map.moveCamera(camUpdate);
		}
	}

	private LatLng getPoint(LatLng center, int radius, double angle) {
		// Get the coordinates of a circle point at the given angle
		double east = radius * Math.cos(angle);
		double north = radius * Math.sin(angle);

		double cLat = center.latitude;
		double cLng = center.longitude;
		double latRadius = 6371000 * Math.cos(cLat / 180 * Math.PI);

		double newLat = cLat + (north / 6371000 / Math.PI * 180);
		double newLng = cLng + (east / latRadius / Math.PI * 180);

		return new LatLng(newLat, newLng);
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		dialog(marker.getTitle());
	}
	
	public void dialog(final String title){
		new AlertDialog.Builder(this)
		.setMessage(getString(R.string.show_measurement))
		.setCancelable(true)
		.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						item_selected=Integer.parseInt( title );
						SamKnowsMapActivity.this
								.finish();
					}
				})
		.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).show();
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpTo(this,
					new Intent(this, SamKnowsAggregateStatViewerActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}