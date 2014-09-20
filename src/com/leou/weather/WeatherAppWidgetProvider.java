package com.leou.weather;

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

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;

public class WeatherAppWidgetProvider extends AppWidgetProvider {

	public static final String WIDGET_IDS_KEY = "mywidgetproviderwidgetids";
	public static final String WIDGET_DATA_KEY = "mywidgetproviderwidgetdata";

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
		RemoteViews views = new RemoteViews("com.leou.weather",
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
			Picasso.with(context)
					.load("http://www.cwb.gov.tw/V7/observe/radar/Data/MOS2_1024N/2014-09-20_2324.2MOS3NC.jpg")
					.into(new Target2(views, appWidgetManager, appWidgetId));

			appWidgetManager.updateAppWidget(appWidgetId, views);
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

			// not important
			views.setTextViewText(R.id.HelloTextView01, "haha");

			appWidgetManager.updateAppWidget(appWidgetId, views);
		}

		@Override
		public void onPrepareLoad(Drawable arg0) {
			// TODO Auto-generated method stub

		}

	}

}
