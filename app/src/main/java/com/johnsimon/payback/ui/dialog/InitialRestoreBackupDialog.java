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

public class InitialRestoreBackupDialog {

	public static Promise<Boolean> attemptRestore(final Activity activity, final Storage storage, final View masterView) {
		final Promise<Boolean> p = new Promise<>();

		if (BackupManager.hasBackups()) {

			new MaterialDialog.Builder(activity)
					.title(R.string.restoredialog_title)
					.content(R.string.restoredialog_content)
					.positiveText(R.string.restoredialog_select_backup)
					.negativeText(R.string.cancel)
					.cancelListener(new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialogInterface) {
							p.fire(false);
						}
					})
					.cancelable(false)
					.callback(new MaterialDialog.ButtonCallback() {
						@Override
						public void onPositive(MaterialDialog dialog) {
							BackupRestoreDialog.attemptRestore(activity, storage, false).then(new Callback<BackupRestoreDialog.RestoreResult>() {
								@Override
								public void onCalled(BackupRestoreDialog.RestoreResult result) {
									p.fire(result.isSuccess());
									if (result == BackupRestoreDialog.RestoreResult.Unknown ||
											result == BackupRestoreDialog.RestoreResult.FileNotFound) {
										Snackbar.make(masterView, R.string.read_failed, Snackbar.LENGTH_SHORT).show();
									}
								}
							});
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
