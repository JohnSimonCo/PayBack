package com.johnsimon.payback.ui.dialog;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;
import com.johnsimon.payback.async.Promise;
import com.johnsimon.payback.data.AppData;
import com.johnsimon.payback.storage.Storage;
import com.johnsimon.payback.util.FileManager;

public class RestoreBackupDialog {

	public static Promise<Boolean> attemptRestore(Context context, final Storage storage) {
		final Promise<Boolean> p = new Promise<>();
		final FileManager.ReadResult result = FileManager.read();
		if(result.success) {
			new MaterialDialog.Builder(context)
					.title("You got a file bro")
					.content("Wanna restore from it???")
					.positiveText("Yes i am")
					.negativeText("No, sure")
					.cancelable(false)
					.callback(new MaterialDialog.ButtonCallback() {
						@Override
						public void onPositive(MaterialDialog dialog) {
							storage.commit(AppData.fromJson(result.content));
							storage.emit();
							
							p.fire(true);
						}

						@Override
						public void onNegative(MaterialDialog dialog) {
							p.fire(false);
						}
					}).show();
		} else {
			p.fire(false);
		}
		return p;
	}
}
