package com.johnsimon.payback.ui.dialog;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.johnsimon.payback.R;
import com.johnsimon.payback.async.Callback;
import com.johnsimon.payback.async.Promise;
import com.johnsimon.payback.storage.DriveLoginManager;
import com.johnsimon.payback.storage.Storage;
import com.johnsimon.payback.data.backup.BackupManager;
import com.johnsimon.payback.storage.StorageManager;
import com.johnsimon.payback.util.PermissionManager;

public class InitialRestoreBackupDialog {


	public static Promise<Boolean> attemptRestore(final Activity activity, final Storage storage, final View masterView) {
		final Promise<Boolean> p = new Promise<>();

		boolean hasBackupPermission = ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

		final boolean hasBackups = hasBackupPermission ? BackupManager.hasBackups() : false;

		int title = R.string.restoredialog_title_welcome;
		int content = hasBackups ? R.string.restoredialog_content_both : R.string.restoredialog_content_cloud;
		int positive = hasBackups ? R.string.restoredialog_select_backup : R.string.activate;

		MaterialDialog.Builder builder = new MaterialDialog.Builder(activity)
				.title(title)
				.content(content)
				.positiveText(positive)
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
						if(hasBackups) {
							restoreFromBackup();
						} else {
							enableCloudSync();
						}
					}

					@Override
					public void onNeutral(MaterialDialog dialog) {
						super.onNeutral(dialog);
						enableCloudSync();
					}

					@Override
					public void onNegative(MaterialDialog dialog) {
						p.fire(false);
					}

					private void restoreFromBackup() {
						BackupRestoreDialog.attemptRestore(activity, storage, true).then(new Callback<BackupRestoreDialog.RestoreResult>() {
							@Override
							public void onCalled(BackupRestoreDialog.RestoreResult result) {

								p.fire(result.isSuccess());
								switch (result) {
									case Unknown: case FileNotFound:
										Snackbar.make(masterView, R.string.read_failed, Snackbar.LENGTH_SHORT).show();
										break;
								}
							}
						});
					}

					private void enableCloudSync() {
						p.fire(true);
						StorageManager.migrateToDrive(activity).then(new Callback<DriveLoginManager.LoginResult>() {
							@Override
							public void onCalled(DriveLoginManager.LoginResult result) {
								if (result.success) {
									Snackbar.make(masterView, R.string.login_successful, Snackbar.LENGTH_LONG).show();
								}
							}
						});
					}
				});

		if (hasBackups) {
			builder.neutralText(R.string.cloud_sync);
		}

		builder.build().show();


		return p;
	}
}
