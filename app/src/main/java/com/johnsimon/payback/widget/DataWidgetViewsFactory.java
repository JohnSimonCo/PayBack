package com.johnsimon.payback.widget;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.RemoteViewsService;

import com.johnsimon.payback.async.Callback;
import com.johnsimon.payback.async.Notification;
import com.johnsimon.payback.async.NotificationCallback;
import com.johnsimon.payback.data.AppData;
import com.johnsimon.payback.data.DataLinker;
import com.johnsimon.payback.loader.ContactsLoader;
import com.johnsimon.payback.storage.Storage;
import com.johnsimon.payback.storage.StorageManager;

public abstract class DataWidgetViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    protected AppData data;
    protected Storage storage;
    protected Context ctx;

    Notification dataLink;

    protected DataWidgetViewsFactory(Context ctx) {
        this.ctx = ctx;
        storage = StorageManager.getStorage(ctx);

        ContactsLoader contactsLoader = new ContactsLoader();
        contactsLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ctx.getContentResolver());

        dataLink = new DataLinker().link(storage.subscription, contactsLoader.promise);
    }

    private Callback<AppData> dataLoadedCallback = new Callback<AppData>() {
        @Override
        public void onCalled(AppData _data) {
            data = _data;
            onDataReceived();
        }
    };

    private NotificationCallback dataLinkedCallback = new NotificationCallback() {
        @Override
        public void onNotify() {
            onDataLinked();
        }
    };

    protected abstract void onDataReceived();
    protected abstract void onDataLinked();

    @Override
    public void onCreate() {
        storage.subscription.listen(dataLoadedCallback);
        dataLink.listen(dataLinkedCallback);
    }

    @Override
    public void onDestroy() {
        storage.subscription.unregister(dataLoadedCallback);
        dataLink.unregister(dataLinkedCallback);
    }
}
