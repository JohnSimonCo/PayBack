package com.johnsimon.payback.util;

import android.app.Activity;
import android.content.Context;

import com.google.android.gms.drive.DriveContents;
import com.johnsimon.payback.R;
import com.williammora.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

public class FileManager {

	private final static String fileName = "savedata.txt";

	public static void write(Activity activity, String JSON) {
		try {
			FileOutputStream outputStream = activity.openFileOutput(fileName, Context.MODE_PRIVATE);
			outputStream.write(JSON.getBytes());
			outputStream.close();

			show(activity, R.string.save_success);
		} catch (Exception e) {
			e.printStackTrace();
			show(activity, R.string.save_fail);
		}
	}

	public static String read(Activity activity) {
		StringBuilder builder = new StringBuilder();

		try {
			FileInputStream inputStream = activity.openFileInput(fileName);

			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
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

	private static void show(Activity activity, int messageId) {
		Snackbar.with(activity)
				.text(activity.getString(messageId))
				.show(activity);
	}
}
