package com.johnsimon.payback.util;

import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.ParseException;
import java.util.Date;

public class Backup {
	public boolean auto;
	public Date date;

	private File file;

	public Backup(File file) throws ParseException {
		this.file = file;
		String fileName = file.getName();
		String[] parts = fileName.split(" ");
		String backupType = parts[0], date = parts[1];
		this.auto = backupType.equals(BackupManager.autoBackupFileName);
		this.date = BackupManager.getFormatter().parse(date);
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
		//TODO implement
		return "";
	}

	public enum ReadError {
		FileNotFound, Unknown
	}

}