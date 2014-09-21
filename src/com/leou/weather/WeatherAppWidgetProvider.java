package com.leou.weather;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;

public class WeatherAppWidgetProvider extends AppWidgetProvider {

	static final String WIDGET_IDS_KEY = "mywidgetproviderwidgetids";
	static final String WIDGET_DATA_KEY = "mywidgetproviderwidgetdata";
	static final String MOS2_1024N = "http://www.cwb.gov.tw/V7/js/MOS2_1024N.js";
	static final String CWB_URL = "http://www.cwb.gov.tw";
	static final String MATCH_PATTERN = "/V7/observe/radar/Data/MOS2_1024N/\\d{4}-\\d{2}-\\d{2}_\\d{4}.2MOS3NC.jpg";

	@Override
	public void onAppWidgetOptionsChanged(Context context,
			AppWidgetManager appWidgetManager, int appWidgetId,
			Bundle newOptions) {
		// TODO Auto-generated method stub
		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId,
				newOptions);
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		// TODO Auto-generated method stub
		super.onDeleted(context, appWidgetIds);
	}

	@Override
	public void onDisabled(Context context) {
		// TODO Auto-generated method stub
		super.onDisabled(context);
	}

	@Override
	public void onEnabled(Context context) {
		// TODO Auto-generated method stub
		Log.d("TAG", "onEnabled");
		super.onEnabled(context);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.d("TAG", "onReceive");
		super.onReceive(context, intent);
		Log.d("TAG", "onReceive : " + intent.hasExtra(WIDGET_IDS_KEY));
		if (intent.hasExtra(WIDGET_IDS_KEY)) {
			int[] ids = intent.getExtras().getIntArray(WIDGET_IDS_KEY);
			// if (intent.hasExtra(WIDGET_DATA_KEY)) {
			// Object data = intent.getExtras().getParcelable(WIDGET_DATA_KEY);
			// this.update(context, AppWidgetManager.getInstance(context),
			// ids, data);
			// } else {
			this.onUpdate(context, AppWidgetManager.getInstance(context), ids);
			// }
		}
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		// TODO Auto-generated method stub
		Log.d("TAG", "onUpdate");
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.weather_appwidget);

		views.setImageViewResource(R.id.imageView, R.drawable.clickhere);

		// set onClkListener
		Intent updateIntent = new Intent();
		updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		updateIntent.putExtra(WIDGET_IDS_KEY, appWidgetIds);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
				updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.imageView, pendingIntent);
		views.setOnClickPendingIntent(R.id.updateBtn, pendingIntent);

		for (int i = 0; i < appWidgetIds.length; i++) {
			int appWidgetId = appWidgetIds[i];

			// set gps listener
			LocationManager locationManager = (LocationManager) context
					.getSystemService(Context.LOCATION_SERVICE);
			LocationListener locationListener = new LocationListener2(views,
					appWidgetManager, appWidgetId, context);
			locationManager.requestLocationUpdates(
					locationManager.getBestProvider(new Criteria(), true), 0,
					0, locationListener);

			// get last gps data
			Location location = locationManager
					.getLastKnownLocation(locationManager.getBestProvider(
							new Criteria(), true));
			double longitude = location.getLongitude();
			double latitude = location.getLatitude();
			Log.d("TAG getLastKnownLocation=", "X=" + longitude + ", Y="
					+ latitude);

			// calc longitude and latitude to x and y
			// longitude 120.0 - 121.998
			// latitude 23.474 - 25.47 & 21.874 - 23.87 = 21.874 - 25.47
			// two image(N&S) overlay (23.87-23.474)/(25.47-23.474)*500=99.198
			longitude = (longitude - 120.0) / (121.998 - 120.0) * (500 - 1) + 1
					- 16;
			latitude = 500 + 401 - (latitude - 21.874) / (25.47 - 21.874)
					* (500 + 401 - 1) + 1;
			Log.d("TAG getLastKnownLocation calc =", "X=" + longitude + ", Y="
					+ latitude);

			// save to SharedPreferences
			SharedPreferences prefs = context.getSharedPreferences("SETTINGS",
					0);
			Editor edit = prefs.edit();
			edit.putInt("x", (int) longitude);
			edit.putInt("y", (int) latitude);
			edit.commit();

			// get url list
			RequestQueue mQueue = Volley.newRequestQueue(context);
			StringRequest stringRequest = new StringRequest(MOS2_1024N,
					new ResponseListener2(views, appWidgetManager, appWidgetId,
							context), new Response.ErrorListener() {

						@Override
						public void onErrorResponse(VolleyError arg0) {
							// TODO Auto-generated method stub
							Log.e("TAG", arg0.getMessage(), arg0);
						}
					});
			mQueue.add(stringRequest);

			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}

	private class LocationListener2 implements LocationListener {
		RemoteViews views;
		AppWidgetManager appWidgetManager;
		int appWidgetId;
		Context context;

		public LocationListener2(RemoteViews views,
				AppWidgetManager appWidgetManager, int appWidgetId,
				Context context) {
			super();
			this.views = views;
			this.appWidgetManager = appWidgetManager;
			this.appWidgetId = appWidgetId;
			this.context = context;
		}

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			double longitude = location.getLongitude();
			double latitude = location.getLatitude();
			Log.d("TAG Location=", "X=" + longitude + ", Y=" + latitude);

			// calc longitude and latitude to x and y
			// longitude 120.0 - 121.998
			// latitude 23.474 - 25.47 & 21.874 - 23.87 = 21.874 - 25.47
			// two image(N&S) overlay (23.87-23.474)/(25.47-23.474)*500=99.198
			longitude = (longitude - 120.0) / (121.998 - 120.0) * (500 - 1) + 1
					- 16;
			latitude = 500 + 401 - (latitude - 21.874) / (25.47 - 21.874)
					* (500 + 401 - 1) + 1;
			Log.d("TAG Location calc =", "X=" + longitude + ", Y=" + latitude);

			// save to SharedPreferences
			SharedPreferences prefs = context.getSharedPreferences("SETTINGS",
					0);
			Editor edit = prefs.edit();
			edit.putInt("x", (int) longitude);
			edit.putInt("y", (int) latitude);
			edit.commit();

			// int[] ids = intent.getExtras().getIntArray(WIDGET_IDS_KEY);
			// onUpdate(context, AppWidgetManager.getInstance(context),
			// appWidgetId);
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}

	}

	private class ResponseListener2 implements Response.Listener<String> {
		RemoteViews views;
		AppWidgetManager appWidgetManager;
		int appWidgetId;
		Context context;

		public ResponseListener2(RemoteViews views,
				AppWidgetManager appWidgetManager, int appWidgetId,
				Context context) {
			super();
			this.views = views;
			this.appWidgetManager = appWidgetManager;
			this.appWidgetId = appWidgetId;
			this.context = context;
		}

		@Override
		public void onResponse(String response) {
			// TODO Auto-generated method stub
			// Log.d("TAG", "onResponse : " + response);
			Pattern pattern = Pattern.compile(MATCH_PATTERN);
			Matcher matcher = pattern.matcher(response);
			if (matcher.find()) {
				Log.d("TAG", "matcher : " + matcher.group());
				Picasso.with(context)
						.load(CWB_URL + matcher.group())
						.placeholder(R.drawable.lodingimg)
						.into(new Target2(views, appWidgetManager, appWidgetId,
								context));
			}
		}

	}

	private class Target2 implements Target {
		RemoteViews views;
		AppWidgetManager appWidgetManager;
		int appWidgetId;
		Context context;

		public Target2(RemoteViews views, AppWidgetManager appWidgetManager,
				int appWidgetId, Context context) {
			super();
			this.views = views;
			this.appWidgetManager = appWidgetManager;
			this.appWidgetId = appWidgetId;
			this.context = context;
		}

		@Override
		public void onBitmapFailed(Drawable arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onBitmapLoaded(Bitmap arg0, LoadedFrom arg1) {
			// TODO Auto-generated method stub
			Log.d("TAG", "onBitmapLoaded : " + arg0.getHeight());
			Bitmap bitmap = arg0.copy(Config.ARGB_8888, true);
			Canvas canvas = new Canvas(bitmap);
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setColor(Color.RED);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(2);

			// get longitude and latitude
			SharedPreferences prefs = context.getSharedPreferences("SETTINGS",
					0);
			int x = prefs.getInt("x", 0);
			int y = prefs.getInt("y", 0);

			// draw icon
			Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.arrow);
			canvas.drawBitmap(icon, x, y, paint);

			// draw circle
			// canvas.drawCircle(x, y, 7, paint);

			Log.d("TAG drawCircle : ", "X=" + x + ", Y=" + y);

			views.setImageViewBitmap(R.id.imageView, bitmap);

			appWidgetManager.updateAppWidget(appWidgetId, views);
		}

		@Override
		public void onPrepareLoad(Drawable arg0) {
			// TODO Auto-generated method stub

		}

	}

}
