package com.johnsimon.payback.ui.dialog;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;
import com.johnsimon.payback.R;
import com.johnsimon.payback.async.Promise;
import com.johnsimon.payback.data.AppData;
import com.johnsimon.payback.storage.Storage;
import com.johnsimon.payback.util.BackupManager;

public class RestoreBackupDialog {

	public static Promise<Boolean> attemptRestore(Context context, final Storage storage) {
		final Promise<Boolean> p = new Promise<>();
		final BackupManager.ReadResult result = BackupManager.read();
		if(result.success) {
			new MaterialDialog.Builder(context)
					.title(R.string.restoredialog_title)
					.content(R.string.restoredialog_content)
					.positiveText(R.string.restore)
					.negativeText(R.string.cancel)
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
