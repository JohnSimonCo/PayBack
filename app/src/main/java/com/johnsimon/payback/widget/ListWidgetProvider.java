package com.johnsimon.payback.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.johnsimon.payback.R;
import com.johnsimon.payback.ui.FeedActivity;

public class ListWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context ctx, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int i = 0; i < appWidgetIds.length; i++) {
            Intent svcIntent = new Intent(ctx, ListWidgetService.class);

            svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews widget = new RemoteViews(ctx.getPackageName(), R.layout.widget_layout);

            widget.setRemoteAdapter(appWidgetIds[i], R.id.feed_list_item_master, svcIntent);

            Intent clickIntent = new Intent(ctx, FeedActivity.class);
            PendingIntent clickPI = PendingIntent
                    .getActivity(ctx, 0,
                            clickIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

            widget.setPendingIntentTemplate(R.id.feed_list_item_master, clickPI);

            appWidgetManager.updateAppWidget(appWidgetIds[i], widget);
        }

        super.onUpdate(ctx, appWidgetManager, appWidgetIds);
    }
}