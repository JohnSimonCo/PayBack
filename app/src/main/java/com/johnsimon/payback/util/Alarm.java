package com.johnsimon.payback.util;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.johnsimon.payback.R;
import com.johnsimon.payback.async.Callback;
import com.johnsimon.payback.data.AppData;
import com.johnsimon.payback.data.Debt;
import com.johnsimon.payback.storage.Storage;
import com.johnsimon.payback.storage.StorageManager;
import com.johnsimon.payback.ui.FeedActivity;
import com.johnsimon.payback.ui.RemindLaterActivity;

import java.util.UUID;

public class Alarm  {

    public final static String ALARM_ID = "ALARM_ID";

    public static void addAlarm(Context context, Debt debt) {
		Intent intentAlarm = new Intent(context, AlarmReceiver.class);
        intentAlarm.putExtra(ALARM_ID, debt.getId());

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		alarmManager.set(AlarmManager.RTC_WAKEUP, debt.getRemindDate(), PendingIntent.getBroadcast(context, debt.getIntegerId(), intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
		//alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 3000, PendingIntent.getBroadcast(context, debt.getIntegerId(), intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
    }

	/*
    public static boolean hasAlarm (Context context, Debt debt) {
        Intent intentAlarm = new Intent(context, AlarmReceiver.class);
        intentAlarm.putExtra(ALARM_ID, debt.id);

		return PendingIntent.getBroadcast(context, debt.getIntegerId(), intentAlarm, PendingIntent.FLAG_NO_CREATE) != null;
    }*/

    public static void cancelAlarm(Context context, Debt debt) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(ALARM_ID, debt.id);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, debt.getIntegerId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        pendingIntent.cancel();

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.cancel(pendingIntent);
    }
    public static void cancelNotification(Context context, Debt debt) {
        cancelNotification(context, debt.id);
    }
    public static void cancelNotification(Context context, UUID id) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id.hashCode());
    }

	public static class AlarmReceiver extends BroadcastReceiver {

        private Context context;
        private Intent intent;

		Storage storage;

		public AlarmReceiver() {
		}

        @Override
        public void onReceive(final Context context, Intent intent) {

            this.context = context;
            this.intent = intent;

			storage = StorageManager.getStorage(context);
			storage.subscription.listen(dataLoadedCallback);
        }

        private Callback<AppData> dataLoadedCallback = new Callback<AppData>() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onCalled(AppData data) {
                UUID id = (UUID) intent.getExtras().get(ALARM_ID);
                Debt debt = data.findDebt(id);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_stat_negative)
                        .setContentTitle(context.getString(R.string.notif_pay_back_reminder))
                        .setContentText(getContentText(debt, data))
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setColor(context.getResources().getColor(R.color.icon_green))
                        .addAction(getPayBackAction(id))
                        .addAction(getRemindLaterAction(id))
                        .setContentIntent(getDetailPendingIntent(id));

                if (Resource.isLOrAbove()) {
                    builder.setVisibility(Notification.VISIBILITY_PRIVATE);
                }

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                notificationManager.notify(debt.id.hashCode(), builder.build());

				//Do not remind again
				debt.setRemindDate(null);
				storage.commit(context);
    }};

        private NotificationCompat.Action getPayBackAction(UUID id) {
            Intent payBackIntent = new Intent(context, NotificationEventReceiver.class);
            payBackIntent.setAction(NotificationEventReceiver.ACTION_PAY_BACK);
            payBackIntent.putExtra(Alarm.ALARM_ID, id);

            return new NotificationCompat.Action(
                    R.drawable.notif_check,
                    context.getString(R.string.pay_back),
                    PendingIntent.getBroadcast(context, 0, payBackIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        }

        private NotificationCompat.Action getRemindLaterAction(UUID id) {
            Intent remindLaterIntent = new Intent(context, NotificationEventReceiver.class);
            remindLaterIntent.setAction(NotificationEventReceiver.ACTION_REMIND_LATER);
            remindLaterIntent.putExtra(Alarm.ALARM_ID, id);

			int icon = Resource.isLOrAbove() ? R.drawable.ic_material_reminder_finger_dark : R.drawable.ic_material_reminder_finger_light;

				return new NotificationCompat.Action(
						icon, context.getString(R.string.notif_remind_later),
						PendingIntent.getBroadcast(context, 0, remindLaterIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        }

        private PendingIntent getDetailPendingIntent(UUID id) {
            Intent detailIntent = new Intent(context, FeedActivity.class);

            detailIntent.putExtra(Alarm.ALARM_ID, id);
            detailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            return PendingIntent.getActivity(context, 0, detailIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        private String getContentText(Debt debt, AppData data) {
			int format = debt.getAmount() > 0 ? R.string.notif_they_owe : R.string.notif_you_owe;
			return context.getString(format, debt.getOwner().getName(), data.preferences.getCurrency().render(debt.getRemainingAbsoluteDebt()));
        }
    }

	public static class AlarmBootListener extends BroadcastReceiver implements Callback<AppData> {

		private AlarmScheduler scheduler;

		@Override
		public void onReceive(Context context, Intent intent) {
			Storage storage = StorageManager.getStorage(context);
			scheduler = new AlarmScheduler(context, storage.subscription);
			storage.subscription.listen(this);
		}

		@Override
		public void onCalled(AppData data) {
			scheduler.die();
		}
	}

    public static class NotificationEventReceiver extends BroadcastReceiver {

        public final static String ACTION_PAY_BACK = "ACTION_PAY_BACK";
        public final static String ACTION_REMIND_LATER = "ACTION_REMIND_LATER";

        private Context context;
        private Intent intent;

        private Storage storage;

        @Override
        public void onReceive(Context context, Intent intent) {

            this.context = context;
            this.intent = intent;

            storage = StorageManager.getStorage(context);
            storage.subscription.listen(dataLoadedCallback);
        }

		private final static int REMOVE_PAIDBACK_NOTIFICATION_DELAY = 4000;
        private Callback<AppData> dataLoadedCallback = new Callback<AppData>() {
            @Override
            public void onCalled(AppData data) {
				storage.subscription.unregister(this);

                final UUID id = (UUID) intent.getExtras().get(ALARM_ID);
                final Debt debt = data.findDebt(id);

                final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                switch (intent.getAction()) {
                    case ACTION_PAY_BACK:
                        if(debt == null) {
                            notificationManager.cancel(id.hashCode());
                            return;
                        }

						debt.payback();

                        storage.commit(context);
						storage.emit();

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.ic_stat_negative)
                                .setContentIntent(getFeedPendingIntent())
                                .setContent(new RemoteViews(context.getPackageName(), R.layout.paid_back_notification));

                        notificationManager.notify(debt.id.hashCode(), builder.build());

						final Handler handler = new Handler();
						handler.postDelayed(new Runnable() {
							@Override
							public void run() {
								notificationManager.cancel(id.hashCode());
							}
						}, REMOVE_PAIDBACK_NOTIFICATION_DELAY);

                        break;

                    case ACTION_REMIND_LATER:
                        if(debt == null) {
                            notificationManager.cancel(id.hashCode());
                            return;
                        }

                        Intent remindLaterIntent = new Intent(context, RemindLaterActivity.class);
                        remindLaterIntent.putExtra(RemindLaterActivity.KEY_DEBT_ID, debt.id.toString());
                        remindLaterIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(remindLaterIntent);

                        notificationManager.cancel(debt.id.hashCode());
                        break;
                }
            }
        };

        private PendingIntent getFeedPendingIntent() {
            Intent detailIntent = new Intent(context, FeedActivity.class);
            detailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            return PendingIntent.getActivity(context, 0, detailIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }
}