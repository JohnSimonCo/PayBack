package com.johnsimon.payback.core;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.johnsimon.payback.storage.LocalStorage;
import com.johnsimon.payback.storage.Storage;
import com.johnsimon.payback.util.AppData;

/**
 * Created by johnrs on 2015-01-02.
 */
public abstract class DataActivity extends ActionBarActivity implements Callback<AppData> {

    public Storage storage;
    public AppData data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        storage = new LocalStorage(this);
        storage.callbacks.add(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        storage.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();

        storage.connect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean handled = storage.handleActivityResult(requestCode, resultCode, data);

        if(!handled) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDataReceived(AppData data) {
        this.data = data;
    }
}
