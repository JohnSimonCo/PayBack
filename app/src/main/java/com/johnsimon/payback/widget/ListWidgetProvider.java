package com.johnsimon.payback.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.johnsimon.payback.R;
import com.johnsimon.payback.ui.CreateDebtActivity;
import com.johnsimon.payback.ui.FeedActivity;

public class ListWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int i = 0; i < appWidgetIds.length; i++) {
            Intent svcIntent = new Intent(context, ListWidgetService.class);

            svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            widget.setRemoteAdapter(R.id.widget_list, svcIntent);

            Intent clickIntent = new Intent(context, FeedActivity.class);
            PendingIntent clickPI = PendingIntent.getActivity(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent homeIntent = new Intent(context, FeedActivity.class);
            PendingIntent homePI = PendingIntent.getActivity(context, 0, homeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent addIntent = new Intent(context, CreateDebtActivity.class);
            addIntent.putExtra(CreateDebtActivity.KEY_NO_FAB_ANIM, true);
            addIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent addPI = PendingIntent.getActivity(context, 0, addIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            widget.setPendingIntentTemplate(R.id.feed_list_item_master, clickPI);

            widget.setOnClickPendingIntent(R.id.widget_layout_title, homePI);
            widget.setOnClickPendingIntent(R.id.widget_layout_add, addPI);

            appWidgetManager.updateAppWidget(appWidgetIds[i], widget);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}