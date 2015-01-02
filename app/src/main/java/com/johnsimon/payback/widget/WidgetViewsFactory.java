package com.johnsimon.payback.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.johnsimon.payback.R;
import com.johnsimon.payback.core.Callback;
import com.johnsimon.payback.storage.LocalStorage;
import com.johnsimon.payback.storage.Storage;
import com.johnsimon.payback.util.AppData;

public class WidgetViewsFactory implements RemoteViewsService.RemoteViewsFactory, Callback<AppData> {

    private Context ctx = null;
    private AppData data;
    private Storage storage;

    public WidgetViewsFactory(Context ctx, Intent intent) {
        this.ctx = ctx;
        storage = new LocalStorage(ctx);
        storage.callbacks.add(this);
    }

    @Override
    public void onCreate() {
        // no-op
    }

    @Override
    public void onDestroy() {
        // no-op
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews row = new RemoteViews(ctx.getPackageName(), R.layout.feed_list_item);

        Intent intent = new Intent();
        Bundle extras = new Bundle();

        extras.putString(ListWidgetService.EXTRA_SEND, "bajs");
        intent.putExtras(extras);
        row.setOnClickFillInIntent(R.id.feed_list_item_master, intent);

        return(row);
    }

    @Override
    public RemoteViews getLoadingView() {
        return(null);
    }

    @Override
    public int getViewTypeCount() {
        return(1);
    }

    @Override
    public long getItemId(int position) {
        return(position);
    }

    @Override
    public boolean hasStableIds() {
        return(true);
    }

    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDataReceived(AppData data) {
        this.data = data;
    }
}