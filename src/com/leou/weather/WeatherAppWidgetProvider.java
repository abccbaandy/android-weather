package com.leou.weather;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
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

		views.setImageViewResource(R.id.imageView, R.drawable.test);

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
			Log.d("TAG", "onResponse : " + response);
			Pattern pattern = Pattern
					.compile(MATCH_PATTERN);
			Matcher matcher = pattern.matcher(response);
			if (matcher.find()) {
				Log.d("TAG", "matcher : " + matcher.group());
				Picasso.with(context)
						.load(CWB_URL + matcher.group())
						.into(new Target2(views, appWidgetManager, appWidgetId));
			}
		}

	}

	private class Target2 implements Target {
		RemoteViews views;
		AppWidgetManager appWidgetManager;
		int appWidgetId;

		public Target2(RemoteViews views, AppWidgetManager appWidgetManager,
				int appWidgetId) {
			super();
			this.views = views;
			this.appWidgetManager = appWidgetManager;
			this.appWidgetId = appWidgetId;
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

			// draw circle
			canvas.drawCircle(50, 50, 7, paint);

			views.setImageViewBitmap(R.id.imageView, bitmap);

			appWidgetManager.updateAppWidget(appWidgetId, views);
		}

		@Override
		public void onPrepareLoad(Drawable arg0) {
			// TODO Auto-generated method stub

		}

	}

}
