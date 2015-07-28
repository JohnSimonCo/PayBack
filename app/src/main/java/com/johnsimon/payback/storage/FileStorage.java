package com.johnsimon.payback.storage;

import com.johnsimon.payback.data.AppData;
import com.johnsimon.payback.util.BackupManager;

/*
public class FileStorage extends ExternalStorage {

	public FileStorage(final LocalStorage localStorage) {
		super(localStorage);

		BackupManager.ReadResult result = BackupManager.read();

		if(result.success) {
			sync(AppData.fromJson((String) result.data));
		} else {
			switch(result.error) {
				case NoFile:
					break;
				default:
					error("Unknown read error", null);
					break;
			}
		}
	}

	@Override
	protected void commitExternally(String JSON) {
		BackupManager.WriteResult result = BackupManager.write(JSON);

		if(result.success) {
			show("Successfully commited to file");
		} else {
			error("File write error", null);
		}
	}
}
*/