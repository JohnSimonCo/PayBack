package com.johnsimon.payback.util;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BackupManager {

	private final static String parentDir = Resource.isKkOrAbove() ? Environment.DIRECTORY_DOCUMENTS : "Documents";
	private final static String dirName = "PayBack";
	private final static String autoBackupFileName = "Auto-backup";
	private final static String manualBackupFileName = "Backup";
	private final static String fileExtension = "json";
	public final static String simpleFilePath = parentDir + File.pathSeparator + dirName;

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
	private static String generateFileName(Boolean autoBackup) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
		Date now = new Date();
		String dateString = formatter.format(now);
		String fileName = autoBackup ? autoBackupFileName : manualBackupFileName;
		return fileName + " " + dateString + File.separator + fileExtension;
	}
	public static ReadResult<Backup[]> fetchBackups() {
		ArrayList<Backup> backups = new ArrayList<>();

		StringBuilder builder = new StringBuilder();
		try {
			File[] files = getFiles();
/*
			BufferedReader reader = new BufferedReader(new FileReader(getFile()));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			reader.close();
*/			for(File file: files) {

			}
			return new ReadResult<>(backups.toArray(new Backup[backups.size()]));
		}/* catch(FileNotFoundException e) {
			return new ReadResult(ReadError.NoFile);
		}*/ catch(Exception e) {
			return new ReadResult(ReadError.Unknown);
		}
	}
	public static Long lastBackupDate() {
		return null;
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
	public static ReadResult<String> read() {
		/*if(!isExternalStorageReadable()) {
			show(activity, R.string.library_roundedimageview_licenseId);
			return null;
		}*/

		StringBuilder builder = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(getFile()));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			reader.close();
			return new ReadResult<>(builder.toString());
		} catch(FileNotFoundException e) {
			return new ReadResult(ReadError.NoFile);
		} catch(Exception e) {
			return new ReadResult(ReadError.Unknown);
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
		Unknown, NoFile
	}
	public static class ReadResult<T> {
		public boolean success;
		public ReadError error;
		public T data;

		private ReadResult(boolean success) {
			this.success = success;
		}

		public ReadResult(T data) {
			this(true);
			this.data = data;
		}

		public ReadResult(ReadError error) {
			this(false);
			this.error = error;
		}
	}
	public class Backup {
		public String fileName;
		private File file;

		public void remove() {

		}
		public String read() {
			return null;
		}
	}
}
