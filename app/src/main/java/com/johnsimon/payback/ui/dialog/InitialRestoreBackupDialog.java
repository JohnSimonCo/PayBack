package com.johnsimon.payback.ui.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.johnsimon.payback.R;
import com.johnsimon.payback.async.Callback;
import com.johnsimon.payback.async.Promise;
import com.johnsimon.payback.storage.Storage;
import com.johnsimon.payback.data.backup.BackupManager;
import com.johnsimon.payback.storage.StorageManager;

public class InitialRestoreBackupDialog {

	public static Promise<Boolean> attemptRestore(final Activity activity, final Storage storage, final View masterView) {
		final Promise<Boolean> p = new Promise<>();

		boolean hasBackups = BackupManager.hasBackups();

		int title = hasBackups ? R.string.restoredialog_title_both : R.string.cloud_sync;
		int content = hasBackups ? R.string.restoredialog_content_both : R.string.restoredialog_content_cloud;
		int positive = hasBackups ? R.string.restoredialog_select_backup : R.string.activate;

		MaterialDialog.Builder builder = new MaterialDialog.Builder(activity)
				.title(title)
				.content(content)
				.positiveText(positive)
				.negativeText(R.string.cancel)
				.cancelListener(dialogInterface -> p.fire(false))
				.cancelable(false)
				.callback(new MaterialDialog.ButtonCallback() {
					@Override
					public void onPositive(MaterialDialog dialog) {
						BackupRestoreDialog.attemptRestore(activity, storage, false).then(result -> {
                            p.fire(result.isSuccess());
							switch (result) {
								case Unknown: case FileNotFound:
									Snackbar.make(masterView, R.string.read_failed, Snackbar.LENGTH_SHORT).show();
									break;
							}
                        });
					}

					@Override
					public void onNeutral(MaterialDialog dialog) {
						super.onNeutral(dialog);
						p.fire(true);
						StorageManager.migrateToDrive(activity).then(result -> {
							if (result.success) {
								Snackbar.make(masterView, R.string.login_successful, Snackbar.LENGTH_LONG).show();
							}
						});
					}

					@Override
					public void onNegative(MaterialDialog dialog) {
						p.fire(false);
					}
				});

		if (hasBackups) {
			builder.neutralText(R.string.cloud_sync);
		}


		return p;
	}
}
