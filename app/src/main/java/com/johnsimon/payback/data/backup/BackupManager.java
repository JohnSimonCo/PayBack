package com.johnsimon.payback.data.backup;

import android.content.Context;
import android.os.Environment;

import com.johnsimon.payback.async.Background;
import com.johnsimon.payback.async.BackgroundBlock;
import com.johnsimon.payback.async.Promise;
import com.johnsimon.payback.data.AppData;
import com.johnsimon.payback.util.ReadResult;
import com.johnsimon.payback.util.Resource;

import java.io.File;
import java.io.FileWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class BackupManager {

	private final static String parentDir = Resource.isKkOrAbove() ? Environment.DIRECTORY_DOCUMENTS : "Documents";
	private final static String dirName = "PayBack";
	private final static String fileExtension = "json";
	//Used for display
	public final static String simpleFilePath = parentDir + "/" + dirName;

	public static boolean createBackup(String JSON, Backup.Type backupType) {
		/*
		if(!isExternalStorageWritable()) {
			show(activity, R.string.library_roundedimageview_licenseId);
			return;
		}*/

		try {
			File dir = getDir(), file = new File(dir, generateFileName(backupType));

			if(!dir.exists()) {
				if(!dir.mkdirs()) {
					throw new UnknownError("dir.mkdirs() failed");
				}
			}

			if(!file.exists()) {
				if(!file.createNewFile()) {
					throw new UnknownError("file.createNewFile() failed");
				}
			}

			FileWriter writer = new FileWriter(file);
			writer.write(JSON);
			writer.close();

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static Promise<Boolean> createBackupAsync(Context context, final AppData data, final Backup.Type backupType) {
		return Background.run(context, new BackgroundBlock<Boolean>() {
			@Override
			public Boolean run() {
				return createBackup(data.save(), backupType);
			}
		});
	}

	public static SimpleDateFormat getFormatter() {
		return new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
	}
	public static String generateFileName(Backup.Type backupType) {
		SimpleDateFormat formatter = getFormatter();
		Date now = new Date();
		String dateString = formatter.format(now);

		return backupType.typeString + " " + dateString + "." + fileExtension;
	}
	public static ReadResult<Backup[], ReadError> fetchBackups() {
		try {
			File[] files = getFiles();
			if (files == null) {
				return ReadResult.error(ReadError.NoFiles);
			}
			Backup[] backups = new Backup[files.length];
			for(int i = 0; i < files.length; i++) {
				backups[i] = new Backup(files[i]);
			}
			Arrays.sort(backups, backupComparator);
			return ReadResult.success(backups);
		} catch(ParseException e) {
			return ReadResult.error(ReadError.Parse);
		}
	}
	private static Comparator<Backup> backupComparator = new Comparator<Backup>() {
		@Override
		public int compare(Backup a, Backup b) {
			return b.date.compareTo(a.date);
		}
	};
	public static Date latestBackupDate() {
		Backup latestBackup = latestBackup();
		return latestBackup != null ? latestBackup.date : null;
	}
	public static Backup latestBackup() {
		ReadResult<Backup[], ReadError> result = fetchBackups();
		Backup[] backups = result.data;
		if(!result.isSuccess() || backups.length < 1) {
			return null;
		}
		return backups[0];
	}
	public static Boolean hasBackups() {
		File[] files = getFiles();
		return files != null && files.length > 0;
	}

	private static File getDir() {
		return new File(Environment.getExternalStoragePublicDirectory(parentDir), dirName);
	}

	private static File[] getFiles() {
		File dir = getDir();
		return dir.listFiles();
	}



	/* Checks if external storage is available for read and write
	private static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}*/

	/* Checks if external storage is available to at least read
	private static boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) ||
				Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}*/

	public static class WriteResult {
		public boolean success;

		public WriteResult(boolean success) {
			this.success = success;
		}
	}

	public enum ReadError {
		Parse, NoFiles
	}
}
