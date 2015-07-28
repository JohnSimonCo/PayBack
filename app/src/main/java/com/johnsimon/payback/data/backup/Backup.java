package com.johnsimon.payback.data.backup;

import android.content.res.Resources;

import com.johnsimon.payback.R;
import com.johnsimon.payback.util.ReadResult;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Backup {
	public boolean auto;
	public Date date;

	private File file;

	public Backup(File file) throws ParseException {
		this.file = file;
		String fileName = file.getName();

		if(fileName.equals("data.json")) { // Old file name (Implemented 28/07-15)
			this.auto = false;
			this.date = new Date();
			file.renameTo(new File(file.getParentFile(), BackupManager.generateFileName(Type.Manual)));
		} else {
			String[] parts = fileName.split(" ");
			String backupType = parts[0], date = parts[1];
			this.auto = backupType.equals(BackupManager.autoBackupFileName);
			this.date = BackupManager.getFormatter().parse(date);
		}
	}

	public boolean remove() {
		return file.delete();
	}
	public ReadResult<String, ReadError> read() {
		try {
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			reader.close();
			return ReadResult.success(builder.toString());
		} catch (FileNotFoundException ex) {
			return ReadResult.error(ReadError.FileNotFound);
		} catch (Exception ex) {
			return ReadResult.error(ReadError.Unknown);
		}
	}

	public String generateString(Resources resources) {
		return resources.getString(auto ? R.string.autobackup : R.string.backup) + " - " + generateDateString();
	}

	public String generateDateString() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm", Locale.getDefault());
		return format.format(date);
	}

	public enum ReadError {
		FileNotFound, Unknown
	}

	public enum Type {
		Manual, Auto, Wipe
	}

}