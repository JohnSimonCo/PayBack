package com.johnsimon.payback.util;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;

import com.google.android.gms.drive.DriveContents;
import com.johnsimon.payback.R;
import com.williammora.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;

public class FileManager {

	private final static String parentDir = Resource.isKkOrAbove() ? Environment.DIRECTORY_DOCUMENTS : "Documents";
	private final static String dirName = "PayBack";
	private final static String fileName = "data.json";
	public final static String simpleFilePath = parentDir + "/" + dirName;

	public static WriteResult write(String JSON) {
		/*
		if(!isExternalStorageWritable()) {
			show(activity, R.string.library_roundedimageview_licenseId);
			return;
		}*/

		try {
			File dir = getDir(), file = new File(dir, fileName);

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

	public static ReadResult read() {
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
			return new ReadResult(builder.toString());
		} catch(FileNotFoundException e) {
			return new ReadResult(ReadResult.ERROR_NO_FILE);
		} catch(Exception e) {
			return new ReadResult(ReadResult.ERROR_UNKNOWN);
		}
	}

	public static boolean hasFile() {
		return getFile().exists();
	}

	public static boolean removeFile() {
		File file = getFile();
		return file.exists() && file.delete();
	}

	private static File getDir() {
		return new File(Environment.getExternalStoragePublicDirectory(parentDir), dirName);
	}

	private static File getFile() {
		return new File(getDir(), fileName);
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
	public static class ReadResult {
		public final static int ERROR_UNKNOWN = 0;
		public final static int ERROR_NO_FILE = 1;

		public boolean success;
		public int error;
		public String content;

		private ReadResult(boolean success) {
			this.success = success;
		}

		public ReadResult(String content) {
			this(true);
			this.content = content;
		}

		public ReadResult(int error) {
			this(false);
			this.error = error;
		}
	}

	private static void show(Activity activity, int messageId) {
        show(activity, activity.getString(messageId));
	}

    private static void show(Activity activity, String show) {
        Snackbar.with(activity)
                .text(show)
                .show(activity);
    }
}
