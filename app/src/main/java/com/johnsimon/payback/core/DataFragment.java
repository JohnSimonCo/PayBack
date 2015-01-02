package com.johnsimon.payback.core;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.johnsimon.payback.storage.Storage;
import com.johnsimon.payback.util.AppData;

/**
 * Created by johnrs on 2015-01-02.
 */
public abstract class DataFragment extends Fragment implements Callback<AppData> {
    public AppData data;
    public Storage storage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        DataActivity activity = (DataActivity) getActivity();
        this.storage = activity.storage;
        storage.callbacks.add(this);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDataReceived(AppData data) {
        this.data = data;
    }
}
