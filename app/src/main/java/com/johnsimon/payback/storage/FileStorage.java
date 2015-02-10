package com.johnsimon.payback.storage;

import com.johnsimon.payback.data.AppData;
import com.johnsimon.payback.util.FileManager;

public class FileStorage extends ExternalStorage {
	public FileStorage(final LocalStorage localStorage) {
		super(localStorage);

		FileManager.ReadResult result = FileManager.read();

		if(result.success) {
			sync(AppData.fromJson(result.content));
		} else {
			switch(result.error) {
				case FileManager.ReadResult.ERROR_NO_FILE:
					break;
				default:
					error("Unknown read error", null);
					break;
			}
		}
	}

	@Override
	protected void commitExternally(String JSON) {
		FileManager.WriteResult result = FileManager.write(JSON);

		if(result.success) {
			show("Successfully commited to file");
		} else {
			error("File write error", null);
		}
	}
}
