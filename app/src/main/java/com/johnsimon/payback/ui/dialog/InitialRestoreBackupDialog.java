package com.johnsimon.payback.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;

import com.afollestad.materialdialogs.MaterialDialog;
import com.johnsimon.payback.R;
import com.johnsimon.payback.async.Callback;
import com.johnsimon.payback.async.Promise;
import com.johnsimon.payback.data.AppData;
import com.johnsimon.payback.storage.Storage;
import com.johnsimon.payback.util.BackupManager;

public class InitialRestoreBackupDialog {

	public static Promise<BackupRestoreDialog.RestoreResult> attemptRestore(final Activity activity, final Storage storage) {
		final Promise<BackupRestoreDialog.RestoreResult> p = new Promise<>();

		if (BackupManager.hasBackups()) {

			new MaterialDialog.Builder(activity)
					.title(R.string.restoredialog_title)
					.content(R.string.restoredialog_content)
					.positiveText(R.string.restoredialog_select_backup)
					.negativeText(R.string.cancel)
					.cancelListener(new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialogInterface) {
							p.fire(BackupRestoreDialog.RestoreResult.Canceled);
						}
					})
					.cancelable(false)
					.callback(new MaterialDialog.ButtonCallback() {
						@Override
						public void onPositive(MaterialDialog dialog) {
							BackupRestoreDialog.attemptRestore(activity, storage).then(new Callback<BackupRestoreDialog.RestoreResult>() {
								@Override
								public void onCalled(BackupRestoreDialog.RestoreResult result) {
									p.fire(result);
								}
							});
						}

						@Override
						public void onNegative(MaterialDialog dialog) {
							p.fire(BackupRestoreDialog.RestoreResult.Canceled);
						}
					}).show();
		} else {
			p.fire(BackupRestoreDialog.RestoreResult.NoBackups);
		}
		return p;
	}
}
