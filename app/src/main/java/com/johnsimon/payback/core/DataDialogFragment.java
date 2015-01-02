package com.johnsimon.payback.core;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import com.johnsimon.payback.storage.Storage;
import com.johnsimon.payback.util.AppData;

/**
 * Created by johnrs on 2015-01-02.
 */
public abstract class DataDialogFragment extends DialogFragment implements Callback<AppData> {
    public AppData data;
    public Storage storage;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DataActivity activity = (DataActivity) getActivity();
        this.storage = activity.storage;
        storage.callbacks.add(this);

        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onDataReceived(AppData data) {
        this.data = data;
    }
}
