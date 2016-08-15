package com.geekband.musicplayer;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * Created by MirQ on 16/7/25.
 */
public class MusicAppWidgetProvider extends AppWidgetProvider {

    public static final int PLAY_OR_PAUSE_MUSIC_REQUEST_CODE = 111;
    public static final int NEXT_MUSIC_REQUEST_CODE = 222;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        final int N = appWidgetIds.length;

        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];

            //create an intent to launch the service
            Intent playIntent = new Intent(context, MusicService.class);
            PendingIntent playPendingIntent = PendingIntent.getService(context, PLAY_OR_PAUSE_MUSIC_REQUEST_CODE, playIntent, 0);

            Intent nextIntent = new Intent(context, MusicService.class);
            PendingIntent nextPendingIntent = PendingIntent.getService(context, NEXT_MUSIC_REQUEST_CODE, nextIntent, 0);

            //get the layout for app widget and attach an on-click listener to the button
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.music_appwidget_layout);
            remoteViews.setOnClickPendingIntent(R.id.play_button, playPendingIntent);
            remoteViews.setOnClickPendingIntent(R.id.skip_button, nextPendingIntent);

            // Tell the AppWWidgetManager to perform an update on current app widget
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }
}
