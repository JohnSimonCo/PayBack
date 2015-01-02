package com.johnsimon.payback.core;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.johnsimon.payback.util.AppData;
import com.johnsimon.payback.util.DriveStorage;

/**
 * Created by johnrs on 2015-01-02.
 */
public abstract class DataActivity extends ActionBarActivity implements DriveStorage.OnDataRecievedCallback {

    private DriveStorage driveStorage;

    protected AppData data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        driveStorage = new DriveStorage(this, this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        driveStorage.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();

        driveStorage.connect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean handled = driveStorage.handleActivityResult(requestCode, resultCode, data);

        if(!handled) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDataRevieced(AppData data) {
        this.data = data;
    }

    private void commit() {
        driveStorage.commit(data);
    }
}
