package com.johnsimon.payback.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

/**
 * Created by johnrs on 2015-01-01.
 */
public class DriveStorage implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int REQUEST_CODE_RESOLUTION = 1;

    private GoogleApiClient client;
    private Activity context;

    public DriveStorage(Activity context) {
        this.context = context;

        client = new GoogleApiClient.Builder(context)
           .addApi(Drive.API)
           .addScope(Drive.SCOPE_APPFOLDER)
           .addConnectionCallbacks(this)
           .addOnConnectionFailedListener(this)
           .build();
    }

    public void connect() {
        client.connect();
    }

    public void disconnect() {
        client.disconnect();
    }

    public boolean onResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_RESOLUTION:
                if (resultCode == Activity.RESULT_OK) {
                    client.connect();
                }
                return true;
        }
        return false;
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(context, REQUEST_CODE_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                // Unable to resolve, message user appropriately
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), context, 0).show();
        }
    }
}
