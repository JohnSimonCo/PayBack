package com.johnsimon.payback.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class ListWidgetService extends RemoteViewsService {

    public final static String EXTRA_OPEN = "EXTRA_OPEN";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetViewsFactory(this.getApplicationContext(), intent);
    }
}