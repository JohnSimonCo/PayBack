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
import android.support.v4.app.NotificationCompat;

import com.johnsimon.payback.R;
import com.johnsimon.payback.async.Callback;
import com.johnsimon.payback.data.AppData;
import com.johnsimon.payback.data.Debt;
import com.johnsimon.payback.storage.LocalStorage;
import com.johnsimon.payback.ui.FeedActivity;

import java.util.Calendar;
import java.util.UUID;

public class Alarm  {

    public final static String ALARM_ID = "ALARM_ID";

    //TODO PendingIntent.getBroadcast requestCode "0" is magic number, see if it has effect or not
    public static void addAlarm(Calendar targetDate, Context ctx, Debt debt) {
        Intent intentAlarm = new Intent(ctx, Alarm.class);
        intentAlarm.putExtra(ALARM_ID, debt.getId());

        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);

        alarmManager.set(AlarmManager.RTC_WAKEUP, targetDate.getTimeInMillis(), PendingIntent.getBroadcast(ctx, 0, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public static boolean hasAlarm (Context ctx, UUID id) {
        Intent intentAlarm = new Intent(ctx, Alarm.class);
        intentAlarm.putExtra(ALARM_ID, id);

        return (PendingIntent.getBroadcast(ctx, 0, intentAlarm,PendingIntent.FLAG_NO_CREATE) != null);
    }

    public static void cancelAlarm(Context ctx, UUID id) {
        Intent intent = new Intent(ctx, Alarm.class);
        intent.putExtra(ALARM_ID, id);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        pendingIntent.cancel();

        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);

        alarmManager.cancel(pendingIntent);
    }

    private static class AlarmReceiver extends BroadcastReceiver {

        private Context context;
        private Intent intent;

        @Override
        public void onReceive(final Context context, Intent intent) {

            this.context = context;
            this.intent = intent;

            LocalStorage localStorage = new LocalStorage(context);
            localStorage.subscription.listen(dataLoadedCallback);
        }

        private Callback<AppData> dataLoadedCallback = new Callback<AppData>() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onCalled(AppData data) {
                UUID id = (UUID) intent.getExtras().get(ALARM_ID);
                Debt debt = data.findDebt(id);
                //TODO det här kanske kan ge fel i edge cases      if (debt == null) return;

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_stat_negative)
                        .setContentTitle(context.getString(R.string.notif_pay_back_reminder))
                        .setContentText(getContentText(debt, data))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setColor(context.getResources().getColor(R.color.primary_color))
                        .addAction(getPayBackAction(id))
                        .addAction(getRemindLaterAction(id))
                        .setContentIntent(getDetailPendingIntent(id));

                if (Resource.isLOrAbove()) {
                    builder.setVisibility(Notification.VISIBILITY_PRIVATE);
                }

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                notificationManager.notify(debt.id.hashCode(), builder.build());
            }
        };

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

            return new NotificationCompat.Action(
                    R.drawable.abc_ab_share_pack_holo_dark,
                    context.getString(R.string.notif_remind_later),
                    PendingIntent.getBroadcast(context, 0, remindLaterIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        }

        private PendingIntent getDetailPendingIntent(UUID id) {
            Intent detailIntent = new Intent(context, FeedActivity.class);

            detailIntent.putExtra(Alarm.ALARM_ID, id);
            detailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            return PendingIntent.getActivity(context, 0, detailIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        private String getContentText(Debt debt, AppData data) {
            if (debt.getAmount() > 0) {
                return context.getString(R.string.notif_they_owe, debt.getOwner().getName(), data.preferences.getCurrency().render(debt.getAmount()));
            } else {
                return context.getString(R.string.notif_you_owe, debt.getOwner().getName(), data.preferences.getCurrency().render(debt.getAmount()));
            }
        }
    }

    private static class NotificationEventReceiver extends BroadcastReceiver {

        public final static String ACTION_PAY_BACK = "ACTION_PAY_BACK";
        public final static String ACTION_REMIND_LATER = "ACTION_REMIND_LATER";

        private Context context;
        private Intent intent;

        @Override
        public void onReceive(Context context, Intent intent) {

            this.context = context;
            this.intent = intent;

            LocalStorage localStorage = new LocalStorage(context);
            localStorage.subscription.listen(dataLoadedCallback);
        }

        private Callback<AppData> dataLoadedCallback = new Callback<AppData>() {
            @Override
            public void onCalled(AppData data) {

                UUID id = (UUID) intent.getExtras().get(ALARM_ID);
                Debt debt = data.findDebt(id);

                switch (intent.getAction()) {
                    case ACTION_PAY_BACK:
                        //TODO SHOW NOTIFICATION IF DEBT IS PAID BACK?
                        debt.setPaidBack(true);
                        break;

                    case ACTION_REMIND_LATER:
                        Calendar calendar = Calendar.getInstance();
                        // 86400000 = 1000 * 60 * 60 * 24 = One day
                        calendar.setTimeInMillis(calendar.getTimeInMillis() + 86400000);

                        Alarm.addAlarm(calendar, context, debt);
                        break;
                }
            }
        };
    }
}