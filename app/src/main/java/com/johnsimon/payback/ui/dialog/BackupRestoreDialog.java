package com.johnsimon.payback.ui.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.provider.ContactsContract;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.johnsimon.payback.R;
import com.johnsimon.payback.async.Promise;
import com.johnsimon.payback.data.AppData;
import com.johnsimon.payback.data.backup.Backup;
import com.johnsimon.payback.data.backup.BackupManager;
import com.johnsimon.payback.storage.Storage;
import com.johnsimon.payback.util.ReadResult;

public class BackupRestoreDialog {

    public static Promise<RestoreResult> attemptRestore(final Activity activity, final Storage storage, final boolean showRemove) {

        final Promise<RestoreResult> promise = new Promise<>();

        final Backup[] backups =  BackupManager.fetchBackups().data;
        String[] backupNames = new String[backups.length];

        for (int i = 0; i < backups.length; i++) {
            backupNames[i] = backups[i].generateString(activity, activity.getResources());
        }

        new MaterialDialog.Builder(activity)
                .title(R.string.pref_manage_backups)
                .items(backupNames)
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        promise.fire(RestoreResult.Canceled);
                    }
                })
                .itemsCallback(new MaterialDialog.ListCallback() {

                    @Override
                    public void onSelection(MaterialDialog dialog, View view, final int which, CharSequence text) {

                        if (showRemove) {
                            new MaterialDialog.Builder(activity)
                                    .items(new String[]{activity.getString(R.string.restore), activity.getString(R.string.delete)})
                                    .itemsCallback(new MaterialDialog.ListCallback() {
                                        @Override
                                        public void onSelection(MaterialDialog dialog, View view, int restoreDeleteWhich, CharSequence text) {
                                            if (restoreDeleteWhich == 0) {
                                                ReadResult<String, Backup.ReadError> result = backups[which].read();
                                                if (result.isSuccess()) {
                                                    storage.commit(activity, AppData.fromJson(result.data));
                                                    storage.emit();

                                                    promise.fire(RestoreResult.Success);
                                                } else {
                                                    if (result.error == Backup.ReadError.FileNotFound) {
                                                        promise.fire(RestoreResult.FileNotFound);
                                                    } else {
                                                        promise.fire(RestoreResult.Unknown);
                                                    }
                                                }
                                            } else {
                                                if (backups[which].remove()) {
                                                    promise.fire(RestoreResult.Deleted);
                                                } else {
                                                    promise.fire(RestoreResult.DeleteFailed);
                                                }
                                            }
                                        }

                                    })
                                    .show();
                        } else {
                            new MaterialDialog.Builder(activity)
                                    .title(R.string.restoredialog_title_both)
                                    .content(R.string.pref_restore_data_description)
                                    .positiveText(R.string.restore)
                                    .negativeText(R.string.cancel)
                                    .callback(new MaterialDialog.ButtonCallback() {
                                        @Override
                                        public void onPositive(MaterialDialog dialog) {
                                            super.onPositive(dialog);
                                            ReadResult<String, Backup.ReadError> result = backups[which].read();
                                            if (result.isSuccess()) {
                                                storage.commit(activity, AppData.fromJson(result.data));
                                                storage.emit();

                                                promise.fire(RestoreResult.Success);
                                            } else {
                                                if (result.error == Backup.ReadError.FileNotFound) {
                                                    promise.fire(RestoreResult.FileNotFound);
                                                } else {
                                                    promise.fire(RestoreResult.Unknown);
                                                }
                                            }
                                            dialog.cancel();
                                        }

                                        @Override
                                        public void onNegative(MaterialDialog dialog) {
                                            super.onNegative(dialog);
                                            promise.fire(RestoreResult.Canceled);
                                            dialog.cancel();
                                        }
                                    }).show();
                        }
                    }
                }).show();

        return promise;
    }

    public enum RestoreResult {
        Success, Canceled, FileNotFound, Unknown, NoBackups, Deleted, DeleteFailed;
        public boolean isSuccess() {
            return this == Success;
        }
    }

}
