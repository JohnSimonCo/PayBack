package com.johnsimon.payback.data.backup;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.johnsimon.payback.ui.SettingsActivity;
import com.johnsimon.payback.util.ReadResult;

import java.util.Calendar;
import java.util.Date;

public class AutoBackuper {
	public final static String JSON_DATA_ALARM_EXTRA_KEY = "AUTO_BACKUP_ID";

	private final static long FIVE_MINUTES = 1000 * 60 * 5;
	private static long nextBackupDate() {
		return System.currentTimeMillis() + FIVE_MINUTES;
	}
	public static void scheduleBackup(Context context, SharedPreferences preferences, String JSON) {
		boolean shouldAutoBackup = preferences.getBoolean(SettingsActivity.PREFERENCE_AUTO_BACKUP, false);
		if(!shouldAutoBackup) { return; }

		Intent intentAlarm = new Intent(context, AlarmReceiver.class);
		intentAlarm.putExtra(JSON_DATA_ALARM_EXTRA_KEY, JSON);

		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		alarmManager.set(AlarmManager.RTC_WAKEUP, nextBackupDate(), PendingIntent.getBroadcast(context, 0, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));

		Log.d("AutoBackuper", "Sheduled auto-backup");
	}

	public static void performBackup(String JSON, Backup.Type type) {
		removeTodaysPreviousBackup();
		boolean success = BackupManager.createBackup(JSON, type);
		if(!success) {
			Log.e("AutoBackuper", "Auto-backup failed");
		}
	}

	private static void removeTodaysPreviousBackup() {
		ReadResult<Backup[], BackupManager.ReadError> backups = BackupManager.fetchBackups();
		if(!backups.isSuccess()) { return; }

		for(Backup backup : backups.data) {
			if(backup.type != Backup.Type.Auto) {
				continue;
			}
			Calendar nowCalendar = Calendar.getInstance();
			Calendar backupCalendar = Calendar.getInstance();
			nowCalendar.setTime(new Date());
			backupCalendar.setTime(backup.date);

			//On the same day
			if(nowCalendar.get(Calendar.YEAR) == backupCalendar.get(Calendar.YEAR) && nowCalendar.get(Calendar.DAY_OF_YEAR) == backupCalendar.get(Calendar.DAY_OF_YEAR)) {

				Log.i("AutoBackuper", "Removed todays previous backup");
				backup.remove();
				//This should be the only auto-backup today so we can stop searching
				break;
			}
		}

	}
	public static class AlarmReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(final Context context, Intent intent) {
			String JSON = intent.getStringExtra(JSON_DATA_ALARM_EXTRA_KEY);
			AutoBackuper.performBackup(JSON, Backup.Type.Auto);
		}
	}
}
