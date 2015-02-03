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

	public static void write(Activity activity, String JSON) {
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

			show(activity, R.string.save_success);
		} catch (Exception e) {
			e.printStackTrace();
			show(activity, R.string.save_fail);
		}
	}

	public static String read(Activity activity) {
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
		} catch(FileNotFoundException e) {
			show(activity, R.string.no_file);
		} catch(Exception e) {
			show(activity, R.string.read_failed);
		}

		return builder.toString();
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

	private static void show(Activity activity, int messageId) {
		Snackbar.with(activity)
				.text(activity.getString(messageId))
				.show(activity);
	}
}
