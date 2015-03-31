package com.johnsimon.payback.util;

import android.content.Context;
import android.util.Log;

import com.johnsimon.payback.async.Callback;
import com.johnsimon.payback.async.Subscription;
import com.johnsimon.payback.data.AppData;
import com.johnsimon.payback.data.Debt;

public class AlarmScheduler {
	private Subscription<AppData> dataSubscription;
	private Context context;

	//TODO inte göra om hela processen varje ny aktivitetsstart
	public AlarmScheduler(Context context, Subscription<AppData> dataSubscription) {
		this.context = context;
		this.dataSubscription = dataSubscription;

		dataSubscription.listen(dataLoadedCallback);

	}

	public void die() {
		dataSubscription.unregister(dataLoadedCallback);
		//You're welcome, gc
		context = null;
	}

	private Callback<AppData> dataLoadedCallback = new Callback<AppData>() {
		@Override
		public void onCalled(AppData data) {
			for(Debt debt : data.debts) {
				if(debt.getRemindDate() != null) {
					Alarm.addAlarm(context, debt);
				}
			}
		}
	};
}