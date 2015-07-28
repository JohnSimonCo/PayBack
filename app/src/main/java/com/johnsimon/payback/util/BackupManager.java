package com.johnsimon.payback.util;

import android.content.res.Resources;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class BackupManager {

	private final static String parentDir = Resource.isKkOrAbove() ? Environment.DIRECTORY_DOCUMENTS : "Documents";
	private final static String dirName = "PayBack";
	public final static String autoBackupFileName = "Auto-backup";
	private final static String manualBackupFileName = "Backup";
	private final static String fileExtension = "json";
	private final static String simpleFilePath = parentDir + File.pathSeparator + dirName;

	public static WriteResult createBackup(String JSON, Boolean autoBackup) {
		/*
		if(!isExternalStorageWritable()) {
			show(activity, R.string.library_roundedimageview_licenseId);
			return;
		}*/

		try {
			File dir = getDir(), file = new File(dir, generateFileName(autoBackup));

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

			return new WriteResult(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new WriteResult(false);
		}
	}

	public static SimpleDateFormat getFormatter() {
		return new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
	}
	private static String generateFileName(Boolean autoBackup) {
		SimpleDateFormat formatter = getFormatter();
		Date now = new Date();
		String dateString = formatter.format(now);
		String fileName = autoBackup ? autoBackupFileName : manualBackupFileName;
		return fileName + " " + dateString + File.separator + fileExtension;
	}
	public static ReadResult<Backup[], ReadError> fetchBackups() {
		try {
			File[] files = getFiles();
			Backup[] backups = new Backup[files.length];
			for(int i = 0; i < files.length; i++) {
				backups[i] = new Backup(files[i]);
			}
			return ReadResult.success(backups);
		}/* catch(FileNotFoundException e) {
			return new ReadResult(ReadError.NoFile);
		}*/ catch(ParseException e) {
			return ReadResult.error(ReadError.Parse);
		}
	}
	public static Date latestBackupDate() {
		Backup latestBackup = latestBackup();
		return latestBackup != null ? latestBackup.date : null;
	}
	public static Backup latestBackup() {
		ReadResult<Backup[], ReadError> result = fetchBackups();
		if(!result.isSuccess() || result.data.length < 1) {
			return null;
		}
		List<Backup> backups = Arrays.asList(result.data);
		Collections.sort(backups, new Comparator<Backup>() {
			@Override
			public int compare(Backup a, Backup b) {
				return a.date.compareTo(b.date);
			}
		});
		return backups.get(0);
	}
	public static Boolean hasBackups() {
		ArrayList<Backup> backups = new ArrayList<>();
		return getFiles().length > 0;
	}

	@Deprecated
	public static WriteResult write(String JSON) {
		/*
		if(!isExternalStorageWritable()) {
			show(activity, R.string.library_roundedimageview_licenseId);
			return;
		}*/

		try {
			File dir = getDir(), file = new File(dir, manualBackupFileName);

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

			return new WriteResult(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new WriteResult(false);
		}
	}

	@Deprecated
	public static boolean hasFile() {
		return getFile().exists();
	}

	@Deprecated
	public static boolean removeFile() {
		File file = getFile();
		return file.exists() && file.delete();
	}

	private static File getDir() {
		return new File(Environment.getExternalStoragePublicDirectory(parentDir), dirName);
	}

	@Deprecated
	private static File getFile() {
		return new File(getDir(), manualBackupFileName);
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
		Parse
	}
}
