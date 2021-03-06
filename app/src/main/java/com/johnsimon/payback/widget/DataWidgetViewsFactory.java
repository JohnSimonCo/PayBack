package com.johnsimon.payback.widget;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.RemoteViewsService;

import com.johnsimon.payback.async.Callback;
import com.johnsimon.payback.async.Notification;
import com.johnsimon.payback.async.NotificationCallback;
import com.johnsimon.payback.core.DataContextInterface;
import com.johnsimon.payback.data.AppData;
import com.johnsimon.payback.data.DataLinker;
import com.johnsimon.payback.loader.ContactsLoader;
import com.johnsimon.payback.storage.Storage;
import com.johnsimon.payback.storage.StorageManager;
import com.johnsimon.payback.util.Alarm;
import com.johnsimon.payback.util.AlarmScheduler;

public abstract class DataWidgetViewsFactory implements DataContextInterface, RemoteViewsService.RemoteViewsFactory {

    protected AppData data;
    protected Storage storage;
    protected Context context;

	private DataLinker dataLinker;
	private AlarmScheduler alarmScheduler;

    protected DataWidgetViewsFactory(Context context) {
        this.context = context;
        storage = StorageManager.getStorage(context);

        ContactsLoader contactsLoader = new ContactsLoader();
        contactsLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, context.getContentResolver());

		dataLinker = new DataLinker(storage.subscription, contactsLoader.promise);

		alarmScheduler = new AlarmScheduler(context, storage.subscription);
	}

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public AppData getData() {
        return data;
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
		dataLinker.linked.listen(dataLinkedCallback);
    }

    @Override
    public void onDestroy() {
        storage.subscription.unregister(dataLoadedCallback);
		dataLinker.die();
		alarmScheduler.die();
    }
}
