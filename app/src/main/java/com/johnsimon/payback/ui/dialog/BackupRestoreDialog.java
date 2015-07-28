package com.johnsimon.payback.ui.dialog;

import android.app.Activity;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.johnsimon.payback.R;
import com.johnsimon.payback.util.BackupManager;

public class BackupRestoreDialog {

    public MaterialDialog backupList(Activity activity, BackupManager backupManager) {

        BackupManager.Backup[] backups =  backupManager.fetchBackups().data;
        String[] backupNames = new String[backups.length];

        for (int i = 0; i < backups.length; i++) {
            backupNames[i] = backups[i].generateString(activity.getResources());
        }

        return new MaterialDialog.Builder(activity)
            .title(R.string.pref_restore_backup)
            .items()
            .itemsCallback(new MaterialDialog.ListCallback() {

                @Override
                public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {

                }
            }).show();
    }

}
